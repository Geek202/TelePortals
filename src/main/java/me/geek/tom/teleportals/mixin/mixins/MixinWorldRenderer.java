package me.geek.tom.teleportals.mixin.mixins;

import me.geek.tom.teleportals.mixin.ducks.IHasClientWorld;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer implements IHasClientWorld {

    // These redirects prevent the clearing of the colour buffer every time the world is rendered
    // because we do that ourselves and it messes with the multiple renders of the world.

    @Redirect(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V"))
    private void onBackgroundRender(Camera camera, float tickDelta, ClientWorld world, int i, float f) {
        // Noop.
    }

    @Redirect(method = "render",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"))
    private void onRenderSystemClear(int mask, boolean getError) {
        // Noop.
    }

    @Accessor
    public abstract ClientWorld getWorld();

}
