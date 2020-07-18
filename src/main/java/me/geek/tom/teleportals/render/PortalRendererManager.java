package me.geek.tom.teleportals.render;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.geek.tom.teleportals.TelePortalsClient;
import me.geek.tom.teleportals.blocks.teleporter.TeleporterBlockEntity;
import me.geek.tom.teleportals.mixin.ducks.IPortalCamera;
import me.geek.tom.teleportals.mixin.ducks.IStenciledFramebuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;

@Environment(EnvType.CLIENT)
public class PortalRendererManager {

    public void initializeRendering() {
        // No portal in portal pls.
        if (TelePortalsClient.isCurrentlyRenderingPortals) return;

        // Clear the buffer ready to draw the frame.
        GL11.glClearStencil(0x01);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        // Enable depth and stencil testing.
        GlStateManager.enableDepthTest();
        GL11.glEnable(GL_STENCIL_TEST);

        // Enable the stencil on the FBO.
        ((IStenciledFramebuffer) MinecraftClient.getInstance().getFramebuffer())
                .teleportals_setStencilAndReload(true);
    }

    public void renderAllPortals(MatrixStack matrices, BufferBuilderStorage buffers,
                                 Camera camera, float tickDelta, MinecraftClient client,
                                 long limitTime, boolean renderBlockOutline,
                                 GameRenderer gameRenderer, WorldRenderer worldRenderer,
                                 LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f) {
        if (TelePortalsClient.isCurrentlyRenderingPortals) return;

        client.getProfiler().push("teleportals_rendering");
        Vec3d camPos = camera.getPos();
        double camX = camPos.getX();
        double camY = camPos.getY();
        double camZ = camPos.getZ();

        VertexConsumerProvider.Immediate immediate = buffers.getEntityVertexConsumers();

        TelePortalsClient.portalsRenderedLastFrame = 0;

        // State flag to render the portals differently in our special case.
        TelePortalsClient.isCurrentlyRenderingPortals = true;
        synchronized (worldRenderer.noCullingBlockEntities) {
            // Make a copy to prevent ConcurrentModificationExceptions.
            Set<BlockEntity> cloned = Sets.newConcurrentHashSet(worldRenderer.noCullingBlockEntities);
            Iterator<BlockEntity> noCullingBlockEntities = cloned.iterator();
            while (noCullingBlockEntities.hasNext()) {
                BlockEntity be = noCullingBlockEntities.next();
                if (!(be instanceof TeleporterBlockEntity)) continue;
                TeleporterBlockEntity te = (TeleporterBlockEntity) be;

                if (!te.hasTarget()) continue;

                BlockPos portalPos = te.getPos();
                Vec3d portalToPlayer = camPos.subtract(new Vec3d(portalPos.getX(), portalPos.getY(), portalPos.getZ()));

                Camera moddedCamera = ((IPortalCamera)camera).teleportals_cloneCamera();
                IPortalCamera portalCam = (IPortalCamera) moddedCamera;
                portalCam.teleportals_shiftToPosition(te.getTargetPos());
                portalCam.teleportals_callMoveBy(portalToPlayer);

                renderPortal(te, matrices, immediate,
                        camX, camY, camZ, tickDelta, limitTime, renderBlockOutline,
                        moddedCamera, gameRenderer, worldRenderer,
                        lightmapTextureManager, matrix4f, client.getProfiler());
            }
        }
        TelePortalsClient.isCurrentlyRenderingPortals = false;

        client.getProfiler().pop();
    }

    public void renderRealWorld(WorldRenderer worldRenderer, MatrixStack matrices, float tickDelta,
                                long limitTime, boolean renderBlockOutline,
                                Camera camera, GameRenderer gameRenderer,
                                LightmapTextureManager lightmapTextureManager,
                                Matrix4f matrix4f) {
        TelePortalsClient.RENDERER.setStencilMask(1);
        worldRenderer.render(matrices, tickDelta, limitTime, renderBlockOutline, camera,
                gameRenderer, lightmapTextureManager, matrix4f);
    }

