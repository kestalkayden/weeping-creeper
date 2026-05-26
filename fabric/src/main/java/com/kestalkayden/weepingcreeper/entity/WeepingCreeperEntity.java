package com.kestalkayden.weepingcreeper.entity;

import com.kestalkayden.weepingcreeper.config.ModConfig;
import com.kestalkayden.weepingcreeper.mixin.CreeperAccessor;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
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

    /** Gametime of the most recent heart particle. Used to throttle particles to
     *  the configured interval. Initialized to 0 (not Long.MIN_VALUE — subtracting
     *  that from a positive game time wraps to a negative number, which would fail
     *  the interval check forever and suppress all hearts). Not persisted; resets
     *  to 0 on chunk reload, which at worst causes one extra heart on first
     *  observation after reload. */
    private long lastHeartTick = 0L;

    /** Explosion damage calculator that damages nothing and applies no knockback.
     *  The weeping creeper's blast is cosmetic; all damage comes from the manual
     *  player-only pulse in {@link #explodeWeeping()}. Necessary because
     *  {@code level().explode(..., NONE)} still runs the vanilla entity-damage pass —
     *  {@code NONE} spares only blocks — which would otherwise destroy boats, item
     *  frames, armor stands, mobs, etc. and double-hit players. */
    private static final ExplosionDamageCalculator NO_ENTITY_DAMAGE = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return false;
        }

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            return 0.0F;
        }
    };

    public WeepingCreeperEntity(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, ModConfig.get().movementSpeed)
            .add(Attributes.FOLLOW_RANGE, ModConfig.get().followRange);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (isBeingObserved(level)) {
            // Total freeze. Skipping super.customServerAiStep is not enough — by the
            // time this method runs, Mob.serverAiStep has already ticked goals AND
            // navigation, so the entity already advanced along its path this tick.
            // Explicitly stop the active path and drop the attack target so the next
            // goal tick doesn't immediately repath. Zero horizontal velocity for
            // good measure (vertical preserved for gravity).
            getNavigation().stop();
            setTarget(null);
            setDeltaMovement(0.0, getDeltaMovement().y, 0.0);
            // Hard-reset the swell fuse so a creeper that was mid-explosion when the
            // player started observing doesn't still detonate. setSwellDir(-1) alone
            // only decrements by 1 per tick — too slow when the player walks into
            // melee range with the fuse already near max.
            setSwellDir(-1);
            ((CreeperAccessor) (Object) this).weeping$setSwell(0);
            maybeSpawnHeartParticle(level);
            return;
        }
        super.customServerAiStep(level);
    }

    /** Throttled heart particle spawn while observed — the "I see you seeing me"
     *  cue. ServerLevel.sendParticles broadcasts to all nearby clients in one
     *  call; no manual ranging needed. */
    private void maybeSpawnHeartParticle(ServerLevel level) {
        if (!ModConfig.get().heartParticlesEnabled) return;
        long now = level.getGameTime();
        if (now - lastHeartTick < ModConfig.get().heartIntervalTicks) return;
        lastHeartTick = now;
        level.sendParticles(ParticleTypes.HEART,
            getX(), getY() + getBbHeight() + 0.3, getZ(),
            1,
            0.3, 0.0, 0.3,
            0.0);
    }

    /** Tests whether any player on the server is currently looking at this creeper,
     *  OR is within the proximity bubble (close-range safety net). Iteration is
     *  bounded to a 64-block radius — the practical range a player could ever see
     *  a mob within. Uses the creeper's bounding-box center (not its feet position)
     *  as the look target, otherwise close-range checks fail when the player looks
     *  horizontally and the eye-to-feet vector points sharply downward, falling
     *  outside the front cone. */
    private boolean isBeingObserved(ServerLevel level) {
        double proximitySq = ModConfig.get().proximityFreezeRadius * ModConfig.get().proximityFreezeRadius;
        double halfArcCos = Math.cos(Math.toRadians(ModConfig.get().lookArcDegrees / 2.0));
        Vec3 creeperCenter = getBoundingBox().getCenter();
        for (Player player : level.players()) {
            double distSq = player.distanceToSqr(this);
            if (distSq > 64.0 * 64.0) continue;
            // Proximity bubble: any close-enough player freezes the creeper regardless
            // of look angle. Skip line-of-sight check too — at melee range we assume
            // the player is aware of the creeper.
            if (proximitySq > 0 && distSq <= proximitySq) return true;
            // Look-cone + LOS check.
            Vec3 lookVec = player.getLookAngle();
            Vec3 toCreeper = creeperCenter.subtract(player.getEyePosition()).normalize();
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

        // Cosmetic explosion only: particles + sound, no block damage (NONE) and no
        // entity damage or knockback (NO_ENTITY_DAMAGE). ExplosionInteraction.NONE
        // spares only *blocks*; the vanilla explosion still runs an entity-damage pass
        // gated by the damage calculator, so the no-op calculator is what keeps boats,
        // mobs, item frames, etc. unharmed. The manual pulse below is the sole damage
        // source.
        level().explode(
            this,
            null,
            NO_ENTITY_DAMAGE,
            getX(), getY(), getZ() + getBbHeight() * 0.0625,
            radius,
            false,
            Level.ExplosionInteraction.NONE);

        // Manual damage pulse: only players, with linear distance falloff matching
        // vanilla creeper damage scaling (radius * 7 at center, fading to 0 at edge).
        DamageSource source = damageSources().mobAttack(this);
        AABB area = getBoundingBox().inflate(radius);
        for (Player p : level().getEntitiesOfClass(Player.class, area)) {
            double dist = p.distanceTo(this);
            if (dist > radius) continue;
            float falloff = (float) (1.0 - dist / radius);
            float damage = (float) (radius * 7.0f * falloff * ModConfig.get().explosionDamageMultiplier);
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
