package com.medinapaul.dreadpasta.block;

import com.medinapaul.dreadpasta.world.DreadTeleporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DreadPortalBlock extends Block {
    public DreadPortalBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && entity instanceof ServerPlayerEntity player) {
            DreadTeleporter.escapeToOverworld(player);
        }
        super.onEntityCollision(state, world, pos, entity);
    }
}
