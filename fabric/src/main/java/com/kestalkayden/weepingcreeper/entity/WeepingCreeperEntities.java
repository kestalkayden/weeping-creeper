package com.kestalkayden.weepingcreeper.entity;

import com.kestalkayden.weepingcreeper.WeepingCreeperFabric;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.Registry;

public final class WeepingCreeperEntities {

    public static final Identifier WEEPING_CREEPER_ID =
        Identifier.fromNamespaceAndPath(WeepingCreeperFabric.MOD_ID, "weeping_creeper");

    public static final EntityType<WeepingCreeperEntity> WEEPING_CREEPER =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            WEEPING_CREEPER_ID,
            EntityType.Builder.<WeepingCreeperEntity>of(WeepingCreeperEntity::new, MobCategory.MONSTER)
                .sized(0.6f, 1.7f)
                .clientTrackingRange(8)
                // Despawn on Peaceful, matching vanilla creeper. In 26.1 this is a
                // per-type flag (EntityType.isAllowedInPeaceful), not inherited from
                // MobCategory.MONSTER — without it, weeping creepers persist on Peaceful.
                .notInPeaceful()
                .build(ResourceKey.create(Registries.ENTITY_TYPE, WEEPING_CREEPER_ID))
        );

    private WeepingCreeperEntities() {}

    public static void register() {
        FabricDefaultAttributeRegistry.register(WEEPING_CREEPER, WeepingCreeperEntity.createAttributes());
    }
}
