package makamys.satchels.inventory;

import net.minecraft.inventory.Slot;
import makamys.satchels.item.ItemPouchUpgrade;

/** marks the final pouch-upgrade slot so its requirement can be shown as a tooltip */
public class SlotSatchelRequired extends SlotCustom {

    public SlotSatchelRequired(Slot original) {
        super(original.inventory, original.getSlotIndex(), original.xPos, original.yPos);
        this.slotNumber = original.slotNumber;
        this.setBackground(ItemPouchUpgrade.EMPTY_SLOT_SPRITE);
    }
}
