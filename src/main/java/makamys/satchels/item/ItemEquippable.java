package makamys.satchels.item;

import java.util.List;

import makamys.satchels.ConfigSatchels;
import makamys.satchels.EntityPropertiesSatchels;
import makamys.satchels.Satchels;
import makamys.satchels.gui.TooltippedItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class ItemEquippable extends Item implements TooltippedItem {

    @Override
    public void getTooltips(List<String> linesNormal, List<String> linesDetails, ItemTooltipEvent event) {
        if (!ConfigSatchels.satchelsTab) {
            linesDetails.add(I18n.format("tooltip.satchels.equipHint", Satchels.proxy.getOpenEquipmentKeyName()));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(player);
        if (props == null) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        ItemStack remaining = props.equip(stack);
        if (remaining.getCount() == stack.getCount()) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, remaining);
    }

}
