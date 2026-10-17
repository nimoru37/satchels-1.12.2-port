package makamys.satchels.item;

import makamys.satchels.Satchels;
import net.minecraft.creativetab.CreativeTabs;

public class ItemSatchel extends ItemEquippable {

    public static final String EMPTY_SLOT_SPRITE = Satchels.MODID + ":items/empty_equipment_slot_satchel";

    public ItemSatchel() {
        setMaxStackSize(1);
        setTranslationKey(Satchels.MODID + ".satchel");
        setRegistryName(Satchels.MODID, "satchel");
        setCreativeTab(CreativeTabs.TOOLS);
    }

}
