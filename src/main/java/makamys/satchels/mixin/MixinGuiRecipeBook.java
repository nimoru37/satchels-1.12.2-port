package makamys.satchels.mixin;

import makamys.satchels.EntityPropertiesSatchels;
import makamys.satchels.gui.GuiSatchelsInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiRecipeBook.class)
public abstract class MixinGuiRecipeBook {

    @ModifyConstant(method = "initVisuals", constant = @Constant(intValue = 86))
    private int satchels$moveRecipeBookRight(int vanillaOffset) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.currentScreen instanceof GuiSatchelsInventory) {
            EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(minecraft.player);
            if (props != null && props.hasLeftPouch()) {
                return vanillaOffset + 18;
            }
        }
        return vanillaOffset;
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void satchels$keepRecipeBookOpen(char typedChar, int keyCode, CallbackInfoReturnable<Boolean> cir) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (keyCode == Keyboard.KEY_ESCAPE
                && minecraft.currentScreen instanceof GuiSatchelsInventory
                && ((GuiRecipeBook) (Object) this).isVisible()) {
            cir.setReturnValue(false);
        }
    }
}
