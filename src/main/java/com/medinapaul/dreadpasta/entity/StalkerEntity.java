package com.medinapaul.dreadpasta.entity;

import com.medinapaul.dreadpasta.DreadpastaMod;
import com.medinapaul.dreadpasta.world.DreadTeleporter;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class StalkerEntity extends ZombieEntity {
    private int touchCooldown = 0;
    private int stareTick = 0;

    public StalkerEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 0;
        this.setSilent(true);
        this.setCustomNameVisible(false);
    }

    public static DefaultAttributeContainer.Builder createStalkerAttributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.MAX_HEALTH, 80.0D)
                .add(EntityAttributes.ATTACK_DAMAGE, 7.0D)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.36D)
                .add(EntityAttributes.FOLLOW_RANGE, 96.0D)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.75D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.38D, true));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.72D));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;

        if (touchCooldown > 0) touchCooldown--;
        stareTick++;

        PlayerEntity closest = this.getWorld().getClosestPlayer(this, 1.25D);
        if (closest instanceof ServerPlayerEntity serverPlayer && touchCooldown <= 0) {
            touchCooldown = 80;
            DreadpastaMod.triggerJumpscare(serverPlayer, 70);
            DreadTeleporter.teleportToDread(serverPlayer);
        }

        LivingEntity target = this.getTarget();
        if (target instanceof ServerPlayerEntity player) {
            if (this.squaredDistanceTo(player) > 70.0D * 70.0D) {
                this.refreshPositionAndAngles(player.getX() + 10.0D, player.getY(), player.getZ() - 10.0D, player.getYaw() + 180.0F, 0.0F);
            }
            if (stareTick % 60 == 0 && this.squaredDistanceTo(player) < 18.0D * 18.0D) {
                DreadpastaMod.triggerJumpscare(player, 28);
            }
        }
    }

    @Override
    protected boolean burnsInDaylight() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_WARDEN_HEARTBEAT;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.entity.damage.DamageSource source) {
        return SoundEvents.ENTITY_ENDERMAN_SCREAM;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WARDEN_DEATH;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }
}
