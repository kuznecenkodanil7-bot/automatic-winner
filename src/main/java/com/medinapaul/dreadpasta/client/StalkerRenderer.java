package com.medinapaul.dreadpasta.client;

import com.medinapaul.dreadpasta.DreadpastaMod;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;

public class StalkerRenderer extends ZombieEntityRenderer {
    private static final Identifier TEXTURE = DreadpastaMod.id("textures/entity/stalker.png");

    public StalkerRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(ZombieEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    protected boolean hasLabel(ZombieEntity entity, double squaredDistanceToCamera) {
        return false;
    }
}
