package makamys.satchels.gui;

import java.util.ArrayList;
import java.util.List;

import makamys.satchels.inventory.ContainerChestGeneric;
import makamys.satchels.inventory.SlotSatchelRequired;
import makamys.satchels.item.ItemPouch;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class GuiChestGeneric extends GuiContainer {
    protected static final ResourceLocation chestGenericTexture = new ResourceLocation("satchels", "textures/gui/container/chest_generic.png");

    private final IInventory playerInventory;
    private final IInventory chestInventory;
    private final int inventoryRows;
    private final int rowSize;

    public GuiChestGeneric(InventoryPlayer playerInventory, IInventory chestInventory) {
        super(ItemPouch.constructUpgradesContainer(playerInventory, chestInventory));
        this.rowSize = ((ContainerChestGeneric) this.inventorySlots).getRowSize();
        this.playerInventory = playerInventory;
        this.chestInventory = chestInventory;
        this.inventoryRows = (int) Math.ceil(chestInventory.getSizeInventory() / (float) this.rowSize);
        this.ySize = 114 + this.inventoryRows * 18;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if (this.mc.player.inventory.getItemStack().isEmpty()
                && this.getSlotUnderMouse() instanceof SlotSatchelRequired) {
            List<String> tooltip = new ArrayList<String>();
            if (this.getSlotUnderMouse().getHasStack()) {
                tooltip.addAll(this.getItemToolTip(this.getSlotUnderMouse().getStack()));
            }
            tooltip.add(I18n.format("tooltip.satchels.pouchUpgrade.requiresSatchel"));
            this.drawHoveringText(tooltip, mouseX, mouseY);
            return;
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.chestInventory.hasCustomName()
            ? this.chestInventory.getName() : I18n.format(this.chestInventory.getName()), 8, 6, 0x404040);
        this.fontRenderer.drawString(this.playerInventory.hasCustomName()
            ? this.playerInventory.getName() : I18n.format(this.playerInventory.getName()), 8, this.ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(chestGenericTexture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, 17);
        int slotStartX = this.inventorySlots.getSlot(0).xPos;
        for (int row = 0; row < this.inventoryRows; ++row) {
            this.drawTexturedModalRect(k, l + 17 + row * 18, 0, 133, this.xSize, 18);
            this.drawTexturedModalRect(k + slotStartX - 1, l + 17 + row * 18, 7, 19, this.rowSize * 18, 18);
        }
        this.drawTexturedModalRect(k, l + this.inventoryRows * 18 + 17, 0, 37, this.xSize, 96);
    }
}