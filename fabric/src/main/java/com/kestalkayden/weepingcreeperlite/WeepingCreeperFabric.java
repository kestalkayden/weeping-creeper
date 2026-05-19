package com.kestalkayden.weepingcreeperlite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.weepingcreeperlite.config.ModConfig;
import com.kestalkayden.weepingcreeperlite.entity.WeepingCreeperEntities;
import com.kestalkayden.weepingcreeperlite.entity.WeepingCreeperEntity;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.Creeper;

public class WeepingCreeperFabric implements ModInitializer {
    public static final String MOD_ID = "weepingcreeperlite";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Weeping Creeper (Fabric)");
        ModConfig.load(FabricLoader.getInstance().getConfigDir());
        WeepingCreeperEntities.register();

        // Replace vanilla creepers on spawn. ServerEntityEvents.ENTITY_LOAD fires also
        // for entities loaded from save, so we gate on tickCount == 0 to detect a
        // fresh spawn. Exact-class check (not instanceof) so we don't recurse on our
        // own subclass spawn.
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (entity.tickCount != 0) return;
            if (entity.getClass() != Creeper.class) return;
            if (!(level instanceof ServerLevel sl)) return;
            double chance = ModConfig.get().replacementChance;
            if (chance <= 0.0) return;
            if (chance < 1.0 && sl.getRandom().nextDouble() >= chance) return;

            Creeper vanilla = (Creeper) entity;
            WeepingCreeperEntity weeping = WeepingCreeperEntities.WEEPING_CREEPER.create(sl, EntitySpawnReason.NATURAL);
            if (weeping == null) return;
            weeping.snapTo(vanilla.getX(), vanilla.getY(), vanilla.getZ(),
                           vanilla.getYRot(), vanilla.getXRot());
            vanilla.discard();
            sl.addFreshEntity(weeping);
        });
    }
}
