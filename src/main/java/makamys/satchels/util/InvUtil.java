package makamys.satchels.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/** replacement for CodeChickenLib's InventoryUtils.insertItem and MCLib's InventoryUtils2.insertItem */
public class InvUtil {

    public static final int PASS_ALL = -1;
    public static final int PASS_MERGE = 0;
    public static final int PASS_EMPTY = 1;

    public static ItemStack insertItem(IInventory inv, ItemStack stack, boolean simulate) {
        return insertItem(inv, null, stack, simulate, PASS_ALL);
    }

    public static ItemStack insertItem(IInventory inv, int[] slots, ItemStack stack, boolean simulate, int pass) {
        ItemStack remaining = stack.copy();
        if (remaining.isEmpty()) {
            return remaining;
        }
        int count = slots != null ? slots.length : inv.getSizeInventory();

        if (pass == PASS_ALL || pass == PASS_MERGE) {
            for (int n = 0; n < count && !remaining.isEmpty(); n++) {
                int slot = slots != null ? slots[n] : n;
                ItemStack existing = inv.getStackInSlot(slot);
                if (existing.isEmpty() || !existing.isStackable() || !canMerge(existing, remaining)
                        || !inv.isItemValidForSlot(slot, remaining)) {
                    continue;
                }
                int limit = Math.min(inv.getInventoryStackLimit(), existing.getMaxStackSize());
                int space = limit - existing.getCount();
                if (space > 0) {
                    int moved = Math.min(space, remaining.getCount());
                    if (!simulate) {
                        ItemStack merged = existing.copy();
                        merged.grow(moved);
                        inv.setInventorySlotContents(slot, merged);
                    }
                    remaining.shrink(moved);
                }
            }
        }

        if (pass == PASS_ALL || pass == PASS_EMPTY) {
            for (int n = 0; n < count && !remaining.isEmpty(); n++) {
                int slot = slots != null ? slots[n] : n;
                if (!inv.getStackInSlot(slot).isEmpty() || !inv.isItemValidForSlot(slot, remaining)) {
                    continue;
                }
                int moved = Math.min(Math.min(inv.getInventoryStackLimit(), remaining.getMaxStackSize()), remaining.getCount());
                if (moved <= 0) {
                    continue;
                }
                if (!simulate) {
                    inv.setInventorySlotContents(slot, remaining.splitStack(moved));
                } else {
                    remaining.shrink(moved);
                }
            }
        }
        return remaining;
    }

    private static boolean canMerge(ItemStack a, ItemStack b) {
        return ItemStack.areItemsEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b);
    }
}
