package makamys.satchels.inventory;

import makamys.satchels.EntityPropertiesSatchels;
import makamys.satchels.item.ItemPouch;
import makamys.satchels.item.ItemSatchel;
import makamys.satchels.util.InvUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEquipment extends ContainerPlayerExtended {

    Slot satchelSlot;
    Slot leftPouchSlot;
    Slot rightPouchSlot;

    public ContainerEquipment(InventoryPlayer playerInventory, boolean localWorld, EntityPlayer player) {
        super(playerInventory, localWorld, player);

        // disable crafting slots
        for (int i = 0; i < 5; i++) {
            inventorySlots.get(i).xPos = -1000;
        }
        // the equipment texture has no room for the off hand slot
        inventorySlots.get(SLOT_OFFHAND).xPos = -1000;

        EntityPropertiesSatchels satchelProps = EntityPropertiesSatchels.fromPlayer(player);

        addSlotToContainer(satchelSlot = new SlotCustom(satchelProps.equipment, 0, 115, 22).setBackground(ItemSatchel.EMPTY_SLOT_SPRITE));
        addSlotToContainer(leftPouchSlot = new SlotCustom(satchelProps.equipment, 1, 101, 58).setBackground(ItemPouch.EMPTY_SLOT_SPRITE));
        addSlotToContainer(rightPouchSlot = new SlotCustom(satchelProps.equipment, 2, 131, 58).setBackground(ItemPouch.EMPTY_SLOT_SPRITE));
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            if (index >= VANILLA_SLOT_COUNT && index < VANILLA_SLOT_COUNT + 3) {
                ItemStack stack = slot.getStack();
                ItemStack originalStack = stack.copy();
                ItemStack movingStack = stack.copy();
                if (!mergeItemStack(movingStack, 9, 45, false)
                        || movingStack.getCount() == originalStack.getCount()) {
                    return ItemStack.EMPTY;
                }

                slot.putStack(movingStack);
                slot.onTake(player, movingStack);
                return originalStack;
            }

            boolean inserted = false;

            ItemStack stack = slot.getStack();

            if (slot.slotNumber >= 9 && slot.slotNumber < 45) {
                EntityPropertiesSatchels satchelProps = EntityPropertiesSatchels.fromPlayer(player);

                int originalSize = stack.getCount();
                ItemStack left = InvUtil.insertItem(satchelProps.equipment, stack, false);
                if (left.getCount() != originalSize) {
                    stack.setCount(left.getCount());
                    inserted = true;
                }
            }

            if (inserted) {
                if (stack.isEmpty()) {
                    slot.putStack(ItemStack.EMPTY);
                } else {
                    slot.onSlotChanged();
                }

                slot.onTake(player, stack);
                return ItemStack.EMPTY;
            } else {
                return super.transferStackInSlot(player, index);
            }
        }

        return ItemStack.EMPTY;
    }
}
