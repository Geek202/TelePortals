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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

public class TeleporterRenderer extends BlockEntityRenderer<TeleporterBlockEntity> {

    public TeleporterRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TeleporterBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        double offset = Math.sin((entity.getWorld().getTime() + tickDelta) / 8.0) / 4.0;
        matrices.translate(0.5, 1, 0.5);

        if (!TelePortalsClient.isCurrentlyRenderingPortals) {
            // This was test rendering code. Commented out.
//            matrices.push();
//            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((entity.getWorld().getTime() + tickDelta) * 4));
//            matrices.translate(0, 0.75f, 0);
//            matrices.push();
//            matrices.scale(Math.abs((float) offset * 8.0f), Math.abs((float) offset * 8.0f), Math.abs((float) offset * 8.0f));
//
//            Item itm;
//            switch ((int) (((entity.getWorld().getTime() + tickDelta) / 8.0) % 16)) {
//                case 0:
//                    itm = Items.WHITE_STAINED_GLASS;
//                    break;
//                case 1:
//                    itm = Items.ORANGE_STAINED_GLASS;
//                    break;
//                case 2:
//                    itm = Items.MAGENTA_STAINED_GLASS;
//                    break;
//                case 3:
//                    itm = Items.LIGHT_BLUE_STAINED_GLASS;
//                    break;
//                case 4:
//                    itm = Items.YELLOW_STAINED_GLASS;
//                    break;
//                case 5:
//                    itm = Items.LIME_STAINED_GLASS;
//                    break;
//                case 6:
//                    itm = Items.PINK_STAINED_GLASS;
//                    break;
//                case 7:
//                    itm = Items.GRAY_STAINED_GLASS;
//                    break;
//                case 8:
//                    itm = Items.LIGHT_GRAY_STAINED_GLASS;
//                    break;
//                case 9:
//                    itm = Items.CYAN_STAINED_GLASS;
//                    break;
//                case 10:
//                    itm = Items.PURPLE_STAINED_GLASS;
//                    break;
//                case 11:
//                    itm = Items.BLUE_STAINED_GLASS;
//                    break;
//                case 12:
//                    itm = Items.BROWN_STAINED_GLASS;
//                    break;
//                case 13:
//                    itm = Items.GREEN_STAINED_GLASS;
//                    break;
//                case 14:
//                    itm = Items.RED_STAINED_GLASS;
//                    break;
//                case 15:
//                    itm = Items.BLACK_STAINED_GLASS;
//                    break;
//                default:
//                    itm = Items.GLASS;
//                    break;
//            }
//
//            ItemStack stack = new ItemStack(itm);
//
//            int itemLight = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up());
//            MinecraftClient.getInstance().getItemRenderer()
//                    .renderItem(stack, ModelTransformation.Mode.GROUND, itemLight, overlay, matrices, vertexConsumers);
//
//            matrices.pop();
//            matrices.pop();
//
//            drawPiece(matrices, vertexConsumers.getBuffer(
//                    RenderLayer.getBeaconBeam(new Identifier("textures/entity/beacon_beam.png"), false)));
        } else {
            // We can cast here because we are sure our rendering code passed a VertexConsumerProvider.Immediate
            // This draws a hole in the stencil buffer for this portal. The hole will then be filled immediately after
            // by a render pass of the world.
            TelePortalsClient.RENDERER.drawHoleInStencil(matrices, (VertexConsumerProvider.Immediate) vertexConsumers);
        }

        matrices.pop();
    }

//    private static void drawPiece(MatrixStack stack, VertexConsumer vertexConsumer) {
//        MatrixStack.Entry entry = stack.peek();
//        Matrix4f model = entry.getModel();
//        Matrix3f normal = entry.getNormal();
//
//        drawTexture(model, normal, vertexConsumer,
//                1.0f, 1.0f, 1.0f, 1.0f,
//                0, 2,
//                -0.1f, 0.1f, 0.1f, 0.1f,
//                0.0f, 1.0f, 0.0f, 1.0f);
//        drawTexture(model, normal, vertexConsumer,
//                1.0f, 1.0f, 1.0f, 1.0f,
//                0, 2,
//                0.1f, -0.1f, -0.1f, -0.1f,
//                0.0f, 1.0f, 0.0f, 1.0f);
//        drawTexture(model, normal, vertexConsumer,
//                1.0f, 1.0f, 1.0f, 1.0f,
//                0, 2,
//                -0.1f, -0.1f, -0.1f, 0.1f,
//                0.0f, 1.0f, 0.0f, 1.0f);
//        drawTexture(model, normal, vertexConsumer,
//                1.0f, 1.0f, 1.0f, 1.0f,
//                0, 2,
//                0.1f, 0.1f, 0.1f, -0.1f,
//                0.0f, 1.0f, 0.0f, 1.0f);
//        drawTexture(model, normal, vertexConsumer,
//                1.0f, 1.0f, 1.0f, 1.0f,
//                0, 2,
//                -0.1f, -0.1f, 0.1f, -0.1f,
//                0.0f, 1.0f, 0.0f, 1.0f);
//        drawTexture(model, normal, vertexConsumer,
//                1.0f, 1.0f, 1.0f, 1.0f,
//                0, 2,
//                0.1f, 0.1f, -0.1f, 0.1f,
//                0.0f, 1.0f, 0.0f, 1.0f);
//        drawTexture(model, normal, vertexConsumer,
//                1.0f, 1.0f, 1.0f, 1.0f,
//                0, 2,
//                0.1f, -0.1f, 0.1f, 0.1f,
//                0.0f, 1.0f, 0.0f, 1.0f);
//        drawTexture(model, normal, vertexConsumer,
//                1.0f, 1.0f, 1.0f, 1.0f,
//                0, 2,
//                -0.1f, 0.1f, -0.1f, -0.1f,
//                0.0f, 1.0f, 0.0f, 1.0f);
//    }

//    private static void drawTexture(Matrix4f model, Matrix3f normal, VertexConsumer vertexConsumer,
//                                    float r, float g, float b, float a,
//                                    int minY, int maxY, float xMin, float zMin, float xMax, float zMax,
//                                    float minU, float maxU, float minV, float maxV) {
//        createVertex(model, normal, vertexConsumer, r, g, b, a, maxY, xMin, zMin, maxU, minV);
//        createVertex(model, normal, vertexConsumer, r, g, b, a, minY, xMin, zMin, maxU, maxV);
//        createVertex(model, normal, vertexConsumer, r, g, b, a, minY, xMax, zMax, minU, maxV);
//        createVertex(model, normal, vertexConsumer, r, g, b, a, maxY, xMax, zMax, minU, minV);
//    }
//
//    private static void createVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer,
//                                     float r, float g, float b, float a,
//                                     int y, float x, float z,
//                                     float u, float v) {
//        vertexConsumer.vertex(matrix4f, x, (float)y, z).color(r, g, b, a).texture(u, v)
//                .overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).next();
//    }

    @Override
    public boolean rendersOutsideBoundingBox(TeleporterBlockEntity blockEntity) {
        return true;
    }
}
