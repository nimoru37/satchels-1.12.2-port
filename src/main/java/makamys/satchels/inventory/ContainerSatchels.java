package makamys.satchels.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import makamys.satchels.EntityPropertiesSatchels;
import makamys.satchels.SatchelsUtils;
import makamys.satchels.util.InvUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSatchels extends ContainerPlayerExtended {
    public static final int PLAYER_X = 25;
    public static final int PLAYER_Y = 7;
    public static final int PLAYER_W = 54;
    public static final int PLAYER_H = 72;

    public List<Slot> leftPouchSlots = new ArrayList<Slot>();
    public List<Slot> rightPouchSlots = new ArrayList<Slot>();
    public List<Slot> satchelSlots = new ArrayList<Slot>();
    public boolean dirty;
    private final EntityPlayer player;
    private final Map<Slot, Pair<Integer, Integer>> originalSlotPositions = new HashMap<Slot, Pair<Integer, Integer>>();
    private boolean shiftArmorSlots;
    private boolean enableExtra;

    public ContainerSatchels(EntityPlayer player) {
        super(player.inventory, !player.world.isRemote, player);
        this.player = player;
        this.redoSlots(false);
    }

    public boolean isExtraEnabled() {
        return this.enableExtra;
    }

    public void redoSlots(boolean enableExtra) {
        this.enableExtra = enableExtra;
        this.redoSlots();
    }

    public void redoSlots() {
        this.removeAllExtraSlots();
        EntityPropertiesSatchels satchelProps = this.getSatchelProps();
        if (this.enableExtra && satchelProps != null) {
            SlotCustom slot;
            int row;
            ArrayList<Object> moddedSlots = new ArrayList<Object>();
            while (this.inventorySlots.size() > 46) {
                int last = this.inventorySlots.size() - 1;
                moddedSlots.add(0, this.inventorySlots.remove(last));
                this.inventoryItemStacks.remove(last);
            }
            int bottomY = 120;
            for (row = 0; row < 8; ++row) {
                slot = new SlotCustom(satchelProps.leftPouch, row, -10, bottomY - row * 18);
                this.leftPouchSlots.add(slot);
                this.addSlotToContainer(slot);
                this.setEnabled(this.leftPouchSlots, row, row < satchelProps.getUsableLeftPouchSlotCount());
            }
            for (row = 0; row < 8; ++row) {
                slot = new SlotCustom(satchelProps.rightPouch, row, 170, bottomY - row * 18);
                this.rightPouchSlots.add(slot);
                this.addSlotToContainer(slot);
                this.setEnabled(this.rightPouchSlots, row, row < satchelProps.getUsableRightPouchSlotCount());
            }
            StorageInventory satchelInv = satchelProps.satchel;
            for (int i = 0; i < 9; ++i) {
                Slot slot2 = new SlotCustom(satchelInv, i, 8 + i * 18, 66);
                this.satchelSlots.add(slot2);
                this.addSlotToContainer(slot2);
                this.setEnabled(this.satchelSlots, i, satchelProps.hasSatchel());
            }
            for (Object slot2 : moddedSlots) {
                this.inventorySlots.add((Slot) slot2);
                this.inventoryItemStacks.add(ItemStack.EMPTY);
            }
        }
        this.fixSlotNumbers();
        this.shiftArmorSlots = this.leftPouchSlots.stream().anyMatch(s -> SatchelsUtils.isPointInRange(s.xPos, 7, 79));
        boolean hasSatchel = satchelProps != null && satchelProps.hasSatchel();
        for (Slot slot : this.inventorySlots) {
            if (slot instanceof SlotDisabled) continue;
            Pair<Integer, Integer> originalPosition = this.originalSlotPositions.get(slot);
            if (originalPosition == null) {
                originalPosition = Pair.of(slot.xPos, slot.yPos);
                this.originalSlotPositions.put(slot, originalPosition);
            }
            boolean pushDown = slot.slotNumber >= 9 && slot.slotNumber != 45 && hasSatchel;
            slot.xPos = originalPosition.getLeft() + 16;
            slot.yPos = originalPosition.getRight() + (pushDown ? 18 : 0);
            if (slot.slotNumber < 5) {
                slot.xPos -= 4;
            }
            if (slot.slotNumber < 5 || slot.slotNumber >= 9) continue;
            slot.xPos += this.getArmorXOffset();
        }
    }

    public void setEnabled(List<Slot> list, int index, boolean enabled) {
        Slot slot = list.get(index);
        Slot newSlot = null;
        if (!(slot instanceof SlotDisabled) && !enabled) {
            if (slot.getHasStack()) {
                if (!this.player.world.isRemote) {
                    this.player.dropItem(slot.getStack(), false);
                }
                slot.putStack(ItemStack.EMPTY);
            }
            newSlot = new SlotDisabled(slot);
        } else if (slot instanceof SlotDisabled && enabled) {
            newSlot = ((SlotDisabled) slot).original;
        }
        if (newSlot != null) {
            list.set(index, newSlot);
            this.inventorySlots.set(slot.slotNumber, newSlot);
        }
    }

    private void removeAllExtraSlots() {
        HashSet<Slot> extraSlots = new HashSet<Slot>();
        extraSlots.addAll(this.leftPouchSlots);
        extraSlots.addAll(this.rightPouchSlots);
        extraSlots.addAll(this.satchelSlots);
        for (int i = this.inventorySlots.size() - 1; i >= 0; --i) {
            Slot slot = this.inventorySlots.get(i);
            if (!extraSlots.contains(slot)) continue;
            this.inventorySlots.remove(i);
            this.inventoryItemStacks.remove(i);
            this.originalSlotPositions.remove(slot);
        }
        this.leftPouchSlots.clear();
        this.rightPouchSlots.clear();
        this.satchelSlots.clear();
    }

    private void fixSlotNumbers() {
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            this.inventorySlots.get(i).slotNumber = i;
        }
    }

    public List<Slot> getEnabledLeftPouchSlots() {
        return this.leftPouchSlots.subList(0,
            Math.min(this.leftPouchSlots.size(), this.getSatchelProps().getUsableLeftPouchSlotCount()));
    }

    public List<Slot> getEnabledRightPouchSlots() {
        return this.rightPouchSlots.subList(0,
            Math.min(this.rightPouchSlots.size(), this.getSatchelProps().getUsableRightPouchSlotCount()));
    }

    public int getArmorXOffset() {
        return this.shiftArmorSlots ? 2 : 0;
    }

    @Override
    public List<Slot> getExtraSlots() {
        ArrayList<Slot> result = new ArrayList<Slot>();
        for (List list : Arrays.asList(this.satchelSlots, this.leftPouchSlots, this.rightPouchSlots)) {
            for (Object obj : list) {
                Slot s = (Slot) obj;
                if (s instanceof SlotDisabled) continue;
                result.add(s);
            }
        }
        return result;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot storageSlot;
        if (index >= 46 && index < this.inventorySlots.size() && this.isStorageSlot(storageSlot = this.inventorySlots.get(index)) && storageSlot.getHasStack()) {
            ItemStack stack = storageSlot.getStack();
            ItemStack originalStack = stack.copy();
            if (!this.mergeItemStack(stack, 36, 45, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                storageSlot.putStack(ItemStack.EMPTY);
            } else {
                storageSlot.onSlotChanged();
            }
            storageSlot.onTake(player, stack);
            return originalStack;
        }
        if (!this.enableExtra || index < 9 || index >= 45) {
            return super.transferStackInSlot(player, index);
        }
        Slot slot = this.inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        EntityPropertiesSatchels satchelProps = this.getSatchelProps();
        if (satchelProps == null) {
            return super.transferStackInSlot(player, index);
        }
        ItemStack stack = slot.getStack();
        ItemStack originalStack = stack.copy();
        ItemStack left = InvUtil.insertItem(satchelProps.aggregate, satchelProps.getEnabledSlots(), stack, false, -1);
        if (left.getCount() == stack.getCount()) {
            return super.transferStackInSlot(player, index);
        }
        stack.setCount(left.getCount());
        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
            slot.onTake(player, stack);
        } else {
            ItemStack vanillaResult = super.transferStackInSlot(player, index);
            if (vanillaResult.isEmpty()) {
                slot.onSlotChanged();
            }
        }
        return originalStack;
    }

    private boolean isStorageSlot(Slot slot) {
        return this.satchelSlots.contains(slot) || this.leftPouchSlots.contains(slot) || this.rightPouchSlots.contains(slot);
    }

    public EntityPropertiesSatchels getSatchelProps() {
        return EntityPropertiesSatchels.fromPlayer(this.player);
    }
}