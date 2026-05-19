package com.kestalkayden.weepingcreeperlite.client;

import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Reuses the vanilla CreeperRenderer (inheriting its model, base texture, scale,
 *  and the CreeperPowerLayer for charged variants), then layers our tears overlay
 *  on top. Using the vanilla texture path means any installed texture pack's
 *  creeper retexture applies to weeping creepers too — only the tears overlay is
 *  ours. */
public class WeepingCreeperRenderer extends CreeperRenderer {
    public WeepingCreeperRenderer(EntityRendererProvider.Context context) {
        super(context);
        addLayer(new TearsLayer(this));
    }
}
