package makamys.satchels.core;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("SatchelsMixinLoader")
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE + 2)
public class SatchelsMixinLoader implements IFMLLoadingPlugin {

    private static final Logger LOGGER = LogManager.getLogger("Satchels");
    private static final String MIXIN_BOOTER_API = "zone.rong.mixinbooter.IEarlyMixinLoader";
    private static final String MIXIN_CONFIG = "mixins.satchels.json";

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        try {
            Class.forName("org.spongepowered.asm.mixin.Mixins")
                .getMethod("addConfiguration", String.class)
                .invoke(null, MIXIN_CONFIG);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Satchels could not register its mixin configuration through MixinBooter.", e);
            throw new IllegalStateException("Satchels failed to initialize its mixins.", e);
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    public SatchelsMixinLoader() {
        try {
            Class.forName(MIXIN_BOOTER_API, false, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            String message = "Satchels requires MixinBooter 8.0 or newer. "
                + "Install MixinBooter in this instance's mods folder and restart the game.";
            LOGGER.error(message);
            throw new IllegalStateException(message, e);
        }
    }
}
