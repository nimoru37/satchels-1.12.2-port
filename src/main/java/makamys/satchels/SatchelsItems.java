package makamys.satchels;

import makamys.satchels.item.ItemPouch;
import makamys.satchels.item.ItemPouchUpgrade;
import makamys.satchels.item.ItemSatchel;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;

/** item / sound / recipe / loot registration. */
@Mod.EventBusSubscriber(modid = Satchels.MODID)
public class SatchelsItems {

    public static final ItemSatchel satchel = new ItemSatchel();
    public static final ItemPouch pouch = new ItemPouch();
    public static final ItemPouchUpgrade pouch_upgrade = new ItemPouchUpgrade();

    public static final ResourceLocation EQUIP_SOUND_ID = new ResourceLocation(Satchels.MODID, "item.armor.equip_leather");
    public static final SoundEvent EQUIP_SOUND = new SoundEvent(EQUIP_SOUND_ID).setRegistryName(EQUIP_SOUND_ID);

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(satchel, pouch, pouch_upgrade);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(EQUIP_SOUND);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        Object ingredient1 = resolveItemOr(ConfigSatchels.satchelIngredient1, Blocks.DIAMOND_BLOCK);
        Object ingredient2 = resolveItemOr(ConfigSatchels.satchelIngredient2, Items.SLIME_BALL);

        event.getRegistry().register(new ShapedOreRecipe(new ResourceLocation(Satchels.MODID, "satchel"),
            satchel, "S S", "2L2", "L1L",
            'L', Items.LEATHER, 'S', Items.STRING, '1', ingredient1, '2', ingredient2)
            .setRegistryName(Satchels.MODID, "satchel"));

        event.getRegistry().register(new ShapedOreRecipe(new ResourceLocation(Satchels.MODID, "pouch"),
            pouch, "SLS", "L L", "SLS",
            'L', Items.LEATHER, 'S', Items.STRING)
            .setRegistryName(Satchels.MODID, "pouch"));
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!LootTableList.CHESTS_SIMPLE_DUNGEON.equals(event.getName())) {
            return;
        }
        LootEntry entry = new LootEntryItem(pouch_upgrade, ConfigSatchels.pouchUpgradeWeight, 0,
            new LootFunction[0], new LootCondition[0], Satchels.MODID + ":pouch_upgrade");

        LootPool main = event.getTable().getPool("main");
        if (main != null) {
            main.addEntry(entry);
        } else {
            event.getTable().addPool(new LootPool(new LootEntry[] { entry }, new LootCondition[0],
                new RandomValueRange(1), new RandomValueRange(0), Satchels.MODID + "_pouch_upgrade"));
        }
    }

    private static Object resolveItemOr(String registryName, Object fallback) {
        Item item = registryName == null || registryName.isEmpty() ? null : Item.REGISTRY.getObject(new ResourceLocation(registryName));
        if (item == null) {
            Satchels.LOGGER.debug("Couldn't resolve item " + registryName + ", falling back to default " + fallback);
            return fallback;
        }
        return item;
    }
}
