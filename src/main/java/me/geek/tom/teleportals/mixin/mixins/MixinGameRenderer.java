package me.geek.tom.teleportals.mixin.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import me.geek.tom.teleportals.TelePortalsClient;
import me.geek.tom.teleportals.mixin.ducks.IHasClientWorld;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Shadow @Final private BufferBuilderStorage buffers;

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V"))
    private void preRenderWorld(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        // Setup and clear the stencil buffer.
        TelePortalsClient.RENDERER.initializeRendering();
    }

    @Redirect(method = "renderWorld",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V"))
    private void beforeRenderWorld(WorldRenderer worldRenderer, MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f) {
        // Clear the colour buffer here.
        // This replaces the WorldRenderer's call to RenderSystem.clear, which are nooped in MixinWorldRenderer.
        BackgroundRenderer.render(camera, tickDelta, ((IHasClientWorld) worldRenderer).getWorld(),
                client.options.viewDistance, gameRenderer.getSkyDarkness(tickDelta));
        RenderSystem.clear(16640, MinecraftClient.IS_SYSTEM_MAC);

        // Draw the portals onto the stencil buffer and then render the worlds behind.
        // We need to do this before the world is loaded, in order to actually cut a hole in it.
        TelePortalsClient.RENDERER.renderAllPortals(matrices, buffers, camera, tickDelta,
                client, limitTime, renderBlockOutline, (GameRenderer) (Object) this, worldRenderer,
                lightmapTextureManager, matrix4f);

        // Render the actual world. This is the call that we have redirected.
        TelePortalsClient.RENDERER.renderRealWorld(worldRenderer, matrices, tickDelta, limitTime,
                renderBlockOutline, camera, gameRenderer, lightmapTextureManager, matrix4f);

        // Cleanup by disabling the stencil test. This prevents the cutouts from making holes
        // in the menus and overlays.
        TelePortalsClient.RENDERER.completeRendering();
    }

//    @Inject(method = "render",
//            at = @At("RETURN"))
//    private void afterRenderComplete(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
//
//    }

}
