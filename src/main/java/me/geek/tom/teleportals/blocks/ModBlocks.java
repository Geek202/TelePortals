package me.geek.tom.teleportals.blocks;

import me.geek.tom.teleportals.blocks.teleporter.TeleporterBlock;
import me.geek.tom.teleportals.blocks.teleporter.TeleporterBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.sound.BlockSoundGroup;

public class ModBlocks {

    public static final TeleporterBlock TELEPORTER_BLOCK = new TeleporterBlock(
            AbstractBlock.Settings.of(Material.STONE, MaterialColor.GRAY)
            .sounds(BlockSoundGroup.ANCIENT_DEBRIS));

    public static final BlockEntityType<TeleporterBlockEntity> TELEPORTER_TILE =
            BlockEntityType.Builder.create(TeleporterBlockEntity::new, TELEPORTER_BLOCK)
            .build(null);

}
