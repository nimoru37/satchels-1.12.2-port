package makamys.satchels.item;

import java.util.List;
import java.util.function.Predicate;

import makamys.satchels.EntityPropertiesSatchels;
import makamys.satchels.GuiHandler;
import makamys.satchels.Packets.MessageOpenContainer;
import makamys.satchels.Satchels;
import makamys.satchels.inventory.ContainerChestGeneric;
import makamys.satchels.inventory.SimpleInventory;
import makamys.satchels.inventory.SlotCustom;
import makamys.satchels.inventory.SlotDisabled;
import makamys.satchels.inventory.SlotSatchelRequired;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class ItemPouch extends ItemEquippable {

    public static final Predicate<ItemStack> acceptedContentsPredicate = (stack) -> !stack.isEmpty() && stack.getItem() instanceof ItemPouchUpgrade;

    public static final String EMPTY_SLOT_SPRITE = Satchels.MODID + ":items/empty_equipment_slot_pouch";

    private static final String NBT_INVENTORY = "Inventory";

    public ItemPouch() {
        setMaxStackSize(1);
        setTranslationKey(Satchels.MODID + ".pouch");
        setRegistryName(Satchels.MODID, "pouch");
        setCreativeTab(CreativeTabs.TOOLS);
    }

    public static IInventory getInventory(ItemStack stack, World world) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemPouch)) {
            return null;
        }
        return new InventoryPouch(stack, world);
    }

    public static int getSlotCount(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemPouch)) {
            return 0;
        }
        IInventory inventory = getInventory(stack, null);
        int slots = EntityPropertiesSatchels.POUCH_INITIAL_SLOTS;
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack invStack = inventory.getStackInSlot(i);
                if (!invStack.isEmpty() && invStack.getItem() instanceof ItemPouchUpgrade) {
                    slots++;
                }
            }
        }
        return slots;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.isSneaking()) {
            if (world.isRemote) {
                Satchels.networkWrapper.sendToServer(new MessageOpenContainer(GuiHandler.ID_POUCH, hand.ordinal()));
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        } else {
            return super.onItemRightClick(world, player, hand);
        }
    }

    public static class InventoryPouch extends SimpleInventory {

        private final ItemStack stack;
        private final World world;

        public InventoryPouch(ItemStack stack, World world) {
            super(EntityPropertiesSatchels.POUCH_MAX_SLOTS - EntityPropertiesSatchels.POUCH_INITIAL_SLOTS,
                stack.getDisplayName(), true);
            this.stack = stack;
            this.world = world;

            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey(NBT_INVENTORY)) {
                ItemStackHelper.loadAllItems(tag.getCompoundTag(NBT_INVENTORY), items);
            }
        }

        @Override
        public void closeInventory(EntityPlayer player) {
            if (world != null && !world.isRemote) {
                NBTTagCompound tag = new NBTTagCompound();
                ItemStackHelper.saveAllItems(tag, items);
                stack.setTagInfo(NBT_INVENTORY, tag);
            }
        }

        @Override
        public int getInventoryStackLimit() {
            return 1;
        }

        @Override
        public boolean isItemValidForSlot(int i, ItemStack itemstack) {
            return acceptedContentsPredicate.test(itemstack);
        }
    }

    @Override
    public void getTooltips(List<String> linesNormal, List<String> linesDetails, ItemTooltipEvent event) {
        int slots = ItemPouch.getSlotCount(event.getItemStack());
        linesNormal.add((slots > EntityPropertiesSatchels.POUCH_INITIAL_SLOTS ? "" + TextFormatting.YELLOW : "")
            + I18n.format("tooltip.satchels.pouch.slotCount", slots));

        linesDetails.add(I18n.format("tooltip.satchels.pouch.sneakUseHint"));

        super.getTooltips(linesNormal, linesDetails, event);
    }

    public static ContainerChestGeneric constructUpgradesContainer(InventoryPlayer playerInv, IInventory upgrades) {
        ContainerChestGeneric container = new ContainerChestGeneric(playerInv, upgrades) {
            @Override
            protected Slot constructSlot(IInventory inventory, int slotIndex, int displayX, int displayY) {
                return new SlotCustom(inventory, slotIndex, displayX, displayY)
                    .setBackground(ItemPouchUpgrade.EMPTY_SLOT_SPRITE);
            }
        };

        for (int i = 0; i < container.inventorySlots.size(); i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot.inventory == playerInv && slot.getSlotIndex() == playerInv.currentItem) {
                container.inventorySlots.set(i, new SlotDisabled(slot, true));
            }
        }

        EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(playerInv.player);
        if ((props == null || !props.hasSatchel()) && upgrades.getSizeInventory()
                == EntityPropertiesSatchels.POUCH_MAX_SLOTS - EntityPropertiesSatchels.POUCH_INITIAL_SLOTS) {
            Slot lastUpgradeSlot = container.inventorySlots.get(upgrades.getSizeInventory() - 1);
            container.inventorySlots.set(lastUpgradeSlot.slotNumber, new SlotSatchelRequired(lastUpgradeSlot));
        }

        return container;
    }
}
