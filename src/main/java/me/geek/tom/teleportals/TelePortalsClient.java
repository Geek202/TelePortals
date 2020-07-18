package me.geek.tom.teleportals;

import me.geek.tom.teleportals.blocks.ModBlocks;
import me.geek.tom.teleportals.blocks.teleporter.client.TeleporterRenderer;
import me.geek.tom.teleportals.render.PortalRendererManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class TelePortalsClient implements ClientModInitializer {

    public static PortalRendererManager RENDERER = new PortalRendererManager();

    // Global flag used to prevent portal in portal and normal TESR rendering
    // of the teleporter.
    public static boolean isCurrentlyRenderingPortals = false;
    // Debug counter used to track how many portals are rendered each frame.
    public static int portalsRenderedLastFrame = 0;

    @Override
    public void onInitializeClient() {
        // Register TESR.
        BlockEntityRendererRegistry.INSTANCE.register(ModBlocks.TELEPORTER_TILE, TeleporterRenderer::new);
    }
}
