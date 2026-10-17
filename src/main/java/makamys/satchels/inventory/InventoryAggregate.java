package makamys.satchels.inventory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class InventoryAggregate implements IInventory {

    private final List<IInventory> inventories;

    public InventoryAggregate(IInventory... inventories) {
        this.inventories = Arrays.stream(inventories).collect(Collectors.toList());
    }

    @Override
    public int getSizeInventory() {
        return inventories.stream().mapToInt(IInventory::getSizeInventory).sum();
    }

    @Override
    public boolean isEmpty() {
        return inventories.stream().allMatch(IInventory::isEmpty);
    }

    public Pair<IInventory, Integer> toLocalIdx(int globalIdx) {
        int invStartGlobal = 0;
        for (IInventory inv : inventories) {
            if (globalIdx >= invStartGlobal && globalIdx < invStartGlobal + inv.getSizeInventory()) {
                return Pair.of(inv, globalIdx - invStartGlobal);
            }
            invStartGlobal += inv.getSizeInventory();
        }
        return null;
    }

    public int toGlobalIdx(IInventory targetInv, int localIdx) {
        int invStartGlobal = 0;
        for (IInventory inv : inventories) {
            if (targetInv == inv) {
                return invStartGlobal + localIdx;
            }
            invStartGlobal += inv.getSizeInventory();
        }
        return -1;
    }

    @Override
    public ItemStack getStackInSlot(int globalIdx) {
        Pair<IInventory, Integer> local = toLocalIdx(globalIdx);
        return local != null ? local.getLeft().getStackInSlot(local.getRight()) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int globalIdx, int count) {
        Pair<IInventory, Integer> local = toLocalIdx(globalIdx);
        return local != null ? local.getLeft().decrStackSize(local.getRight(), count) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int globalIdx) {
        Pair<IInventory, Integer> local = toLocalIdx(globalIdx);
        return local != null ? local.getLeft().removeStackFromSlot(local.getRight()) : ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int globalIdx, ItemStack stack) {
        Pair<IInventory, Integer> local = toLocalIdx(globalIdx);
        if (local != null) {
            local.getLeft().setInventorySlotContents(local.getRight(), stack);
        }
    }

    @Override
    public String getName() {
        return "aggregate{" + inventories.stream().map(IInventory::getName).collect(Collectors.joining(",")) + "}";
    }

    @Override
    public boolean hasCustomName() {
        return inventories.stream().anyMatch(IInventory::hasCustomName);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    @Override
    public int getInventoryStackLimit() {
        return inventories.stream().mapToInt(IInventory::getInventoryStackLimit).min().orElse(64);
    }

    @Override
    public void markDirty() {
        inventories.forEach(IInventory::markDirty);
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return inventories.stream().allMatch(i -> i.isUsableByPlayer(player));
    }

    @Override
    public void openInventory(EntityPlayer player) {
        inventories.forEach(i -> i.openInventory(player));
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        inventories.forEach(i -> i.closeInventory(player));
    }

    @Override
    public boolean isItemValidForSlot(int globalIdx, ItemStack stack) {
        Pair<IInventory, Integer> local = toLocalIdx(globalIdx);
        return local != null && local.getLeft().isItemValidForSlot(local.getRight(), stack);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        inventories.forEach(IInventory::clear);
    }
}
