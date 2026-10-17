package makamys.satchels.inventory;

import makamys.satchels.ConfigSatchels;
import net.minecraft.item.ItemStack;

/** Inventory backing satchel / pouch contents; honours the item blacklist from the config */
public class StorageInventory extends SimpleInventory {

    public StorageInventory(int size, String name) {
        super(size, name);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return ConfigSatchels.isAllowed(stack);
    }

}
