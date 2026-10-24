package makamys.satchels.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import makamys.satchels.ConfigSatchels;
import makamys.satchels.GuiHandler;
import makamys.satchels.Packets.MessageOpenContainer;
import makamys.satchels.Satchels;
import makamys.satchels.SatchelsItems;
import makamys.satchels.client.LayerSatchels;
import makamys.satchels.gui.GuiEquipment;
import makamys.satchels.gui.GuiSatchelsInventory;
import makamys.satchels.gui.GuiTabButton;
import makamys.satchels.gui.TooltippedItem;
import makamys.satchels.inventory.ContainerPlayerExtended;
import makamys.satchels.inventory.ContainerSatchels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SatchelsProxyClient extends SatchelsProxyCommon {

    public final KeyBinding openEquipment = new KeyBinding("key.satchels.openEquipment", Keyboard.KEY_P, "key.categories.satchels");

    private static final int TAB_ID_INVENTORY = 7770;
    private static final int TAB_ID_EQUIPMENT = 7771;
    private static final int INVTWEAKS_ID_BASE = 54696386;
    private static final String BAUBLES_BUTTON = "baubles.client.gui.GuiBaublesButton";
    private static final String GALACTICRAFT_INVENTORY_GUI =
        "micdoodle8.mods.galacticraft.core.client.gui.container.GuiExtendedInventory";
    private static final String TECHGUNS_INVENTORY_GUI = "techguns.gui.player.TGPlayerInventoryGui";
    private static final String GALACTICRAFT_TAB_REGISTRY =
        "micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry";
    private static final String GALACTICRAFT_VANILLA_TAB =
        "micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabVanilla";

    @Override
    public void init() {
        super.init();
        ClientRegistry.registerKeyBinding(openEquipment);
    }

    @Override
    public void postInit() {
        super.postInit();
        for (RenderPlayer render : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()) {
            render.addLayer(new LayerSatchels(render));
        }
    }

    @Override
    public EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().player;
    }

    @Override
    public void runOnClientThread(Runnable runnable) {
        Minecraft.getMinecraft().addScheduledTask(runnable);
    }

    @Override
    public String getOpenEquipmentKeyName() {
        return GameSettings.getKeyDisplayString(openEquipment.getKeyCode());
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen gui = event.getGui();
        if (gui != null && gui.doesGuiPauseGame()) {
            ConfigSatchels.reloadIfChanged();
        }
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (gui != null && player != null && gui.getClass() == GuiInventory.class
                && player.inventoryContainer instanceof ContainerSatchels && !player.isCreative()) {
            event.setGui(new GuiSatchelsInventory(player));
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (ConfigSatchels.hotSwap) {
            ConfigSatchels.reloadIfChanged();
        }
        while (openEquipment.isPressed()) {
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if ((screen == null || screen instanceof GuiContainer) && !(screen instanceof GuiEquipment)) {
                Satchels.networkWrapper.sendToServer(new MessageOpenContainer(GuiHandler.ID_EQUIPMENT));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onInitGuiPost(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen gui = event.getGui();
        if (gui instanceof GuiSatchelsInventory) {
            for (GuiButton button : event.getButtonList()) {
                if (button.getClass().getName().equals(BAUBLES_BUTTON)) {
                    button.x += 16;
                }
            }
        }
        if (gui instanceof GuiContainer && ((GuiContainer) gui).inventorySlots instanceof ContainerPlayerExtended) {
            event.getButtonList().removeIf(b -> b.id > INVTWEAKS_ID_BASE && b.id < INVTWEAKS_ID_BASE + 4);
        }
        if (!ConfigSatchels.satchelsTab) {
            return;
        }
        boolean inInventory = gui instanceof GuiInventory;
        boolean inEquipment = gui instanceof GuiEquipment;
        boolean inModInventory = isModInventoryTabScreen(gui);
        if (inInventory || inEquipment || inModInventory) {
            GuiContainer containerGui = (GuiContainer) gui;
            boolean hasGalacticraftTabs = inEquipment && addGalacticraftTabs(containerGui, event.getButtonList());
            int tabLeft = containerGui.getGuiLeft() + (gui instanceof GuiSatchelsInventory ? 16 : 0);
            int tabTop = containerGui.getGuiTop() - 28;
            if (gui instanceof GuiSatchelsInventory) {
                updateGalacticraftTabPositions(tabLeft, tabTop + 28);
            }
            int tabStartOffset = GuiTabButton.getTabStartOffset(event.getButtonList(), tabLeft, tabTop);
            if (inInventory && !hasGalacticraftVanillaTab()) {
                event.getButtonList().add(new GuiTabButton(TAB_ID_INVENTORY, 0, new ItemStack(Blocks.CRAFTING_TABLE), true,
                    0, () -> {}));
                event.getButtonList().add(new GuiTabButton(TAB_ID_EQUIPMENT, 1, new ItemStack(SatchelsItems.pouch), false,
                    0, () -> Satchels.networkWrapper.sendToServer(new MessageOpenContainer(GuiHandler.ID_EQUIPMENT))));
            } else if (inInventory || inModInventory) {
                event.getButtonList().add(new GuiTabButton(TAB_ID_EQUIPMENT, 0, new ItemStack(SatchelsItems.pouch), false,
                    tabStartOffset, () -> Satchels.networkWrapper.sendToServer(new MessageOpenContainer(GuiHandler.ID_EQUIPMENT))));
            } else if (hasGalacticraftTabs) {
                event.getButtonList().add(new GuiTabButton(TAB_ID_EQUIPMENT, 0, new ItemStack(SatchelsItems.pouch), true,
                    tabStartOffset, () -> {}));
            } else {
                event.getButtonList().add(new GuiTabButton(TAB_ID_INVENTORY, 0, new ItemStack(Blocks.CRAFTING_TABLE), false, 0, () -> {
                    Minecraft mc = Minecraft.getMinecraft();
                    int mouseX = Mouse.getX();
                    int mouseY = Mouse.getY();
                    mc.player.closeScreen();
                    mc.displayGuiScreen(new GuiInventory(mc.player));
                    Mouse.setCursorPosition(mouseX, mouseY);
                }));
                event.getButtonList().add(new GuiTabButton(TAB_ID_EQUIPMENT, 1, new ItemStack(SatchelsItems.pouch), true,
                    0, () -> {}));
            }
        }
    }

    private static boolean isModInventoryTabScreen(GuiScreen gui) {
        if (!(gui instanceof GuiContainer)) {
            return false;
        }
        String className = gui.getClass().getName();
        return className.equals(GALACTICRAFT_INVENTORY_GUI) || className.equals(TECHGUNS_INVENTORY_GUI);
    }

    private static boolean addGalacticraftTabs(GuiContainer gui, List<GuiButton> buttons) {
        try {
            Class<?> tabRegistry = Class.forName(GALACTICRAFT_TAB_REGISTRY);
            Method updateTabValues = tabRegistry.getMethod("updateTabValues", int.class, int.class, Class.class);
            Method addTabsToList = tabRegistry.getMethod("addTabsToList", List.class);
            updateTabValues.invoke(null, gui.getGuiLeft(), gui.getGuiTop(), GuiEquipment.class);
            addTabsToList.invoke(null, buttons);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Satchels.LOGGER.error("Failed to add Galacticraft inventory tabs to the Satchels equipment screen", e);
            return false;
        }
    }

    private static boolean hasGalacticraftVanillaTab() {
        try {
            Class<?> tabRegistry = Class.forName(GALACTICRAFT_TAB_REGISTRY);
            Method getTabList = tabRegistry.getMethod("getTabList");
            Object tabs = getTabList.invoke(null);
            if (tabs instanceof Iterable<?>) {
                for (Object tab : (Iterable<?>) tabs) {
                    if (tab.getClass().getName().equals(GALACTICRAFT_VANILLA_TAB)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Satchels.LOGGER.error("Failed to detect Galacticraft's vanilla inventory tab", e);
            return false;
        }
    }

    public static void updateGalacticraftTabPositions(int left, int top) {
        try {
            Class<?> tabRegistry = Class.forName(GALACTICRAFT_TAB_REGISTRY);
            Class<?> vanillaTab = Class.forName(GALACTICRAFT_VANILLA_TAB);
            Method updateTabValues = tabRegistry.getMethod("updateTabValues", int.class, int.class, Class.class);
            updateTabValues.invoke(null, left, top, vanillaTab);
        } catch (ClassNotFoundException e) {
            // Galacticraft is optional.
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Satchels.LOGGER.error("Failed to synchronize Galacticraft inventory tab positions", e);
        }
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof TooltippedItem) {
            List<String> linesNormal = new ArrayList<>();
            List<String> linesDetails = new ArrayList<>();
            ((TooltippedItem) event.getItemStack().getItem()).getTooltips(linesNormal, linesDetails, event);

            event.getToolTip().addAll(linesNormal);
            if (!linesDetails.isEmpty()) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    event.getToolTip().addAll(linesDetails);
                } else {
                    event.getToolTip().add(I18n.format("tooltip.satchels.holdCtrl"));
                }
            }
        }
    }
}
