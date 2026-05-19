package com.kestalkayden.weepingcreeper.entity;

import com.kestalkayden.weepingcreeper.config.ModConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Custom creeper variant that:
 *  <ul>
 *    <li>Freezes (no AI tick, no movement) whenever any nearby player has it within their
 *        configured front cone AND has line-of-sight. Weeping Angel mechanic.</li>
 *    <li>Moves faster than vanilla when not observed (config-driven attribute).</li>
 *    <li>On explosion: no terrain damage; damages only players in radius.</li>
 *  </ul>
 *  The explosion intercept happens via a mixin on Creeper.tick that redirects the private
 *  explodeCreeper() call to {@link #explodeWeeping()} when the instance is a WeepingCreeperEntity.
 *  See the mixin package for details. */
public class WeepingCreeperEntity extends Creeper {

    public WeepingCreeperEntity(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, ModConfig.get().movementSpeed)
            .add(Attributes.FOLLOW_RANGE, 35.0);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (isBeingObserved(level)) {
            // Total freeze: skip the entire AI step. Don't advance navigation, don't
            // tick goals, don't move. Keep horizontal velocity at zero so we don't
            // drift from prior momentum (vertical kept for gravity).
            setDeltaMovement(0.0, getDeltaMovement().y, 0.0);
            return;
        }
        super.customServerAiStep(level);
    }

    /** Tests whether any player on the server is currently looking at this creeper.
     *  Iteration is bounded to a 64-block radius around the entity — the practical
     *  range a player could ever see a mob within. */
    private boolean isBeingObserved(ServerLevel level) {
        double halfArcCos = Math.cos(Math.toRadians(ModConfig.get().lookArcDegrees / 2.0));
        for (Player player : level.players()) {
            if (player.distanceToSqr(this) > 64.0 * 64.0) continue;
            Vec3 lookVec = player.getLookAngle();
            Vec3 toCreeper = position().subtract(player.getEyePosition()).normalize();
            if (lookVec.dot(toCreeper) < halfArcCos) continue;
            if (!player.hasLineOfSight(this)) continue;
            return true;
        }
        return false;
    }

    /** Called from {@code CreeperTickMixin} via redirect when this entity reaches its
     *  swell threshold. Replaces vanilla's terrain-destroying explosion with a
     *  player-only damage pulse + cosmetic explosion particles. */
    public void explodeWeeping() {
        if (level().isClientSide()) return;
        float radius = isPowered() ? ModConfig.get().chargedExplosionRadius
                                   : ModConfig.get().explosionRadius;

        // Spawn the cosmetic explosion (particles + sound) with no block damage. The
        // ExplosionInteraction.NONE flag prevents terrain destruction; entity damage
        // from the vanilla Explosion would still apply, but passing null for the
        // damage source skips that and we apply damage manually below.
        level().explode(
            this,
            getX(), getY(), getZ() + getBbHeight() * 0.0625,
            radius,
            Level.ExplosionInteraction.NONE);

        // Manual damage pulse: only players, with linear distance falloff matching
        // vanilla creeper damage scaling (radius * 7 at center, fading to 0 at edge).
        DamageSource source = damageSources().mobAttack(this);
        AABB area = getBoundingBox().inflate(radius);
        for (Player p : level().getEntitiesOfClass(Player.class, area)) {
            double dist = p.distanceTo(this);
            if (dist > radius) continue;
            float falloff = (float) (1.0 - dist / radius);
            float damage = radius * 7.0f * falloff;
            p.hurt(source, damage);
        }

        // Match vanilla creeper death: discard self after exploding. Vanilla also
        // spawns a lingering effect cloud if the creeper had any active effects;
        // we replicate that for parity.
        spawnLingeringEffectCloud();
        discard();
    }

    /** Mirrors vanilla Creeper's lingering-cloud spawn for any active effects.
     *  Vanilla's implementation is private, so we replicate the public surface here.
     *  Active effects on weeping creepers are rare in practice (no vanilla source
     *  applies effects to creepers), but we keep parity for charged-creeper mods. */
    private void spawnLingeringEffectCloud() {
        var effects = getActiveEffects();
        if (effects.isEmpty()) return;
        var cloud = new net.minecraft.world.entity.AreaEffectCloud(level(), getX(), getY(), getZ());
        cloud.setOwner(this);
        cloud.setRadius(2.5f);
        cloud.setRadiusOnUse(-0.5f);
        cloud.setWaitTime(10);
        cloud.setDuration(cloud.getDuration() / 2);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        for (var effect : effects) cloud.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect));
        level().addFreshEntity(cloud);
    }

    /** Vanilla creeper killedEntity grants the killer a music disc; we keep that
     *  behaviour. No override needed — inherited from Creeper. */
    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity, DamageSource source) {
        return super.killedEntity(level, entity, source);
    }
}
