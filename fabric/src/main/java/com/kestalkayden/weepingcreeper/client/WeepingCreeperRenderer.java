package com.kestalkayden.weepingcreeper.client;

import com.kestalkayden.weepingcreeper.config.ModConfig;

import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;

/** Reuses the vanilla CreeperRenderer (inheriting the model, base texture path,
 *  scale, and the CreeperPowerLayer for charged variants), then layers our tears
 *  overlay on top and applies a config-driven brightness tint via {@link #getModelTint}.
 *
 *  Keeping the vanilla texture path means installed resource packs (Faithful,
 *  Sphax, etc.) apply their HD creeper retextures naturally — they just render
 *  through our slightly-darker tint. */
public class WeepingCreeperRenderer extends CreeperRenderer {
    public WeepingCreeperRenderer(EntityRendererProvider.Context context) {
        super(context);
        addLayer(new TearsLayer(this));
    }

    /** Per-channel tint applied to every vertex when the model draws. Returned as
     *  packed ARGB. R and B are scaled uniformly by tintBrightness; G is scaled
     *  more aggressively by tintBrightness × (1 − desaturation × 0.5) so the
     *  vanilla creeper's green skin drains toward grey at higher desaturation
     *  values. The 0.5 coefficient caps how aggressive the green-drain can get
     *  even at desat = 1 — keeps the magenta artifact on highlights bounded
     *  rather than running away. */
    @Override
    protected int getModelTint(CreeperRenderState state) {
        double brightness = ModConfig.get().tintBrightness;
        double desat = ModConfig.get().desaturation;
        double rb = brightness;
        double g = brightness * (1.0 - desat * 0.5);
        int rByte = clamp255(rb);
        int gByte = clamp255(g);
        int bByte = clamp255(rb);
        return (0xFF << 24) | (rByte << 16) | (gByte << 8) | bByte;
    }

    private static int clamp255(double v) {
        int i = (int) Math.round(255.0 * v);
        return Math.max(0, Math.min(255, i));
    }
}
