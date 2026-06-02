package com.medinapaul.dreadpasta;

import com.medinapaul.dreadpasta.block.DreadPortalBlock;
import com.medinapaul.dreadpasta.entity.StalkerEntity;
import com.medinapaul.dreadpasta.net.JumpscarePayload;
import com.medinapaul.dreadpasta.world.DreadTeleporter;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;

import java.util.List;

public class DreadpastaMod implements ModInitializer {
    public static final String MOD_ID = "dreadpasta";

    public static boolean ENABLED = true;
    public static int INTENSITY = 4; // 0..5, 4 = частые скримеры и разрушения

    public static final RegistryKey<World> DREAD_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD, id("dread"));

    public static final Identifier STALKER_ID = id("stalker");
    public static final RegistryKey<EntityType<?>> STALKER_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, STALKER_ID);
    public static final EntityType<StalkerEntity> STALKER = Registry.register(
            Registries.ENTITY_TYPE,
            STALKER_ID,
            EntityType.Builder.<StalkerEntity>create(StalkerEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6F, 1.95F)
                    .build(STALKER_KEY)
    );

    public static final Identifier DREAD_PORTAL_ID = id("dread_portal");
    public static final RegistryKey<Block> DREAD_PORTAL_KEY = RegistryKey.of(RegistryKeys.BLOCK, DREAD_PORTAL_ID);
    public static final Block DREAD_PORTAL = Registry.register(
            Registries.BLOCK,
            DREAD_PORTAL_ID,
            new DreadPortalBlock(AbstractBlock.Settings.create()
                    .registryKey(DREAD_PORTAL_KEY)
                    .noCollision()
                    .luminance(state -> 12)
                    .strength(-1.0F, 3600000.0F)
                    .dropsNothing())
    );

    private static int tickCounter = 0;

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(JumpscarePayload.ID, JumpscarePayload.CODEC);
        FabricDefaultAttributeRegistry.register(STALKER, StalkerEntity.createStalkerAttributes());

        registerCommands();
        ServerTickEvents.END_WORLD_TICK.register(DreadpastaMod::onWorldTick);
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                CommandManager.literal("dreadpasta")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("on").executes(ctx -> {
                            ENABLED = true;
                            ctx.getSource().sendFeedback(() -> Text.literal("Dreadpasta: ON"), false);
                            return 1;
                        }))
                        .then(CommandManager.literal("off").executes(ctx -> {
                            ENABLED = false;
                            ctx.getSource().sendFeedback(() -> Text.literal("Dreadpasta: OFF"), false);
                            return 1;
                        }))
                        .then(CommandManager.literal("intensity")
                                .then(CommandManager.argument("value", IntegerArgumentType.integer(0, 5)).executes(ctx -> {
                                    INTENSITY = IntegerArgumentType.getInteger(ctx, "value");
                                    ctx.getSource().sendFeedback(() -> Text.literal("Dreadpasta intensity = " + INTENSITY), false);
                                    return INTENSITY;
                                })))
                        .then(CommandManager.literal("spawn").executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            spawnStalkerNear(player.getServerWorld(), player.getBlockPos(), player);
                            return 1;
                        }))
                        .then(CommandManager.literal("dread").executes(ctx -> {
                            DreadTeleporter.teleportToDread(ctx.getSource().getPlayerOrThrow());
                            return 1;
                        }))
        ));
    }

    private static void onWorldTick(ServerWorld world) {
        if (!ENABLED || world.getPlayers().isEmpty()) return;

        tickCounter++;
        int interval = MathHelper.clamp(90 - INTENSITY * 14, 18, 90);
        if (tickCounter % interval != 0) return;

        Random random = world.getRandom();
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.isSpectator() || player.isCreative()) continue;

            int roll = random.nextInt(100);
            if (roll < 28 + INTENSITY * 9) triggerJumpscare(player, 34 + INTENSITY * 8);
            if (roll < 22 + INTENSITY * 7) glitchChat(player, random);
            if (roll < 18 + INTENSITY * 8) strikeLightningNear(world, player, random);
            if (roll < 16 + INTENSITY * 7) deleteBlocksNear(world, player, random);
            if (roll < 12 + INTENSITY * 7) breakChunkPatch(world, player, random);
            if (roll < 20 + INTENSITY * 6) ensureStalker(world, player);
        }
    }

    public static void triggerJumpscare(ServerPlayerEntity player, int ticks) {
        ServerPlayNetworking.send(player, new JumpscarePayload(ticks));
        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_SCREAM, SoundCategory.HOSTILE, 2.0F, 0.45F);
        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_WARDEN_ROAR, SoundCategory.HOSTILE, 1.2F, 0.65F);
        player.sendMessage(Text.literal("§4§k000 §cНЕ ОБОРАЧИВАЙСЯ §4§k000"), true);
    }

    private static void glitchChat(ServerPlayerEntity player, Random random) {
        String[] messages = {
                "§0[§4NULL§0] §kWHERE ARE YOU",
                "§4<unknown> §cя вижу тебя",
                "§8Система: §cчанк повреждён: " + player.getChunkPos().x + ", " + player.getChunkPos().z,
                "§5§kXXXXXXXX §r§4НЕ БЕГИ §5§kXXXXXXXX",
                "§c" + player.getName().getString() + " left the game",
                "§4[ERROR] §cplayer_model/nameplate = null"
        };
        player.sendMessage(Text.literal(messages[random.nextInt(messages.length)]), false);
    }

    private static void strikeLightningNear(ServerWorld world, ServerPlayerEntity player, Random random) {
        BlockPos pos = player.getBlockPos().add(random.nextBetween(-10, 10), 0, random.nextBetween(-10, 10));
        pos = world.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, pos);
        LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        bolt.refreshPositionAfterTeleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        world.spawnEntity(bolt);
    }

    private static void deleteBlocksNear(ServerWorld world, ServerPlayerEntity player, Random random) {
        BlockPos origin = player.getBlockPos();
        int count = 6 + INTENSITY * 5;
        for (int i = 0; i < count; i++) {
            BlockPos pos = origin.add(random.nextBetween(-7, 7), random.nextBetween(-2, 4), random.nextBetween(-7, 7));
            BlockState state = world.getBlockState(pos);
            if (state.isAir() || state.isOf(Blocks.BEDROCK) || world.getBlockEntity(pos) != null) continue;
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            if (random.nextInt(4) == 0) {
                world.playSound(null, pos, SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK, SoundCategory.BLOCKS, 1.1F, 0.5F);
            }
        }
    }

    private static void breakChunkPatch(ServerWorld world, ServerPlayerEntity player, Random random) {
        BlockPos base = player.getBlockPos().withY(MathHelper.clamp(player.getBlockY(), world.getBottomY() + 5, world.getTopY() - 10));
        int radius = 8;
        int count = 22 + INTENSITY * 12;
        Block[] glitchBlocks = {Blocks.BLACK_CONCRETE, Blocks.CRYING_OBSIDIAN, Blocks.SCULK, Blocks.REDSTONE_BLOCK, Blocks.AIR};

        for (int i = 0; i < count; i++) {
            BlockPos pos = base.add(random.nextBetween(-radius, radius), random.nextBetween(-4, 5), random.nextBetween(-radius, radius));
            if (world.getBlockState(pos).isOf(Blocks.BEDROCK) || world.getBlockEntity(pos) != null) continue;
            Block block = glitchBlocks[random.nextInt(glitchBlocks.length)];
            world.setBlockState(pos, block.getDefaultState(), Block.NOTIFY_ALL);
        }
    }

    private static void ensureStalker(ServerWorld world, ServerPlayerEntity player) {
        Box box = player.getBoundingBox().expand(80.0);
        List<StalkerEntity> nearby = world.getEntitiesByClass(StalkerEntity.class, box, entity -> entity.isAlive());
        if (nearby.isEmpty()) {
            spawnStalkerNear(world, player.getBlockPos(), player);
        } else {
            StalkerEntity stalker = nearby.get(0);
            if (stalker.squaredDistanceTo(player) > 65 * 65) {
                stalker.refreshPositionAndAngles(player.getX() + 12.0, player.getY(), player.getZ() - 12.0, player.getYaw() + 180.0F, 0.0F);
            }
        }
    }

    public static void spawnStalkerNear(ServerWorld world, BlockPos around, PlayerEntity target) {
        StalkerEntity stalker = new StalkerEntity(STALKER, world);
        BlockPos pos = around.add(8, 0, -8);
        pos = world.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos);
        stalker.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, target.getYaw() + 180.0F, 0.0F);
        stalker.setTarget(target instanceof ServerPlayerEntity sp ? sp : null);
        stalker.setPersistent();
        stalker.setSilent(true);
        stalker.setCustomNameVisible(false);
        world.spawnEntity(stalker);
    }
}