    public void renderPortal(TeleporterBlockEntity te, MatrixStack matrices, VertexConsumerProvider.Immediate immediate,
                             double camX, double camY, double camZ, float tickDelta, long limitTime, boolean renderBlockOutline,
                             Camera camera, GameRenderer gameRenderer, WorldRenderer worldRenderer,
                             LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Profiler profiler) {
        BlockPos pos = te.getPos();

        profiler.push("render_to_stencil");

        matrices.push();
        matrices.translate((double) pos.getX() - camX, (double) pos.getY() - camY, (double) pos.getZ() - camZ);
        // Draw the portal to the stencil buffer. A special flag was set in the function above,
        // and so this will cut a hole in the buffer.
        BlockEntityRenderDispatcher.INSTANCE.render(te, tickDelta, matrices, immediate);
        matrices.pop();

        profiler.swap("draw_portal_world");

        renderPortalWorld(worldRenderer, matrices,
                tickDelta, limitTime, renderBlockOutline,
                camera, gameRenderer,
                lightmapTextureManager, matrix4f);

        profiler.pop();

        // Increment debug counter.
        TelePortalsClient.portalsRenderedLastFrame++;
    }

    public void renderPortalWorld(WorldRenderer renderer, MatrixStack matrices,
                                  float tickDelta, long limitTime, boolean renderBlockOutline,
                                  Camera camera, GameRenderer gameRenderer,
                                  LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f) {
        // Invert the effect of the stencil buffer for the other worlds.
        setStencilMask(0);
        renderer.render(matrices, tickDelta, limitTime, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, matrix4f);
        setStencilMask(1);
    }

    public void completeRendering() {
        // Clean up state, fixes issues with menus after loading a world.
        GL11.glDisable(GL_STENCIL_TEST);
    }

    public void drawHoleInStencil(MatrixStack stack, VertexConsumerProvider.Immediate vertexConsumerProvider) {
        // Init stencil drawing.
        GL11.glStencilFunc(GL_ALWAYS, 0, 0xFF);
        GL11.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        GL11.glStencilMask(0xFF);

        // Configure state.
        GlStateManager.disableBlend();
        GL20.glUseProgram(0);
        RenderSystem.enableDepthTest();

        // Don't modify the depth or colour buffers pls.
        GlStateManager.depthMask(false);
        GlStateManager.colorMask(false, false, false, false);

        GlStateManager.disableCull();
        GlStateManager.disableTexture();

        // Draw to the buffer.
        VertexConsumer builder = vertexConsumerProvider.getBuffer(RenderTypes.STENCIL_CUTOUT);

        Vec3d fog = Vec3d.ZERO; // fix

        drawQuad(vec -> makeVertex(builder, vec, fog),
                new Vec3d(.5, 0, 0), new Vec3d(-.5, 0, 0),
                new Vec3d(-.5, 2, 0), new Vec3d(.5, 2, 0));

        // Use the transformations of the MatrixStack
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(stack.peek().getModel());
        vertexConsumerProvider.draw();
        RenderSystem.popMatrix();

        // Restore state.
        GlStateManager.enableCull();
        GlStateManager.enableTexture();
        GlStateManager.enableBlend();

        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(true);

        // Don't edit the buffer until the next portal.
        GL11.glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
    }

    private void drawQuad(Consumer<Vec3d> builder, Vec3d a, Vec3d b, Vec3d c, Vec3d d) {
        // Tri 1
        builder.accept(b);
        builder.accept(c);
        builder.accept(d);

        // Tri 2
        builder.accept(d);
        builder.accept(a);
        builder.accept(b);
    }

    private static void makeVertex(VertexConsumer bufferBuilder, Vec3d pos, Vec3d fogColor) {
        bufferBuilder.vertex(pos.x, pos.y, pos.z)
                .color((float) fogColor.x, (float) fogColor.y, (float) fogColor.z, 1.0f)
                .next();
    }

    public void setStencilMask(int ref) {
        GL11.glStencilFunc(GL_EQUAL, ref, 0xFF);
        GL11.glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        GL11.glStencilMask(0xFF);
    }

    public static class RenderTypes extends RenderLayer {
        // Why is this not a thing in vanilla?
        private static final WriteMaskState NO_DEPTH_NO_COLOUR = new WriteMaskState(false, false);

        public static final RenderLayer STENCIL_CUTOUT = of("stencil_cutout",
                VertexFormats.POSITION_COLOR, GL_TRIANGLES, 256,
                RenderLayer.MultiPhaseParameters.builder()
                        .layering(NO_LAYERING)
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .texture(NO_TEXTURE)
                        .depthTest(ALWAYS_DEPTH_TEST)
                        .cull(DISABLE_CULLING)
                        .lightmap(DISABLE_LIGHTMAP)
                        .writeMaskState(NO_DEPTH_NO_COLOUR)
                        .build(false));

        // Dummy
        private RenderTypes(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
            super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
        }
    }

}
