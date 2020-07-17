package me.geek.tom.teleportals.blocks.teleporter;

import me.geek.tom.teleportals.blocks.ModBlocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.stream.Collectors;

public class TeleporterBlockEntity extends BlockEntity implements Tickable {
    public TeleporterBlockEntity() {
        super(ModBlocks.TELEPORTER_TILE);
    }

    private Box teleportBox;

    @Override
    public void tick() {
        if (this.teleportBox == null) computeBox();

        for (Entity e : this.findEntitiesToTeleport()) {
            // Only teleport players on the client, in an attempt to make first person teleportation
            // be smoother.
            if (this.world.isClient && !(e instanceof PlayerEntity)) continue;
            Vec3d pos = e.getPos();
            pos = pos.add(0, 10, 0);
            e.teleport(pos.x, pos.y, pos.z);
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
}
