package makamys.satchels;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** replaces 1.7.10's IExtendedEntityProperties: attaches an {@link EntityPropertiesSatchels} to every player */
public class SatchelsCapability {

    @CapabilityInject(EntityPropertiesSatchels.class)
    public static Capability<EntityPropertiesSatchels> CAPABILITY = null;

    public static final ResourceLocation KEY = new ResourceLocation(Satchels.MODID, "equipment");

    public static void register() {
        CapabilityManager.INSTANCE.register(EntityPropertiesSatchels.class,
            new Capability.IStorage<EntityPropertiesSatchels>() {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<EntityPropertiesSatchels> capability, EntityPropertiesSatchels instance, EnumFacing side) {
                    return instance.writeToNBT();
                }

                @Override
                public void readNBT(Capability<EntityPropertiesSatchels> capability, EntityPropertiesSatchels instance, EnumFacing side, NBTBase nbt) {
                    if (nbt instanceof NBTTagCompound) {
                        instance.readFromNBT((NBTTagCompound) nbt);
                    }
                }
            },
            () -> new EntityPropertiesSatchels(null));
    }

    public static class Provider implements ICapabilitySerializable<NBTTagCompound> {

        private final EntityPropertiesSatchels instance;

        public Provider(EntityPlayer player) {
            this.instance = new EntityPropertiesSatchels(player);
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CAPABILITY;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == CAPABILITY ? CAPABILITY.cast(instance) : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return instance.writeToNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            instance.readFromNBT(nbt);
        }
    }
}
