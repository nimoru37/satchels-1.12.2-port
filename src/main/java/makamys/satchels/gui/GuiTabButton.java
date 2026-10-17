package makamys.satchels.gui;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/** a tab above the inventory GUI */
public class GuiTabButton extends GuiButton {

    private static final ResourceLocation TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

    private final int index;
    private final ItemStack icon;
    private final boolean selected;
    private final Runnable action;
    private int tabStartOffset;
    private boolean firstInRow;
    private boolean drawnBeforePanel;

    public GuiTabButton(int id, int index, ItemStack icon, boolean selected, int tabStartOffset, Runnable action) {
        super(id, 0, 0, 28, 28, "");
        this.index = index;
        this.icon = icon;
        this.selected = selected;
        this.tabStartOffset = tabStartOffset;
        this.action = action;
    }

    public static int getTabStartOffset(List<GuiButton> buttons, int left, int top) {
        int rightmostTab = left - 1;
        for (GuiButton button : buttons) {
            if (!(button instanceof GuiTabButton) && button.visible && button.y == top && button.x >= left - 28 && button.x < left + 280
                    && button.width >= 24 && button.width <= 32 && button.height >= 24 && button.height <= 32) {
                rightmostTab = Math.max(rightmostTab, button.x + button.width - 1);
            }
        }
        return rightmostTab + 1 - left;
    }

    public void setTabStartOffset(int tabStartOffset) {
        this.tabStartOffset = tabStartOffset;
    }

    public static void drawInactiveTabs(Minecraft mc, List<GuiButton> buttons) {
        for (GuiButton button : buttons) {
            if (button instanceof GuiTabButton && button.visible && !((GuiTabButton) button).selected) {
                GuiTabButton tab = (GuiTabButton) button;
                tab.drawTab(mc);
                tab.drawnBeforePanel = true;
            }
        }
    }

    private void reposition(Minecraft mc) {
        if (mc.currentScreen instanceof GuiContainer) {
            GuiContainer gui = (GuiContainer) mc.currentScreen;
            int left = gui.getGuiLeft() + (gui instanceof GuiSatchelsInventory ? 16 : 0);
            this.x = left + tabStartOffset + index * 28;
            firstInRow = this.x == left;
            this.y = gui.getGuiTop() - 28;
        }
    }

    private boolean isInside(int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + 28 && mouseY < y + 28;
    }

    public void drawTab(Minecraft mc) {
        reposition(mc);
        if (!visible) {
            return;
        }
        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(TABS);
        int u = firstInRow ? 0 : 28;
        drawTexturedModalRect(x, y, u, selected ? 32 : 0, 28, 32);

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        mc.getRenderItem().renderItemAndEffectIntoGUI(icon, x + 6, y + 9);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        reposition(mc);
        hovered = visible && isInside(mouseX, mouseY);
        if (drawnBeforePanel) {
            drawnBeforePanel = false;
            return;
        }
        drawTab(mc);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        reposition(mc);
        if (visible && enabled && !selected && isInside(mouseX, mouseY)) {
            playPressSound(mc.getSoundHandler());
            action.run();
        }
        return false;
    }
}
