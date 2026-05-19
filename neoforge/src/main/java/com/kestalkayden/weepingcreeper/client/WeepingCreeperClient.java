package com.kestalkayden.weepingcreeper.client;

import com.kestalkayden.weepingcreeper.entity.WeepingCreeperEntities;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only setup. Called from the main mod class behind a Dist.CLIENT guard so
 *  loading on a dedicated server never touches client-only classes. */
public final class WeepingCreeperClient {

    private WeepingCreeperClient() {}

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
            WeepingCreeperEntities.WEEPING_CREEPER.get(),
            WeepingCreeperRenderer::new);
    }
}
