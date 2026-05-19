package com.kestalkayden.weepingcreeper.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.kestalkayden.weepingcreeper.entity.WeepingCreeperEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.monster.Creeper;

/** Intercepts the private {@code Creeper#explodeCreeper()} invocation inside
 *  {@code Creeper#tick()}. For weeping creepers, redirect to our player-only,
 *  terrain-safe explosion. For vanilla creepers, call through unchanged.
 *
 *  {@code @WrapOperation} comes from MixinExtras, which Fabric Loader bundles. */
@Mixin(Creeper.class)
public abstract class CreeperTickMixin {

    @WrapOperation(
        method = "tick",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/monster/Creeper;explodeCreeper()V"))
    private void weepingcreeper$redirectExplode(Creeper self, Operation<Void> original) {
        if (self instanceof WeepingCreeperEntity weeping) {
            weeping.explodeWeeping();
        } else {
            original.call(self);
        }
    }
}
