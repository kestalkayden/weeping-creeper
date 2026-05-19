package com.kestalkayden.weepingcreeperlite.client;

import com.kestalkayden.weepingcreeperlite.WeepingCreeperFabric;
import com.kestalkayden.weepingcreeperlite.config.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Draws the tears overlay on top of the vanilla creeper texture. Toggled live via
 *  {@link ModConfig#tearsEnabled} — no resource reload needed. */
public class TearsLayer extends RenderLayer<CreeperRenderState, CreeperModel> {
    private static final Identifier TEARS = Identifier.fromNamespaceAndPath(
        WeepingCreeperFabric.MOD_ID, "textures/entity/tears.png");

    public TearsLayer(RenderLayerParent<CreeperRenderState, CreeperModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light,
                       CreeperRenderState state, float yRot, float xRot) {
        if (!ModConfig.get().tearsEnabled) return;
        renderColoredCutoutModel(getParentModel(), TEARS, pose, collector, light, state,
            0xFFFFFFFF, OverlayTexture.NO_OVERLAY);
    }
}
