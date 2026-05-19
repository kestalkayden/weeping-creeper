package com.kestalkayden.weepingcreeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.weepingcreeper.client.WeepingCreeperClient;
import com.kestalkayden.weepingcreeper.config.ModConfig;
import com.kestalkayden.weepingcreeper.entity.WeepingCreeperEntities;
import com.kestalkayden.weepingcreeper.entity.WeepingCreeperEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(WeepingCreeperNeoForge.MOD_ID)
public class WeepingCreeperNeoForge {
    public static final String MOD_ID = "weepingcreeper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public WeepingCreeperNeoForge(IEventBus modBus) {
        LOGGER.info("Initializing Weeping Creeper (NeoForge)");
        ModConfig.load(FMLPaths.CONFIGDIR.get());
        WeepingCreeperEntities.register(modBus);

        modBus.addListener(WeepingCreeperNeoForge::onRegisterAttributes);
        NeoForge.EVENT_BUS.addListener(WeepingCreeperNeoForge::onEntityJoinLevel);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modBus.addListener(WeepingCreeperClient::onRegisterRenderers);
        }
    }

    private static void onRegisterAttributes(EntityAttributeCreationEvent event) {
        event.put(WeepingCreeperEntities.WEEPING_CREEPER.get(), WeepingCreeperEntity.createAttributes().build());
    }

    /** Replace vanilla creepers on spawn. EntityJoinLevelEvent fires for all entities
     *  joining the world (including chunk-loaded ones); we gate on tickCount == 0 to
     *  isolate fresh spawns. Exact-class check (not instanceof) so we don't recurse on
     *  our own subclass. */
    private static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk()) return;
        var entity = event.getEntity();
        if (entity.getClass() != Creeper.class) return;
        if (!(event.getLevel() instanceof ServerLevel sl)) return;
        double chance = ModConfig.get().replacementChance;
        if (chance <= 0.0) return;
        if (chance < 1.0 && sl.getRandom().nextDouble() >= chance) return;

        Creeper vanilla = (Creeper) entity;
        WeepingCreeperEntity weeping = WeepingCreeperEntities.WEEPING_CREEPER.get().create(sl, EntitySpawnReason.NATURAL);
        if (weeping == null) return;
        weeping.snapTo(vanilla.getX(), vanilla.getY(), vanilla.getZ(),
                       vanilla.getYRot(), vanilla.getXRot());
        event.setCanceled(true);
        sl.addFreshEntity(weeping);
    }
}
