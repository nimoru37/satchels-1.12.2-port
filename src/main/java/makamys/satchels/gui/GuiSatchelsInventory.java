package makamys.satchels.gui;

import static makamys.satchels.inventory.ContainerSatchels.PLAYER_H;
import static makamys.satchels.inventory.ContainerSatchels.PLAYER_W;
import static makamys.satchels.inventory.ContainerSatchels.PLAYER_X;
import static makamys.satchels.inventory.ContainerSatchels.PLAYER_Y;

import java.io.IOException;
import java.util.List;

import org.lwjgl.opengl.GL11;

import makamys.satchels.ConfigSatchels;
import makamys.satchels.Satchels;
import makamys.satchels.proxy.SatchelsProxyClient;
import makamys.satchels.inventory.ContainerSatchels;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

/** survival inventory screen */
public class GuiSatchelsInventory extends GuiInventory {

    private static final int RECIPE_BUTTON_ID = 10;
    private static final ResourceLocation SATCHELS_INVENTORY_BACKGROUND =
        new ResourceLocation(Satchels.MODID, "textures/gui/container/inventory.png");

    private final ContainerSatchels satchelsSlots;
    private final int originalXSize, originalYSize;

    protected static final ResourceLocation sidebar_corners = new ResourceLocation(Satchels.MODID, "textures/gui/container/sidebar_corners.png");

    public GuiSatchelsInventory(EntityPlayer player) {
        super(player);
        this.satchelsSlots = (ContainerSatchels) player.inventoryContainer;

        originalXSize = this.xSize;
        originalYSize = this.ySize;
    }

    @Override
    public void initGui() {
        this.xSize = originalXSize + 16 + (satchelsSlots.getEnabledRightPouchSlots().isEmpty() ? 0 : 16);
        this.ySize = originalYSize + (satchelsSlots.getSatchelProps().hasSatchel() ? 16 : 0);
        super.initGui();
        replaceRecipeButtonTexture();
        updateLayoutPosition();
        updateTabPositions();
    }

    private void replaceRecipeButtonTexture() {
        for (int i = 0; i < this.buttonList.size(); i++) {
            GuiButton button = this.buttonList.get(i);
            if (button.id == RECIPE_BUTTON_ID) {
                this.buttonList.set(i, new GuiButtonImage(RECIPE_BUTTON_ID, button.x, button.y,
                    20, 18, 178, 0, 19, SATCHELS_INVENTORY_BACKGROUND));
                return;
            }
        }
    }

