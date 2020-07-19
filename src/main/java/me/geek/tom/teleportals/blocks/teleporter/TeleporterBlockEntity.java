package me.geek.tom.teleportals.blocks.teleporter;

import me.geek.tom.teleportals.blocks.ModBlocks;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TeleporterBlockEntity extends BlockEntity implements Tickable, BlockEntityClientSerializable {
    private BlockPos target;

    private Box trackingBox;
    private Map<Entity, Float> trackedEntities;

    public TeleporterBlockEntity() {
        super(ModBlocks.TELEPORTER_TILE);
        this.trackedEntities = new HashMap<>();
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);

        if (target != null)
            tag.put("TargetPosition", NbtHelper.fromBlockPos(target));

        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);

        if (tag.contains("TargetPosition", 10))
            this.target = NbtHelper.toBlockPos(tag.getCompound("TargetPosition"));
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        if (tag.contains("TargetPosition", 10))
            this.target = NbtHelper.toBlockPos(tag.getCompound("TargetPosition"));
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        if (target != null)
            tag.put("TargetPosition", NbtHelper.fromBlockPos(target));

        return tag;
    }

    @Override
    public void tick() {
        if (this.trackingBox == null) computeBox();
        if (this.target == null) return;

        updateTrackedEntitiesAndTeleport();
    }

    /**
     * Initialises the tracking box when the tile entity is first ticked.
     */
    private void computeBox() {
        this.trackingBox = new Box(this.pos);
        this.trackingBox = this.trackingBox.stretch(0, 1, 0);
    }

    /**
     * @return A list of entities that are within the portal area above the block.
     */
    private List<Entity> findEntitiesToTeleport() {
        // bc of naming, the null is ambiguous, and therefor must be cast.
        List<Entity> teleportable = this.world.getEntities((Entity) null, this.trackingBox, entity -> true);
        return teleportable.stream()
                .filter(entity -> this.trackingBox.contains(entity.getPos()))
                .collect(Collectors.toList());
    }

    private void updateTrackedEntitiesAndTeleport() {
        List<Entity> nearbyEntities = findEntitiesToTeleport();
        for (Entity entity : this.trackedEntities.keySet()) {
            if (!nearbyEntities.contains(entity)) {
                this.trackedEntities.remove(entity);
            }
        }

        for (Entity entity : nearbyEntities) {
            if (this.trackedEntities.containsKey(entity)) {
                float prevDotProd = this.trackedEntities.get(entity);
                Vec3d toEntityVector = this.getPositionVector().subtract(entity.getPos());
                Vec3d toEntityNormalisedVector = toEntityVector.normalize();
                float currentDotProd = (float) this.getForwardVector().dotProduct(toEntityNormalisedVector);
                if ((prevDotProd < 0 && currentDotProd > 0)
                        || (prevDotProd > 0 && currentDotProd < 0)) {
                    this.teleportEntity(entity, toEntityVector);
                    this.trackedEntities.remove(entity);
                } else {
                    this.trackedEntities.put(entity, currentDotProd);
                }
            } else {
                Vec3d toEntityVector = this.getPositionVector().subtract(entity.getPos());
                Vec3d toEntityNormalisedVector = toEntityVector.normalize();
                float currentDotProd = (float) this.getForwardVector().dotProduct(toEntityNormalisedVector);
                this.trackedEntities.put(entity, currentDotProd);
            }
        }
    }

    private void teleportEntity(Entity entity, Vec3d toEntityVector) {
        Vec3d target = this.getTargetPos().add(toEntityVector);
        entity.teleport(target.x, target.y, target.z);
    }

    private Vec3d getForwardVector() {
        return new Vec3d(0d, 0d, 1d).normalize();
    }

    public Vec3d getTargetPos() {
        return new Vec3d(target.getX(), target.getY(), target.getZ());
    }

    public Vec3d getPositionVector() {
        return new Vec3d(this.pos.getX() + 0.5d, this.pos.getY(), this.pos.getZ() + 0.5d);
    }

    public boolean hasTarget() {
        return target != null;
    }
}
