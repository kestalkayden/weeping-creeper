package com.kestalkayden.weepingcreeper.client;

import com.kestalkayden.weepingcreeper.entity.WeepingCreeperEntities;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class WeepingCreeperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(
            WeepingCreeperEntities.WEEPING_CREEPER,
            WeepingCreeperRenderer::new);
    }
}
