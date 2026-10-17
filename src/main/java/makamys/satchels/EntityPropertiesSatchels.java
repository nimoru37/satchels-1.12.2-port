package makamys.satchels;

import java.util.ArrayList;
import java.util.List;

import makamys.satchels.inventory.ContainerSatchels;
import makamys.satchels.inventory.InventoryAggregate;
import makamys.satchels.inventory.InventorySimpleNotifying;
import makamys.satchels.inventory.SimpleInventory;
import makamys.satchels.inventory.StorageInventory;
import makamys.satchels.item.ItemPouch;
import makamys.satchels.item.ItemSatchel;
import makamys.satchels.util.InvUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.SoundCategory;

public class EntityPropertiesSatchels {

    public static final int SATCHEL_MAX_SLOTS = 9;
    public static final int POUCH_MAX_SLOTS = 8;
    public static final int POUCH_INITIAL_SLOTS = 3;

    public static final int SLOT_SATCHEL = 0;
    public static final int SLOT_LEFT_POUCH = 1;
    public static final int SLOT_RIGHT_POUCH = 2;

    public final InventorySimpleNotifying equipment = new InventorySimpleNotifying(3, "container.equipment", this::onEquipmentChanged) {
        @Override
        public boolean isItemValidForSlot(int i, ItemStack stack) {
            return (i == SLOT_SATCHEL && stack.getItem() instanceof ItemSatchel)
                || ((i == SLOT_LEFT_POUCH || i == SLOT_RIGHT_POUCH) && stack.getItem() instanceof ItemPouch);
        }
    };

    public final StorageInventory satchel = new StorageInventory(SATCHEL_MAX_SLOTS, "container.satchel");
    public final StorageInventory leftPouch = new StorageInventory(POUCH_MAX_SLOTS, "container.leftPouch");
    public final StorageInventory rightPouch = new StorageInventory(POUCH_MAX_SLOTS, "container.rightPouch");
    public final InventoryAggregate aggregate = new InventoryAggregate(satchel, leftPouch, rightPouch);
    public final EntityPlayer player;

    public EntityPropertiesSatchels(EntityPlayer player) {
        this.player = player;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        writeIfNotEmpty(tag, "Equipment", equipment);
        writeIfNotEmpty(tag, "Satchel", satchel);
        writeIfNotEmpty(tag, "LeftPouch", leftPouch);
        writeIfNotEmpty(tag, "RightPouch", rightPouch);
        return tag;
    }

    private static void writeIfNotEmpty(NBTTagCompound tag, String key, SimpleInventory inv) {
        if (!inv.isEmpty()) {
            tag.setTag(key, inv.writeToNBT());
        }
    }

    public void readFromNBT(NBTTagCompound tag) {
        readOrClear(tag, "Equipment", equipment);
        readOrClear(tag, "Satchel", satchel);
        readOrClear(tag, "LeftPouch", leftPouch);
        readOrClear(tag, "RightPouch", rightPouch);
    }

    private static void readOrClear(NBTTagCompound tag, String key, SimpleInventory inv) {
        if (tag.hasKey(key)) {
            inv.readFromNBT(tag.getCompoundTag(key));
        } else {
            inv.clear();
        }
    }

    public void copyFrom(EntityPropertiesSatchels from) {
        equipment.copyFrom(from.equipment);
        satchel.copyFrom(from.satchel);
        leftPouch.copyFrom(from.leftPouch);
        rightPouch.copyFrom(from.rightPouch);
    }

    public void collectAllStacks(List<ItemStack> out) {
        for (SimpleInventory inv : new SimpleInventory[] { equipment, satchel, leftPouch, rightPouch }) {
            for (ItemStack stack : inv.items) {
                if (!stack.isEmpty()) {
                    out.add(stack.copy());
                }
            }
            inv.clear();
        }
    }

