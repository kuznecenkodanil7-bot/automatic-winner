package com.medinapaul.dreadpasta.world;

import com.medinapaul.dreadpasta.DreadpastaMod;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.Set;

public final class DreadTeleporter {
    private DreadTeleporter() {}

    public static void teleportToDread(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerWorld dread = server.getWorld(DreadpastaMod.DREAD_WORLD_KEY);
        if (dread == null) {
            player.sendMessage(Text.literal("§4Dread dimension not found. Пересоздай мир/проверь datapack JSON."), false);
            return;
        }

        BlockPos arena = new BlockPos(0, 72, 0);
        buildArena(dread, arena);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 220, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 70, 0));
        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE, 1.5F, 0.6F);
        player.teleport(dread, arena.getX() + 0.5D, arena.getY() + 1.0D, arena.getZ() + 0.5D, Set.<PositionFlag>of(), player.getYaw(), player.getPitch(), true);
        player.sendMessage(Text.literal("§4ВЫБЕРИСЬ. ПОРТАЛ ОХРАНЯЕТ ТО, ЧТО ТЕБЯ ПРИВЕЛО."), false);

        DreadpastaMod.spawnStalkerNear(dread, arena.add(18, 0, 0), player);
    }

    public static void escapeToOverworld(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        RegistryKey<World> current = player.getWorld().getRegistryKey();
        if (!current.equals(DreadpastaMod.DREAD_WORLD_KEY)) return;

        ServerWorld overworld = server.getOverworld();
        BlockPos spawn = overworld.getSpawnPos();
        BlockPos safe = overworld.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, spawn);
        player.teleport(overworld, safe.getX() + 0.5D, safe.getY() + 1.0D, safe.getZ() + 0.5D, Set.<PositionFlag>of(), player.getYaw(), player.getPitch(), true);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 80, 0));
        overworld.playSound(null, safe.getX(), safe.getY(), safe.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0F, 0.5F);
        player.sendMessage(Text.literal("§2Ты выбрался. Но чат уже не твой."), false);
    }

    private static void buildArena(ServerWorld world, BlockPos center) {
        int floorY = center.getY();

        for (int x = -18; x <= 18; x++) {
            for (int z = -18; z <= 18; z++) {
                BlockPos floor = center.add(x, 0, z);
                world.setBlockState(floor, Blocks.DEEPSLATE_TILES.getDefaultState(), Block.NOTIFY_ALL);
                for (int y = 1; y <= 7; y++) {
                    BlockPos air = floor.up(y);
                    world.setBlockState(air, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                }
                if (Math.abs(x) == 18 || Math.abs(z) == 18) {
                    for (int y = 1; y <= 8; y++) {
                        Block block = y % 3 == 0 ? Blocks.SCULK : Blocks.BLACKSTONE;
                        world.setBlockState(floor.up(y), block.getDefaultState(), Block.NOTIFY_ALL);
                    }
                }
            }
        }

        // Длинный коридор к выходу
        for (int x = 19; x <= 34; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos p = center.add(x, 0, z);
                world.setBlockState(p, Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState(), Block.NOTIFY_ALL);
                for (int y = 1; y <= 5; y++) world.setBlockState(p.up(y), Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                if (Math.abs(z) == 3) {
                    for (int y = 1; y <= 5; y++) world.setBlockState(p.up(y), Blocks.CRYING_OBSIDIAN.getDefaultState(), Block.NOTIFY_ALL);
                }
            }
        }

        BlockPos portal = center.add(35, 1, 0);
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 3; y++) {
                boolean frame = x == -1 || x == 1 || y == 0 || y == 3;
                world.setBlockState(portal.add(0, y, x), frame ? Blocks.CRYING_OBSIDIAN.getDefaultState() : DreadpastaMod.DREAD_PORTAL.getDefaultState(), Block.NOTIFY_ALL);
            }
        }

        // Ловушки/атмосфера
        for (int i = 0; i < 18; i++) {
            BlockPos candle = center.add(world.getRandom().nextBetween(-14, 14), 1, world.getRandom().nextBetween(-14, 14));
            world.setBlockState(candle, Blocks.SCULK_SHRIEKER.getDefaultState(), Block.NOTIFY_ALL);
        }
    }
}
