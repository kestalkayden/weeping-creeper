package com.kestalkayden.weepingcreeper.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** JSON config at config/weepingcreeper.json. Default behaviour: every vanilla
 *  creeper spawn becomes a weeping creeper; cosmetic tears overlay enabled. Server
 *  owners dial down replacementChance for a hybrid world, or set 0 to spawn weeping
 *  creepers only via spawn eggs. */
public final class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("weepingcreeper");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "weepingcreeper.json";

    private static ModConfig INSTANCE = new ModConfig();

    /** Fraction of vanilla creeper spawns that become weeping creepers. 1.0 = always
     *  replace, 0.0 = never (spawn eggs only). Clamped to [0, 1]. */
    public double replacementChance = 1.0;

    /** Movement speed attribute for weeping creepers. Vanilla creeper is 0.25; the
     *  default 0.40 is noticeably faster (+60%) without feeling oppressive. */
    public double movementSpeed = 0.40;

    /** Cone (in degrees) in front of the player that counts as "looking at". When a
     *  weeping creeper falls within this arc AND has line-of-sight, it freezes. */
    public double lookArcDegrees = 120.0;

    /** Any weeping creeper within this distance of any player freezes regardless of
     *  look angle. Safety net for melee range so close encounters don't always end
     *  in explosions. Set 0 to disable. */
    public double proximityFreezeRadius = 1.5;

    /** Detection / pursuit range. Vanilla creeper FOLLOW_RANGE is around 16; the
     *  default 64 makes them chase from much further away. */
    public double followRange = 64.0;

    /** Whether to render the tears overlay on top of the vanilla creeper texture. */
    public boolean tearsEnabled = true;

    /** Whether to spawn a heart particle above the creeper while it's being
     *  observed — comedic "I see you seeing me" cue. Off by default; the tonal
     *  shift to comedic hearts doesn't fit every player's taste. */
    public boolean heartParticlesEnabled = false;

    /** Ticks between heart particle spawns while observed. 20 ticks = 1 second;
     *  default 60 = once every 3 seconds. */
    public int heartIntervalTicks = 60;

    /** Uniform brightness multiplier applied to the creeper texture (entity tint).
     *  1.0 = no change, 0.0 = pure black. The default 0.55 makes weeping creepers
     *  noticeably darker than vanilla while preserving the vanilla texture path so
     *  installed resource packs (Faithful, Sphax, etc.) apply their HD creeper
     *  retextures, just tinted darker. */
    public double tintBrightness = 0.55;

    /** Per-channel "drain green" factor for a statue-grey aesthetic. 0.0 = uniform
     *  darken (just the tintBrightness multiplier). 1.0 = aggressively suppress the
     *  green channel so vanilla creeper green pulls toward neutral grey. The default
     *  0.25 is gentle — strong values shift near-grey pixels (eyes/mouth) toward
     *  magenta, since reducing G more than R/B inverts the channel ratio on greys.
     *  Most of the "statue feel" comes from tintBrightness; desaturation just adds
     *  a touch of greying on top. */
    public double desaturation = 0.25;

    /** Explosion radius for non-charged weeping creepers. Vanilla is 3.0. */
    public float explosionRadius = 3.0f;

    /** Explosion radius when struck by lightning (charged). Vanilla charged is 6.0. */
    public float chargedExplosionRadius = 6.0f;

    public static ModConfig get() { return INSTANCE; }

    public static void load(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        if (!Files.exists(file)) {
            INSTANCE = new ModConfig();
            save(configDir);
            return;
        }
        try {
            ModConfig loaded = GSON.fromJson(Files.readString(file), ModConfig.class);
            INSTANCE = loaded != null ? loaded : new ModConfig();
        } catch (JsonSyntaxException | IOException e) {
            LOGGER.error("Failed to read {}, using defaults", file, e);
            INSTANCE = new ModConfig();
        }
        INSTANCE.replacementChance = Math.max(0.0, Math.min(1.0, INSTANCE.replacementChance));
        INSTANCE.movementSpeed = Math.max(0.01, Math.min(2.0, INSTANCE.movementSpeed));
        INSTANCE.lookArcDegrees = Math.max(1.0, Math.min(360.0, INSTANCE.lookArcDegrees));
        INSTANCE.proximityFreezeRadius = Math.max(0.0, Math.min(64.0, INSTANCE.proximityFreezeRadius));
        INSTANCE.followRange = Math.max(4.0, Math.min(256.0, INSTANCE.followRange));
        INSTANCE.heartIntervalTicks = Math.max(1, Math.min(6000, INSTANCE.heartIntervalTicks));
        INSTANCE.tintBrightness = Math.max(0.0, Math.min(1.0, INSTANCE.tintBrightness));
        INSTANCE.desaturation = Math.max(0.0, Math.min(1.0, INSTANCE.desaturation));
        INSTANCE.explosionRadius = Math.max(0.5f, Math.min(20.0f, INSTANCE.explosionRadius));
        INSTANCE.chargedExplosionRadius = Math.max(0.5f, Math.min(40.0f, INSTANCE.chargedExplosionRadius));
    }

    private static void save(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        try {
            Files.createDirectories(configDir);
            Files.writeString(file, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            LOGGER.error("Failed to write default config to {}", file, e);
        }
    }
}
