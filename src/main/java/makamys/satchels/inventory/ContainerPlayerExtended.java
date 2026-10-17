package makamys.satchels.inventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import invtweaks.api.container.ChestContainer;
import invtweaks.api.container.ContainerSection;
import invtweaks.api.container.ContainerSectionCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;

@ChestContainer(rowSize = 1)
public abstract class ContainerPlayerExtended extends ContainerPlayer {

    public static final int SLOT_OFFHAND = 45;
    public static final int VANILLA_SLOT_COUNT = 46;

    public ContainerPlayerExtended(InventoryPlayer playerInventory, boolean localWorld, EntityPlayer player) {
        super(playerInventory, localWorld, player);
    }

    @ContainerSectionCallback
    public Map<ContainerSection, List<Slot>> getContainerSectionMap() {
        Map<ContainerSection, List<Slot>> slotRefs = new HashMap<>();

        slotRefs.put(ContainerSection.CRAFTING_OUT, inventorySlots.subList(0, 1));
        slotRefs.put(ContainerSection.CRAFTING_IN, inventorySlots.subList(1, 5));
        slotRefs.put(ContainerSection.ARMOR, inventorySlots.subList(5, 9));
        slotRefs.put(ContainerSection.INVENTORY, inventorySlots.subList(9, 45));
        slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, inventorySlots.subList(9, 36));
        slotRefs.put(ContainerSection.INVENTORY_HOTBAR, inventorySlots.subList(36, 45));
        slotRefs.put(ContainerSection.CHEST, getExtraSlots());

        return slotRefs;
    }

    public List<Slot> getExtraSlots() {
        return Arrays.asList();
    }

}
