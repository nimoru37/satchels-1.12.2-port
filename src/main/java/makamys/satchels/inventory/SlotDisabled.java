package makamys.satchels.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotDisabled extends Slot {

    private static final IInventory EMPTY_INVENTORY = new SimpleInventory(1, "container.emptyInventory");

    public final Slot original;
    private final boolean copy;

    public SlotDisabled(Slot original) {
        this(original, false);
    }

    public SlotDisabled(Slot original, boolean copy) {
        super(!copy ? EMPTY_INVENTORY : original.inventory,
              !copy ? 0 : original.getSlotIndex(),
              !copy ? -1000 : original.xPos,
              !copy ? -1000 : original.yPos);
        this.original = original;
        this.copy = copy;
        if (original != null) {
            this.slotNumber = original.slotNumber;
        }
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return false;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return copy;
    }
}
