package me.geek.tom.teleportals.blocks.teleporter.client;

import me.geek.tom.teleportals.TelePortalsClient;
import me.geek.tom.teleportals.blocks.teleporter.TeleporterBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

import static me.geek.tom.teleportals.TelePortals.MODID;

public class TeleporterRenderer extends BlockEntityRenderer<TeleporterBlockEntity> {

    private static final Identifier PORTAL_NO_TARGET_TEXTURE = new Identifier("minecraft", "textures/block/nether_portal.png");

    public TeleporterRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TeleporterBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        double offset = Math.sin((entity.getWorld().getTime() + tickDelta) / 8.0) / 4.0;
        matrices.translate(0.5, 1, 0.5);

        if (!TelePortalsClient.isCurrentlyRenderingPortals && !entity.hasTarget()) {
            Matrix4f model = matrices.peek().getModel();
            Matrix3f normal = matrices.peek().getNormal();
            drawTexture(model, normal, vertexConsumers.getBuffer(RenderLayer.getEntityCutout(PORTAL_NO_TARGET_TEXTURE)),
                    1, 1, 1, 1,
                    0, 1,
                    0, 0.5f,
                    1, 0.5f,
                    0, 1,
                    0, 1);
        } else {
            // We can cast here because we are sure our rendering code passed a VertexConsumerProvider.Immediate
            // This draws a hole in the stencil buffer for this portal. The hole will then be filled immediately after
            // by a render pass of the world.
            TelePortalsClient.RENDERER.drawHoleInStencil(matrices, (VertexConsumerProvider.Immediate) vertexConsumers);
        }

        matrices.pop();
    }

    private static void drawTexture(Matrix4f model, Matrix3f normal, VertexConsumer vertexConsumer,
                                    float r, float g, float b, float a,
                                    int minY, int maxY, float xMin, float zMin, float xMax, float zMax,
                                    float minU, float maxU, float minV, float maxV) {
        createVertex(model, normal, vertexConsumer, r, g, b, a, maxY, xMin, zMin, maxU, minV);
        createVertex(model, normal, vertexConsumer, r, g, b, a, minY, xMin, zMin, maxU, maxV);
        createVertex(model, normal, vertexConsumer, r, g, b, a, minY, xMax, zMax, minU, maxV);
        createVertex(model, normal, vertexConsumer, r, g, b, a, maxY, xMax, zMax, minU, minV);
    }

    private static void createVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer,
                                     float r, float g, float b, float a,
                                     int y, float x, float z,
                                     float u, float v) {
        vertexConsumer.vertex(matrix4f, x, (float)y, z).color(r, g, b, a).texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).next();
    }

    @Override
    public boolean rendersOutsideBoundingBox(TeleporterBlockEntity blockEntity) {
        return true;
    }
}
