package makamys.satchels;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Packets {

    public static class MessageOpenContainer implements IMessage {

        public int id;
        public int data;

        public MessageOpenContainer() {}

        public MessageOpenContainer(int id) {
            this(id, 0);
        }

        public MessageOpenContainer(int id, int data) {
            this.id = id;
            this.data = data;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            id = buf.readInt();
            data = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(id);
            buf.writeInt(data);
        }
    }

    public static class HandlerOpenContainer implements IMessageHandler<MessageOpenContainer, IMessage> {

        @Override
        public IMessage onMessage(MessageOpenContainer message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().player;
            final int id = message.id;
            final int data = message.data;
            player.getServerWorld().addScheduledTask(() ->
                player.openGui(Satchels.instance, id, player.world, data, (int) player.posY, (int) player.posZ));
            return null;
        }
    }

    public static class MessageSyncEquipment implements IMessage {

        public NBTTagCompound tag;

        public MessageSyncEquipment() {}

        public MessageSyncEquipment(NBTTagCompound tag) {
            this.tag = tag;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            tag = ByteBufUtils.readTag(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeTag(buf, tag);
        }
    }

    public static class HandlerSyncEquipment implements IMessageHandler<MessageSyncEquipment, IMessage> {

        @Override
        public IMessage onMessage(final MessageSyncEquipment message, MessageContext ctx) {
            Satchels.proxy.runOnClientThread(() -> {
                EntityPlayer player = Satchels.proxy.getClientPlayer();
                if (player == null) {
                    return;
                }
                EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(player);
                if (props != null && message.tag != null) {
                    props.readFromNBT(message.tag);
                    props.updateInventories();
                }
            });
            return null;
        }
    }

    public static class MessageSyncEquipmentVisual implements IMessage {

        public int entityId;
        public NBTTagCompound equipment;

        public MessageSyncEquipmentVisual() {}

        public MessageSyncEquipmentVisual(int entityId, NBTTagCompound equipment) {
            this.entityId = entityId;
            this.equipment = equipment;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            entityId = buf.readInt();
            equipment = ByteBufUtils.readTag(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(entityId);
            ByteBufUtils.writeTag(buf, equipment);
        }
    }

    public static class HandlerSyncEquipmentVisual implements IMessageHandler<MessageSyncEquipmentVisual, IMessage> {

        @Override
        public IMessage onMessage(MessageSyncEquipmentVisual message, MessageContext ctx) {
            Satchels.proxy.runOnClientThread(() -> {
                EntityPlayer player = Satchels.proxy.getClientPlayer();
                if (player == null || player.world == null) {
                    return;
                }
                net.minecraft.entity.Entity entity = player.world.getEntityByID(message.entityId);
                if (entity instanceof EntityPlayer) {
                    EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer((EntityPlayer) entity);
                    if (props != null && message.equipment != null) {
                        props.equipment.readFromNBT(message.equipment);
                    }
                }
            });
            return null;
        }
    }

    public static void syncEquipmentToTracking(EntityPlayer player) {
        if (player.world.isRemote || !(player.world instanceof WorldServer)) {
            return;
        }
        EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(player);
        if (props != null) {
            Packet<?> packet = Satchels.networkWrapper.getPacketFrom(
                new MessageSyncEquipmentVisual(player.getEntityId(), props.equipment.writeToNBT()));
            ((WorldServer) player.world).getEntityTracker().sendToTracking(player, packet);
        }
    }

    public static void syncEquipmentToPlayer(EntityPlayer player) {
        if (player.world.isRemote || !(player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPropertiesSatchels props = EntityPropertiesSatchels.fromPlayer(player);
        if (props != null) {
            Satchels.networkWrapper.sendTo(new MessageSyncEquipment(props.writeToNBT()), (EntityPlayerMP) player);
        }
    }

}
