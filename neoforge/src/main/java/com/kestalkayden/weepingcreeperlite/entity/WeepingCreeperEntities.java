package com.kestalkayden.weepingcreeperlite.entity;

import com.kestalkayden.weepingcreeperlite.WeepingCreeperNeoForge;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WeepingCreeperEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, WeepingCreeperNeoForge.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<WeepingCreeperEntity>> WEEPING_CREEPER =
        ENTITY_TYPES.register("weeping_creeper",
            () -> EntityType.Builder.<WeepingCreeperEntity>of(WeepingCreeperEntity::new, MobCategory.MONSTER)
                .sized(0.6f, 1.7f)
                .clientTrackingRange(8)
                .build(ResourceKey.create(Registries.ENTITY_TYPE,
                    Identifier.fromNamespaceAndPath(WeepingCreeperNeoForge.MOD_ID, "weeping_creeper")))
        );

    private WeepingCreeperEntities() {}

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
