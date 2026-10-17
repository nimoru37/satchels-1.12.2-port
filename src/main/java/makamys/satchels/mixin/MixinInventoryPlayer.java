package makamys.satchels.mixin;

import makamys.satchels.EntityPropertiesSatchels;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryPlayer.class)
public class MixinInventoryPlayer {

    @Unique
    private boolean satchels$result;

    @Inject(method = "addItemStackToInventory(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"))
    private void satchels$preAdd(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(((InventoryPlayer) (Object) this).player);
        satchels$result = props != null && props.preAddItemStackToInventory(stack);
    }

    @Inject(method = "addItemStackToInventory(Lnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
    private void satchels$postAdd(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(((InventoryPlayer) (Object) this).player);
        if (props != null) {
            satchels$result |= props.postAddItemStackToInventory(stack);
        }
        cir.setReturnValue(cir.getReturnValue() | satchels$result);
    }

    @Inject(method = "clearMatchingItems(Lnet/minecraft/item/Item;IILnet/minecraft/nbt/NBTTagCompound;)I",
        at = @At("RETURN"), cancellable = true)
    private void satchels$clearMatchingItems(Item item, int metadata, int removeCount, NBTTagCompound itemNBT,
            CallbackInfoReturnable<Integer> cir) {
        EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(((InventoryPlayer) (Object) this).player);
        if (props != null) {
            cir.setReturnValue(cir.getReturnValue()
                + props.clearMatchingItems(item, metadata, removeCount, itemNBT, cir.getReturnValue()));
        }
    }

}
