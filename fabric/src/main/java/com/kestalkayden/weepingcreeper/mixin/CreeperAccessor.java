package com.kestalkayden.weepingcreeper.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.monster.Creeper;

/** Exposes Creeper's private swell field so WeepingCreeperEntity can hard-reset
 *  the fuse to zero each tick while it's observed. Without this we can only call
 *  the public setSwellDir(-1) which decrements one per tick — too slow if the
 *  player walks up to a creeper that's already mid-explosion (max - 1 swell). */
@Mixin(Creeper.class)
public interface CreeperAccessor {
    @Accessor("swell")
    void weeping$setSwell(int swell);
}
