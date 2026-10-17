package makamys.satchels.proxy;

import java.util.ArrayList;
import java.util.List;

import makamys.satchels.EntityPropertiesSatchels;
import makamys.satchels.Packets.MessageSyncEquipment;
import makamys.satchels.Packets.MessageSyncEquipmentVisual;
import makamys.satchels.Satchels;
import makamys.satchels.SatchelsCapability;
import makamys.satchels.inventory.ContainerSatchels;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SatchelsProxyCommon {

    public void preInit() {
    }

    public void init() {
    }

    public void postInit() {
    }

    public EntityPlayer getClientPlayer() {
        return null;
    }

    public void runOnClientThread(Runnable runnable) {
    }

    public String getOpenEquipmentKeyName() {
        return "";
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(SatchelsCapability.KEY, new SatchelsCapability.Provider((EntityPlayer) event.getObject()));
        }
    }

    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntity();
        if (!(player.inventoryContainer instanceof ContainerSatchels)) {
            boolean inventoryWasOpen = player.openContainer == player.inventoryContainer;
            ContainerSatchels container = new ContainerSatchels(player);
            player.inventoryContainer = container;
            if (inventoryWasOpen) {
                player.openContainer = container;
            }
        }
        ((ContainerSatchels) player.inventoryContainer).redoSlots(!player.isCreative());

        if (!player.world.isRemote) {
            EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(player);
            if (props != null) {
                props.updateInventories();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Container container = event.player.inventoryContainer;
        if (container instanceof ContainerSatchels) {
            ContainerSatchels satchels = (ContainerSatchels) container;
            boolean wantExtra = !event.player.isCreative();
            if (satchels.isExtraEnabled() != wantExtra) {
                satchels.redoSlots(wantExtra);
            }
        }
    }

    @SubscribeEvent
    public void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        sync(event.player);
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sync(event.player);
    }

    @SubscribeEvent
    public void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        sync(event.player);
    }

    @SubscribeEvent
    public void onStartTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntityPlayer && event.getEntityPlayer() instanceof EntityPlayerMP) {
            EntityPlayer target = (EntityPlayer) event.getTarget();
            EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(target);
            if (props != null) {
                Satchels.networkWrapper.sendTo(
                    new MessageSyncEquipmentVisual(target.getEntityId(), props.equipment.writeToNBT()),
                    (EntityPlayerMP) event.getEntityPlayer());
            }
        }
    }

    private static void sync(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(player);
            if (props != null) {
                Satchels.networkWrapper.sendTo(new MessageSyncEquipment(props.writeToNBT()), (EntityPlayerMP) player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerDrops(PlayerDropsEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(player);
        if (props == null) {
            return;
        }
        List<ItemStack> stacks = new ArrayList<>();
        props.collectAllStacks(stacks);
        for (ItemStack stack : stacks) {
            EntityItem item = new EntityItem(player.world, player.posX, player.posY + player.getEyeHeight() - 0.3, player.posZ, stack);
            item.setPickupDelay(40);
            float speed = player.getRNG().nextFloat() * 0.5f;
            float angle = player.getRNG().nextFloat() * ((float) Math.PI * 2f);
            item.motionX = -MathHelper.sin(angle) * speed;
            item.motionZ = MathHelper.cos(angle) * speed;
            item.motionY = 0.2;
            event.getDrops().add(item);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        EntityPropertiesSatchels from = EntityPropertiesSatchels.fromPlayer(event.getOriginal());
        EntityPropertiesSatchels to = EntityPropertiesSatchels.fromPlayer(event.getEntityPlayer());
        if (from != null && to != null) {
            to.copyFrom(from);
        }
    }
}
