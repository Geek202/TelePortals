package me.geek.tom.teleportals.mixin.mixins;

import com.mojang.blaze3d.platform.FramebufferInfo;
import com.mojang.blaze3d.platform.GlStateManager;
import me.geek.tom.teleportals.mixin.ducks.IStenciledFramebuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.IntBuffer;

@Environment(EnvType.CLIENT)
@Mixin(Framebuffer.class)
public abstract class MixinFramebuffer implements IStenciledFramebuffer {
    @Shadow public abstract void initFbo(int width, int height, boolean getError);

    @Shadow public int textureWidth;
    @Shadow public int textureHeight;
    private boolean useStencil = false;

    @Redirect(method = "initFbo",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;texImage2D(IIIIIIIILjava/nio/IntBuffer;)V"))
    private void textImage2DRedirect(int target, int level, int internalFormat,
                                     int width, int height, int border,
                                     int format, int type, IntBuffer pixels) {
        if (internalFormat == 6402 && useStencil) {
            GlStateManager.texImage2D(
                    target, level, ARBFramebufferObject.GL_DEPTH24_STENCIL8,
                    width, height, border,
                    ARBFramebufferObject.GL_DEPTH_STENCIL, GL30.GL_UNSIGNED_INT_24_8, pixels
            );
        }
        else {
            GlStateManager.texImage2D(
                    target, level, internalFormat,
                    width, height, border,
                    format, type, pixels
            );
        }
    }

    @Redirect(method = "initFbo",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;framebufferTexture2D(IIIII)V"))
    private void framebufferTexture2DRedirect(int target, int attachment,
                                              int textureTarget, int texture, int level) {
        if (attachment == FramebufferInfo.DEPTH_ATTACHMENT && useStencil) {
            GlStateManager.framebufferTexture2D(
                    target,
                    GL30.GL_DEPTH_STENCIL_ATTACHMENT,
                    textureTarget,
                    texture,
                    level
            );
        }
        else {
            GlStateManager.framebufferTexture2D(target, attachment, textureTarget, texture, level);
        }
    }

    @Override
    public void teleportals_setStencilAndReload(boolean enabled) {
        if (enabled != useStencil) {
            useStencil = enabled;
            reloadFbo();
        }
    }

    private void reloadFbo() {
        initFbo(textureWidth, textureHeight, MinecraftClient.IS_SYSTEM_MAC);
        if (MinecraftClient.isFabulousGraphicsOrBetter())
            MinecraftClient.getInstance().worldRenderer.reload();
    }

}
