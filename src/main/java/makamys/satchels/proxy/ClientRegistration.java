package makamys.satchels.proxy;

import makamys.satchels.Satchels;
import makamys.satchels.SatchelsItems;
import makamys.satchels.item.ItemPouch;
import makamys.satchels.item.ItemPouchUpgrade;
import makamys.satchels.item.ItemSatchel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Satchels.MODID, value = Side.CLIENT)
public class ClientRegistration {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (Item item : new Item[] { SatchelsItems.satchel, SatchelsItems.pouch, SatchelsItems.pouch_upgrade }) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.getMap() != Minecraft.getMinecraft().getTextureMapBlocks()) {
            return;
        }
        for (String sprite : new String[] { ItemSatchel.EMPTY_SLOT_SPRITE, ItemPouch.EMPTY_SLOT_SPRITE, ItemPouchUpgrade.EMPTY_SLOT_SPRITE }) {
            event.getMap().registerSprite(new ResourceLocation(sprite));
        }
    }
}
