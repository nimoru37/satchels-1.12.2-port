package makamys.satchels.item;

import java.util.List;

import makamys.satchels.Satchels;
import makamys.satchels.gui.TooltippedItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class ItemPouchUpgrade extends Item implements TooltippedItem {

    public static final String EMPTY_SLOT_SPRITE = Satchels.MODID + ":items/empty_pouch_upgrade_slot";

    public ItemPouchUpgrade() {
        setMaxStackSize(16);
        setTranslationKey(Satchels.MODID + ".pouch_upgrade");
        setRegistryName(Satchels.MODID, "pouch_upgrade");
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public void getTooltips(List<String> linesNormal, List<String> linesDetails, ItemTooltipEvent event) {
        linesDetails.add(I18n.format("tooltip.satchels.pouchUpgrade"));
    }
}
