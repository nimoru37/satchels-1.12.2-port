package makamys.satchels.client;

import makamys.satchels.ConfigSatchels;
import makamys.satchels.EntityPropertiesSatchels;
import makamys.satchels.Satchels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;

/** render satchels and pouches on the player */
public class LayerSatchels implements LayerRenderer<AbstractClientPlayer> {

    private static final float DEFAULT_SCALE_MOD = 16f / 24f;

    private static final ResourceLocation SATCHEL_TEXTURE = new ResourceLocation(Satchels.MODID, "textures/models/satchel.png");
    private static final ResourceLocation POUCH_TEXTURE = new ResourceLocation(Satchels.MODID, "textures/models/pouch.png");

    private final RenderPlayer renderer;

    private final ModelRenderer satchel;
    private final ModelRenderer strap;
    private final ModelRenderer pouchLeft;
    private final ModelRenderer pouchRight;

    public LayerSatchels(RenderPlayer renderer) {
        this.renderer = renderer;
        ModelBase base = new ModelBase() {};

        satchel = new ModelRenderer(base, 0, 0).setTextureSize(32, 32);
        satchel.addBox(0, 0, 0, 8, 7, 2);
        satchel.setRotationPoint(-7.5f, 13, 4);
        satchel.rotateAngleZ = 0.05f;
        satchel.rotateAngleY = (float) (Math.PI / 2f);

        strap = new ModelRenderer(base, 0, 9).setTextureSize(32, 32);
        strap.addBox(0, 0, 0, 8, 19, 1);
        strap.setRotationPoint(7.5f - 0.2f, -0.1f + 0.4f, -4);
        strap.rotateAngleY = -(float) (Math.PI / 2f);
        strap.rotateAngleZ = 0.75f;

        pouchLeft = new ModelRenderer(base, 0, 0).setTextureSize(16, 16);
        pouchLeft.addBox(0, 0, 0, 5, 5, 2);
        pouchLeft.setRotationPoint(8f, 15, -3);
        pouchLeft.rotateAngleY = -(float) (Math.PI / 2f);

        pouchRight = new ModelRenderer(base, 0, 0).setTextureSize(16, 16);
        pouchRight.addBox(0, 0, 0, 5, 5, 2);
        pouchRight.setRotationPoint(-1.5f, 15, 5f);
        pouchRight.rotateAngleY = (float) Math.PI;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (player.isInvisible()) {
            return;
        }
        EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(player);
        if (props == null) {
            return;
        }
        boolean hasChestplate = !player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty();
        ModelBiped model = renderer.getMainModel();

        boolean drawSatchel = props.hasSatchel() && ConfigSatchels.drawSatchel;
        boolean drawLeft = props.hasLeftPouch() && ConfigSatchels.drawLeftPouch;
        boolean drawRight = props.hasRightPouch() && ConfigSatchels.drawRightPouch;
        if (!drawSatchel && !drawLeft && !drawRight) {
            return;
        }

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.pushMatrix();
        model.bipedBody.postRender(scale);

        if (drawSatchel) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(SATCHEL_TEXTURE);
            float mod = DEFAULT_SCALE_MOD * (hasChestplate ? 1.2f : 1f);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0f, hasChestplate ? -0.08f : 0f, 0f);
            renderPart(satchel, scale, mod);
            if (ConfigSatchels.drawSatchelStrap) {
                renderPart(strap, scale, mod * 1.001f);
            }
            GlStateManager.popMatrix();
        }
        if (drawLeft || drawRight) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(POUCH_TEXTURE);
            if (drawLeft) {
                renderPart(pouchLeft, scale, DEFAULT_SCALE_MOD);
            }
            if (drawRight) {
                renderPart(pouchRight, scale, DEFAULT_SCALE_MOD);
            }
        }
        GlStateManager.popMatrix();
    }

    private static void renderPart(ModelRenderer part, float scale, float mod) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(mod, mod, mod);
        part.render(scale);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
