package me.geek.tom.teleportals.mixin.ducks;

import net.minecraft.client.render.Camera;

/**
 * Duck to allow cloning of Camera objects.
 */
public interface IPortalCamera {
    Camera teleportals_cloneCamera();
    void teleportals_setPosition(double x, double y, double z);
    void teleportals_setRotation(float yaw, float pitch);
    void teleportals_callMoveBy(double x, double y, double z);
}
