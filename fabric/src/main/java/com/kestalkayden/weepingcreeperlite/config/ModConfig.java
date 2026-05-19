package com.kestalkayden.weepingcreeperlite.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** JSON config at config/weepingcreeperlite.json. Default behaviour: every vanilla
 *  creeper spawn becomes a weeping creeper; cosmetic tears overlay enabled. Server
 *  owners dial down replacementChance for a hybrid world, or set 0 to spawn weeping
 *  creepers only via spawn eggs. */
public final class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("weepingcreeperlite");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "weepingcreeperlite.json";

    private static ModConfig INSTANCE = new ModConfig();

    /** Fraction of vanilla creeper spawns that become weeping creepers. 1.0 = always
     *  replace, 0.0 = never (spawn eggs only). Clamped to [0, 1]. */
    public double replacementChance = 1.0;

    /** Movement speed attribute for weeping creepers. Vanilla creeper is 0.25; the
     *  default 0.35 is noticeably faster but not zombie-tier. */
    public double movementSpeed = 0.35;

    /** Cone (in degrees) in front of the player that counts as "looking at". When a
     *  weeping creeper falls within this arc AND has line-of-sight, it freezes. */
    public double lookArcDegrees = 120.0;

    /** Whether to render the tears overlay on top of the vanilla creeper texture. */
    public boolean tearsEnabled = true;

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
        // Clamp ranges
        INSTANCE.replacementChance = Math.max(0.0, Math.min(1.0, INSTANCE.replacementChance));
        INSTANCE.movementSpeed = Math.max(0.01, Math.min(2.0, INSTANCE.movementSpeed));
        INSTANCE.lookArcDegrees = Math.max(1.0, Math.min(360.0, INSTANCE.lookArcDegrees));
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
