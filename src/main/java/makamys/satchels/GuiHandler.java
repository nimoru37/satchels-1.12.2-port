package makamys.satchels;

import makamys.satchels.gui.GuiChestGeneric;
import makamys.satchels.gui.GuiEquipment;
import makamys.satchels.inventory.ContainerEquipment;
import makamys.satchels.item.ItemPouch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static final int ID_EQUIPMENT = 0;
    public static final int ID_POUCH = 1;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        switch (id) {
        case ID_EQUIPMENT:
            return new ContainerEquipment(player.inventory, !world.isRemote, player);
        case ID_POUCH:
            IInventory inv = ItemPouch.getInventory(getHeld(player, x), world);
            return inv != null ? ItemPouch.constructUpgradesContainer(player.inventory, inv) : null;
        default:
            throw new IllegalArgumentException("Unknown container ID: " + id);
        }
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        switch (id) {
        case ID_EQUIPMENT:
            return new GuiEquipment(player);
        case ID_POUCH:
            IInventory inv = ItemPouch.getInventory(getHeld(player, x), world);
            return inv != null ? new GuiChestGeneric(player.inventory, inv) : null;
        default:
            throw new IllegalArgumentException("Unknown container ID: " + id);
        }
    }

    private static ItemStack getHeld(EntityPlayer player, int handOrdinal) {
        return player.getHeldItem(EnumHand.values()[MathHelper.clamp(handOrdinal, 0, EnumHand.values().length - 1)]);
    }

}