    private int getFullLayoutGuiLeft() {
        int fullLayoutWidth = originalXSize + 2 * 16;
        if (this.mc.player.getRecipeBook().isGuiOpen() && this.width >= 379) {
            return 177 + (this.width - fullLayoutWidth - 200) / 2;
        }
        return (this.width - fullLayoutWidth) / 2;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == RECIPE_BUTTON_ID) {
            updateLayoutPosition();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateLayoutPosition();
        updateTabPositions();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void updateTabPositions() {
        int tabLeft = this.guiLeft + 16;
        int tabTop = this.guiTop - 28;
        SatchelsProxyClient.updateGalacticraftTabPositions(tabLeft, tabTop + 28);

        int tabStartOffset = GuiTabButton.getTabStartOffset(this.buttonList, tabLeft, tabTop);
        for (GuiButton button : this.buttonList) {
            if (button instanceof GuiTabButton) {
                ((GuiTabButton) button).setTabStartOffset(tabStartOffset);
            }
        }
    }

    private void updateLayoutPosition() {
        this.guiLeft = getFullLayoutGuiLeft();
        positionRecipeButton();
    }

    private void positionRecipeButton() {
        int supposedTop = (this.height - this.originalYSize) / 2;
        for (GuiButton button : this.buttonList) {
            if (button.id == RECIPE_BUTTON_ID) {
                button.x = this.guiLeft + 16 + 104;
                button.y = this.height / 2 - 22 + (this.guiTop - supposedTop);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("container.crafting"), 97 + 16, 8, 4210752);
    }

    private static void colorFromRGB(int rgb) {
        GlStateManager.color(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, 1f);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        boolean hasSatchel = satchelsSlots.getSatchelProps().hasSatchel();

        GlStateManager.color(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(SATCHELS_INVENTORY_BACKGROUND);
        int k = this.guiLeft + 16;
        int l = this.guiTop;

        int playerXOff = satchelsSlots.getArmorXOffset();

        this.drawTexturedModalRect(k, l, 0, 0, originalXSize, PLAYER_Y);

        this.drawTexturedModalRect(k, l + PLAYER_Y, 0, PLAYER_Y, 7, PLAYER_H);
        this.drawTexturedModalRect(k + 7, l + PLAYER_Y, 3, PLAYER_Y, playerXOff, PLAYER_H);
        this.drawTexturedModalRect(k + 7 + playerXOff, l + PLAYER_Y, 7, PLAYER_Y, 18, PLAYER_H);
        this.drawTexturedModalRect(k + PLAYER_X + playerXOff, l + PLAYER_Y, PLAYER_X, PLAYER_Y, PLAYER_W / 2, PLAYER_H);
        this.drawTexturedModalRect(k + PLAYER_X + playerXOff + PLAYER_W / 2, l + PLAYER_Y, PLAYER_X + PLAYER_W / 2 + playerXOff, PLAYER_Y, PLAYER_W - PLAYER_W / 2 - playerXOff, PLAYER_H);
        this.drawTexturedModalRect(k + PLAYER_X + PLAYER_W, l + PLAYER_Y, PLAYER_X + PLAYER_W, PLAYER_Y, originalXSize - (PLAYER_X + PLAYER_W), PLAYER_H);

        this.drawTexturedModalRect(k, l + PLAYER_Y + PLAYER_H, 0, PLAYER_Y + PLAYER_H, originalXSize, 2);

        if (hasSatchel) {
            GL11.glPushAttrib(GL11.GL_CURRENT_BIT);
            colorFromRGB(ConfigSatchels.satchelBgColor);
            this.drawTexturedModalRect(k, l + 80 + 1, 0, 80 + 1, originalXSize, 18 + 2);
            GL11.glPopAttrib();
        }

        GlStateManager.color(1f, 1f, 1f, 1f);
        this.drawTexturedModalRect(k, l + 80 + (hasSatchel ? 18 + 3 : 0), 0, 80 + (hasSatchel ? 3 : 0), originalXSize, this.ySize - 80 - (hasSatchel ? 16 + 3 : 0));

        GL11.glPushAttrib(GL11.GL_CURRENT_BIT);
        colorFromRGB(ConfigSatchels.pouchBgColor);
        for (int side = 0; side < 2; side++) {
            List<Slot> pouchSlots = side == 0 ? satchelsSlots.getEnabledLeftPouchSlots() : satchelsSlots.getEnabledRightPouchSlots();
            if (!pouchSlots.isEmpty()) {
                Slot first = pouchSlots.get(0);
                int no = pouchSlots.size();

                boolean left = side == 0;

                int xOff = left ? -24 : -17;

                int firstX = k + first.xPos + xOff;
                int firstY = l + first.yPos - 1;

                int u = left ? 0 : 151;
                int uOff = left ? 0 : 7;
                int edgeXOff = left ? 18 : 0;

                int bottomCornerU = left ? 0 : 7;
                int bottomCornerV = 0;
                int topCornerU = left ? 0 : 7;
                int topCornerV = no == 3 ? 7 : 14;

                this.drawTexturedModalRect(firstX + uOff, firstY - 18 * no + 11, u + uOff, 0, 18, 7);

                for (int i = 0; i < no; i++) {
                    this.drawTexturedModalRect(firstX, firstY - 18 * i, u, 141, 25, 18);
                }
                this.drawTexturedModalRect(firstX + uOff, firstY + 18, u + uOff, 141 + 18, 18, 7);

                if (no == 8 && !hasSatchel) {
                    this.drawTexturedModalRect(firstX + edgeXOff, firstY - 18 * no + 11, left ? 169 : 0, 0, 7, 7);
                }

                this.mc.getTextureManager().bindTexture(sidebar_corners);
                this.drawTexturedModalRect(firstX + edgeXOff, firstY - 18 * no + 11, topCornerU, topCornerV, 7, 7);
                this.drawTexturedModalRect(firstX + edgeXOff, firstY + 18, bottomCornerU, bottomCornerV, 7, 7);
                this.mc.getTextureManager().bindTexture(SATCHELS_INVENTORY_BACKGROUND);
            }
        }

        GL11.glPopAttrib();
        GlStateManager.color(1f, 1f, 1f, 1f);

        drawEntityOnScreen(k + PLAYER_X + 26, l + PLAYER_Y + 68, 30, (float) (k + 51) - (float) mouseX,
            (float) (l + 75 - 50) - (float) mouseY, this.mc.player);

        positionRecipeButton();
    }
}
