package makamys.satchels.gui;

import makamys.satchels.inventory.ContainerEquipment;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiEquipment extends InventoryEffectRenderer {

    protected static final ResourceLocation equipmentTexture = new ResourceLocation("satchels", "textures/gui/container/equipment.png");

    private float oldMouseX;
    private float oldMouseY;

    public GuiEquipment(EntityPlayer player) {
        super(new ContainerEquipment(player.inventory, !player.world.isRemote, player));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawCenteredText(I18n.format("container.equipment.satchel"), 123, 12);
        drawCenteredText(I18n.format("container.equipment.pouches"), 124, 48);
    }

    private void drawCenteredText(String text, int centerX, int y) {
        this.fontRenderer.drawString(text, centerX - this.fontRenderer.getStringWidth(text) / 2, y, 4210752);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(equipmentTexture);
        int k = this.guiLeft;
        int l = this.guiTop;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        GuiInventory.drawEntityOnScreen(k + 51, l + 75, 30, (float) (k + 51) - this.oldMouseX,
            (float) (l + 75 - 50) - this.oldMouseY, this.mc.player);
    }
}
