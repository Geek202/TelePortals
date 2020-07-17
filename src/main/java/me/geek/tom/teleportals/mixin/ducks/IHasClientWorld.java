package me.geek.tom.teleportals.mixin.ducks;

import net.minecraft.client.world.ClientWorld;

/**
 * A duck used to get the world from the WorldRenderer.
 */
public interface IHasClientWorld {
    ClientWorld getWorld();
}
