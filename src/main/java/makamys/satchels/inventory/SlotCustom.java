package makamys.satchels.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotCustom extends Slot {

    public SlotCustom(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return inventory.isItemValidForSlot(this.getSlotIndex(), stack);
    }

    public SlotCustom setBackground(String spriteName) {
        super.setBackgroundName(spriteName);
        return this;
    }

}
