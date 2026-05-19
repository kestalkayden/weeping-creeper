package com.kestalkayden.weepingcreeperlite.client;

import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class WeepingCreeperRenderer extends CreeperRenderer {
    public WeepingCreeperRenderer(EntityRendererProvider.Context context) {
        super(context);
        addLayer(new TearsLayer(this));
    }
}
