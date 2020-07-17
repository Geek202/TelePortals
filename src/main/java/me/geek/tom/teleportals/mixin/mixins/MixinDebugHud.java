package me.geek.tom.teleportals.mixin.mixins;

import me.geek.tom.teleportals.TelePortalsClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(DebugHud.class)
public abstract class MixinDebugHud {

    @Shadow protected abstract List<String> getLeftText();

    // Add debug info onto the F3 screen.
    @Redirect(method = "renderLeftText",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;getLeftText()Ljava/util/List;"))
    private List<String> modifyLeftText(DebugHud debugHud) {
        List<String> text = getLeftText();

        text.add("[Teleportals] Rendered portals: " + TelePortalsClient.portalsRenderedLastFrame);

        return text;
    }

}
