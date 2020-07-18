package me.geek.tom.teleportals.blocks.teleporter;

import me.geek.tom.teleportals.blocks.ModBlocks;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.stream.Collectors;

public class TeleporterBlockEntity extends BlockEntity implements Tickable, BlockEntityClientSerializable {
    private Box teleportBox;
    private BlockPos target;

    public TeleporterBlockEntity() {
        super(ModBlocks.TELEPORTER_TILE);
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
        if (this.teleportBox == null) computeBox();
        if (this.target == null) return;

        for (Entity e : this.findEntitiesToTeleport()) {
            // Only teleport players on the client, in an attempt to make first person teleportation
            // be smoother.
            if (this.world.isClient && !(e instanceof PlayerEntity)) continue;
            e.teleport(target.getX(), target.getY(), target.getZ());
        }
    }

    /**
     * Initialises the box when the tile entity is created.
     */
    private void computeBox() {
        this.teleportBox = new Box(this.pos);
        this.teleportBox = this.teleportBox.stretch(0, 1, 0);
        this.teleportBox = this.teleportBox.expand(0d, 0d, -0.9d);
    }

    /**
     * @return A list of entities that are within the portal area above the block.
     */
    private List<Entity> findEntitiesToTeleport() {
        // bc of naming, the null is ambiguous, and therefor must be cast.
        List<Entity> teleportable = this.world.getEntities((Entity) null, this.teleportBox, entity -> true);
        return teleportable.stream()
                .filter(entity -> this.teleportBox.contains(entity.getPos()))
                .collect(Collectors.toList());
    }

    public Vec3d getTargetPos() {
        return new Vec3d(target.getX(), target.getY(), target.getZ());
    }

    public boolean hasTarget() {
        return target != null;
    }
}
