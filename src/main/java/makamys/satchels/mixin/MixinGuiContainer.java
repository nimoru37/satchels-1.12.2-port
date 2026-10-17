package makamys.satchels.mixin;

import makamys.satchels.gui.GuiTabButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer {

    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void satchels$drawInactiveTabsBeforeContainer(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiTabButton.drawInactiveTabs(Minecraft.getMinecraft(),
            ((MixinGuiScreenAccessor) (Object) this).satchels$getButtonList());
    }
}
