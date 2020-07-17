package me.geek.tom.teleportals;

import me.geek.tom.teleportals.blocks.ModBlocks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TelePortals implements ModInitializer {

    public static final String MODID = "teleportals";
    public static ItemGroup ITEMGROUP = FabricItemGroupBuilder.build(
            new Identifier(MODID, "teleportals"),
            () -> new ItemStack(Items.GLASS_PANE)
    );

    @Override
    public void onInitialize() {
        // Register blocks and items.
        Registry.register(Registry.BLOCK, new Identifier(MODID, "teleporter"), ModBlocks.TELEPORTER_BLOCK);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MODID, "teleporter"), ModBlocks.TELEPORTER_TILE);

        Registry.register(Registry.ITEM, new Identifier(MODID, "teleporter"), new BlockItem(ModBlocks.TELEPORTER_BLOCK,
                new Item.Settings().group(ITEMGROUP)));
    }
}