    public int clearMatchingItems(Item item, int metadata, int removeCount, NBTTagCompound itemNBT, int alreadyRemoved) {
        int removed = 0;
        int remaining = removeCount > 0 ? removeCount - alreadyRemoved : removeCount;

        for (SimpleInventory inv : new SimpleInventory[] { satchel, leftPouch, rightPouch, equipment }) {
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack.isEmpty() || (item != null && stack.getItem() != item)
                        || (metadata > -1 && stack.getMetadata() != metadata)
                        || (itemNBT != null && !NBTUtil.areNBTEquals(itemNBT, stack.getTagCompound(), true))) {
                    continue;
                }

                int count = remaining <= 0 ? stack.getCount() : Math.min(remaining, stack.getCount());
                removed += count;
                if (removeCount != 0) {
                    inv.decrStackSize(i, count);
                }
                if (remaining > 0) {
                    remaining -= count;
                    if (remaining == 0) {
                        return removed;
                    }
                }
            }
        }
        return removed;
    }

    private void dropStack(IInventory inv, int i) {
        ItemStack stack = inv.getStackInSlot(i);
        if (!stack.isEmpty()) {
            inv.setInventorySlotContents(i, ItemStack.EMPTY);
            if (player != null) {
                player.dropItem(stack, false, false);
            }
        }
    }

    public ItemStack equip(ItemStack stack) {
        return InvUtil.insertItem(equipment, stack, false);
    }

    private void onEquipmentChanged(ItemStack stack) {
        if (player != null && !stack.isEmpty() && !player.world.isRemote) {
            player.world.playSound(null, player.posX, player.posY, player.posZ,
                SatchelsItems.EQUIP_SOUND, SoundCategory.PLAYERS, 1f, 1f);
        }
        if (player != null && !player.world.isRemote) {
            updateInventories();
            Packets.syncEquipmentToPlayer(player);
            Packets.syncEquipmentToTracking(player);
        } else {
            updateInventories();
        }
    }

    public void updateInventories() {
        if (player == null) {
            return;
        }
        Container container = player.inventoryContainer;
        if (container instanceof ContainerSatchels) {
            ((ContainerSatchels) container).redoSlots();
        }
        if (!player.world.isRemote) {
            for (int i = 0; i < leftPouch.getSizeInventory(); i++) {
                if (i >= getLeftPouchSlotCount() || !ConfigSatchels.isAllowed(leftPouch.getStackInSlot(i))) {
                    dropStack(leftPouch, i);
                }
            }
            for (int i = 0; i < rightPouch.getSizeInventory(); i++) {
                if (i >= getRightPouchSlotCount() || !ConfigSatchels.isAllowed(rightPouch.getStackInSlot(i))) {
                    dropStack(rightPouch, i);
                }
            }
            for (int i = 0; i < satchel.getSizeInventory(); i++) {
                if (i >= getSatchelSlotCount() || !ConfigSatchels.isAllowed(satchel.getStackInSlot(i))) {
                    dropStack(satchel, i);
                }
            }
        }
    }

    public int getLeftPouchSlotCount() {
        return ItemPouch.getSlotCount(equipment.getStackInSlot(SLOT_LEFT_POUCH));
    }

    public int getRightPouchSlotCount() {
        return ItemPouch.getSlotCount(equipment.getStackInSlot(SLOT_RIGHT_POUCH));
    }

    public int getUsableLeftPouchSlotCount() {
        return Math.min(getLeftPouchSlotCount(), hasSatchel() ? POUCH_MAX_SLOTS : POUCH_MAX_SLOTS - 1);
    }

    public int getUsableRightPouchSlotCount() {
        return Math.min(getRightPouchSlotCount(), hasSatchel() ? POUCH_MAX_SLOTS : POUCH_MAX_SLOTS - 1);
    }

    public int getSatchelSlotCount() {
        return !equipment.getStackInSlot(SLOT_SATCHEL).isEmpty() ? SATCHEL_MAX_SLOTS : 0;
    }

    public boolean hasSatchel() {
        return getSatchelSlotCount() > 0;
    }

    public boolean hasLeftPouch() {
        return getUsableLeftPouchSlotCount() > 0;
    }

    public boolean hasRightPouch() {
        return getUsableRightPouchSlotCount() > 0;
    }

    public static EntityPropertiesSatchels fromPlayer(EntityPlayer player) {
        return player.getCapability(SatchelsCapability.CAPABILITY, null);
    }

    public boolean preAddItemStackToInventory(ItemStack stack) {
        return addItemStackToInventory(stack, InvUtil.PASS_MERGE);
    }

    public boolean postAddItemStackToInventory(ItemStack stack) {
        return addItemStackToInventory(stack, InvUtil.PASS_EMPTY);
    }

    private boolean addItemStackToInventory(ItemStack stack, int pass) {
        if (stack.isEmpty() || (player != null && player.isCreative())) {
            return false;
        }
        int originalSize = stack.getCount();
        ItemStack left = InvUtil.insertItem(aggregate, getEnabledSlots(), stack, false, pass);
        stack.setCount(left.getCount());
        return stack.getCount() != originalSize;
    }

    public int[] getEnabledSlots() {
        List<Integer> slots = new ArrayList<>();
        if (hasSatchel()) {
            for (int i = 0; i < satchel.getSizeInventory(); i++) {
                slots.add(aggregate.toGlobalIdx(satchel, i));
            }
        }
        for (int i = 0; i < getUsableLeftPouchSlotCount(); i++) {
            slots.add(aggregate.toGlobalIdx(leftPouch, i));
        }
        for (int i = 0; i < getUsableRightPouchSlotCount(); i++) {
            slots.add(aggregate.toGlobalIdx(rightPouch, i));
        }
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }
}
