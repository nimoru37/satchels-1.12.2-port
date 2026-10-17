package makamys.satchels;

import makamys.satchels.Packets.HandlerOpenContainer;
import makamys.satchels.Packets.HandlerSyncEquipment;
import makamys.satchels.Packets.MessageOpenContainer;
import makamys.satchels.Packets.MessageSyncEquipment;
import makamys.satchels.Packets.MessageSyncEquipmentVisual;
import makamys.satchels.proxy.SatchelsProxyCommon;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Satchels - modular player inventory upgrades. 1.12.2 port of the 1.7.10 mod by makamys */
@Mod(modid = Satchels.MODID,
     name = Satchels.NAME,
     version = Satchels.VERSION,
     acceptedMinecraftVersions = "[1.12.2]",
     dependencies = "required-after:mixinbooter@[8.0,)")
public class Satchels {

    public static final String MODID = "satchels";
    public static final String NAME = "Satchels";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MODID)
    public static Satchels instance;

    @SidedProxy(clientSide = "makamys.satchels.proxy.SatchelsProxyClient",
                serverSide = "makamys.satchels.proxy.SatchelsProxyCommon")
    public static SatchelsProxyCommon proxy;

    public static SimpleNetworkWrapper networkWrapper;

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigSatchels.init(event.getSuggestedConfigurationFile());
        SatchelsCapability.register();

        networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        networkWrapper.registerMessage(HandlerOpenContainer.class, MessageOpenContainer.class, 0, Side.SERVER);
        networkWrapper.registerMessage(HandlerSyncEquipment.class, MessageSyncEquipment.class, 1, Side.CLIENT);
        networkWrapper.registerMessage(Packets.HandlerSyncEquipmentVisual.class, MessageSyncEquipmentVisual.class, 2, Side.CLIENT);

        MinecraftForge.EVENT_BUS.register(proxy);
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }
}
