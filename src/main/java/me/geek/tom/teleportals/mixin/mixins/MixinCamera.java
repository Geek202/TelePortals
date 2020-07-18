package me.geek.tom.teleportals.mixin.mixins;

import me.geek.tom.teleportals.mixin.ducks.IPortalCamera;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("ConstantConditions")
@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public abstract class MixinCamera implements IPortalCamera {

    @Shadow private BlockView area;

    @Shadow private Entity focusedEntity;

    @Shadow private boolean thirdPerson;

    @Shadow private boolean inverseView;

    @Shadow protected abstract void setPos(double x, double y, double z);

    @Shadow private Vec3d pos;

    @Shadow @Final private Quaternion rotation;

    @Shadow private float pitch;

    @Shadow private float yaw;

    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Shadow protected abstract void moveBy(double x, double y, double z);

    @Shadow public abstract Vec3d getPos();

    @Override
    public Camera teleportals_cloneCamera() {
        Camera ret = new Camera();

        ret.update(area, focusedEntity, thirdPerson, inverseView, 0);
        ((IPortalCamera)ret).teleportals_setPosition(pos.x, pos.y, pos.z);
        ((IPortalCamera)ret).teleportals_setRotation(yaw, pitch);

        return ret;
    }

    @Override
    public void teleportals_setPosition(double x, double y, double z) {
        this.setPos(x, y, z);
    }

    @Override
    public void teleportals_setRotation(float yaw, float pitch) {
        setRotation(yaw, pitch);
    }

    @Override
    public void teleportals_callMoveBy(double x, double y, double z) {
        this.moveBy(x, y, z);
    }

    @Override
    public void teleportals_callMoveBy(Vec3d movement) {
        this.teleportals_callMoveBy(movement.x, movement.y, movement.z);
    }

    @Override
    public void teleportals_shiftToPosition(Vec3d target) {
        Vec3d offset = target.subtract(pos);
        this.teleportals_callMoveBy(offset.x, offset.y, offset.z);
    }
}
