package makamys.satchels.inventory;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

public class InventorySimpleNotifying extends SimpleInventory {

    public Consumer<ItemStack> callback;

    public InventorySimpleNotifying(int size, String name, Consumer<ItemStack> callback) {
        super(size, name);
        this.callback = callback;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        boolean changed = !ItemStack.areItemStacksEqual(getStackInSlot(slot), stack);
        super.setInventorySlotContents(slot, stack);
        if (changed && callback != null) {
            callback.accept(stack);
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack result = super.decrStackSize(index, count);
        if (!result.isEmpty() && callback != null) {
            callback.accept(ItemStack.EMPTY);
        }
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack result = super.removeStackFromSlot(index);
        if (!result.isEmpty() && callback != null) {
            callback.accept(ItemStack.EMPTY);
        }
        return result;
    }

}
