package makamys.satchels.inventory;

import makamys.satchels.util.InvUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerChestGeneric extends Container {

    private final IInventory chestInventory;
    private final int rowSize;

    public ContainerChestGeneric(InventoryPlayer playerInventory, IInventory chestInventory) {
        this(playerInventory, chestInventory, 9);
    }

    public ContainerChestGeneric(InventoryPlayer playerInventory, IInventory chestInventory, int rowSize) {
        this.chestInventory = chestInventory;
        this.rowSize = rowSize = Math.min(rowSize, chestInventory.getSizeInventory());
        if (chestInventory.getSizeInventory() % rowSize != 0) {
            throw new IllegalArgumentException("Non-rectangular inventory. " + chestInventory.getSizeInventory()
                + " slots, " + rowSize + " columns.");
        }

        int numRows = (int) Math.ceil(chestInventory.getSizeInventory() / (float) rowSize);
        chestInventory.openInventory(playerInventory.player);
        int i = (numRows - 4) * 18;

        int slotStartXOff = ((9 - rowSize) * 18) / 2;

        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < rowSize; ++col) {
                if (col + row * rowSize < chestInventory.getSizeInventory()) {
                    this.addSlotToContainer(constructSlot(chestInventory, col + row * rowSize,
                        slotStartXOff + 8 + col * 18, 18 + row * 18));
                }
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + i));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 161 + i));
        }
    }

    protected Slot constructSlot(IInventory inventory, int slotIndex, int displayX, int displayY) {
        return new SlotCustom(inventory, slotIndex, displayX, displayY);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.chestInventory.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (index < this.chestInventory.getSizeInventory()) {
                if (!this.mergeItemStack(stack, this.chestInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertIntoChest(stack)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return result;
    }

    private boolean insertIntoChest(ItemStack stack) {
        int originalSize = stack.getCount();
        ItemStack left = InvUtil.insertItem(chestInventory, stack, false);
        stack.setCount(left.getCount());
        return left.getCount() != originalSize;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        this.chestInventory.closeInventory(player);
    }

    public IInventory getChestInventory() {
        return this.chestInventory;
    }

    public int getRowSize() {
        return this.rowSize;
    }
}
