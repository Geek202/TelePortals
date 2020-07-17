package me.geek.tom.teleportals.mixin.ducks;

/**
 * Enabling the stencil buffer on the main Framebuffer.
 */
public interface IStenciledFramebuffer {
    void teleportals_setStencilAndReload(boolean enabled);
}
