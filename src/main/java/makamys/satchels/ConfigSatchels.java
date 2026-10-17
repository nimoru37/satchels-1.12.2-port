package makamys.satchels;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

public class ConfigSatchels {

    public static Configuration config;

    private static File configFile;
    private static WatchService watcher;

    public static int pouchSlotColor; // TODO
    public static int pouchBgColor;
    public static int satchelSlotColor; // TODO
    public static int satchelBgColor;

    public static boolean hotSwap;
    public static boolean satchelsTab;

    public static int pouchUpgradeWeight;

    public static boolean drawSatchel;
    public static boolean drawSatchelStrap;
    public static boolean drawLeftPouch;
    public static boolean drawRightPouch;

    public static String satchelIngredient1;
    public static String satchelIngredient2;

    private static final Set<String> blacklist = new HashSet<>();

    private static final String[] DEFAULT_BLACKLIST = {
        "satchels:satchel",
        "satchels:pouch"
    };

    public static void init(File file) {
        configFile = file;
        reload();

        try {
            registerWatchService();
        } catch (IOException e) {
            Satchels.LOGGER.warn("Failed to register watch service: " + e + " (" + e.getMessage()
                + "). Changes to the config file will not be reflected");
        }
    }

    public static void reload() {
        config = new Configuration(configFile);
        config.load();
        reparse();
    }

    public static void reloadIfChanged() {
        if (watcher != null) {
            WatchKey key = watcher.poll();

            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.context().toString().equals(configFile.getName())) {
                        reload();
                    }
                }
                key.reset();
            }
        }
    }

    public static void reparse() {
        hotSwap = config.getBoolean("hotSwap", "_general", false,
            "Apply changes made in the config file immediately.\nOff by default because it could potentially cause poor performance on certain platforms.\nUseful for tweaking the GUI.");
        satchelsTab = config.getBoolean("satchelsTab", "_general", true, "Add Satchels tab to inventory GUI");

        pouchSlotColor = getColor("pouchSlotColor", "interface colors", "FFB266", "Not implemented yet!");
        pouchBgColor = getColor("pouchBgColor", "interface colors", "FFB266", "");
        satchelSlotColor = getColor("satchelSlotColor", "interface colors", "FFBF99", "Not implemented yet!");
        satchelBgColor = getColor("satchelBgColor", "interface colors", "FFBF99", "");

        pouchUpgradeWeight = config.getInt("pouchUpgradeWeight", "world generation", 7, 0, Integer.MAX_VALUE,
            "The weight of the pouch upgrade in the dungeon loot table.\nIncrease this to make them more common, or decrease to make them rarer.\nFor reference, saddles have a weight of 10 while golden apples have a weight of 1.\nYou might want to bump this up if you have many other mods adding loot, or if this is a multiplayer server.");

        drawSatchel = config.getBoolean("drawSatchel", "player model", true, "");
        drawSatchelStrap = config.getBoolean("drawSatchelStrap", "player model", true, "");
        drawLeftPouch = config.getBoolean("drawLeftPouch", "player model", true, "");
        drawRightPouch = config.getBoolean("drawRightPouch", "player model", true, "");

        satchelIngredient1 = config.getString("satchelIngredient1", "recipes", "minecraft:diamond_block",
            "The ingredient in the center of the bottom row of the satchel crafting recipe (registry name, requires restart)");
        satchelIngredient2 = config.getString("satchelIngredient2", "recipes", "minecraft:slime_ball",
            "The ingredient in the left and right of the center row of the satchel crafting recipe (registry name, requires restart)");

        String[] list = config.getStringList("itemBlacklist", "inventory", DEFAULT_BLACKLIST,
            "Items that aren't allowed in satchels or pouches.\nFormat: modid:item (matches any metadata) or modid:* (matches everything from a mod).");
        blacklist.clear();
        for (String s : list) {
            blacklist.add(s.trim().toLowerCase());
        }

        config.getCategory("_general").setComment(
            "Note: Changes in this file will get applied when the game is paused, or immediately if the hotSwap option is enabled.");

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static boolean isAllowed(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ResourceLocation name = stack.getItem().getRegistryName();
        if (name == null) {
            return true;
        }
        String full = name.toString().toLowerCase();
        String wildcard = name.getNamespace().toLowerCase() + ":*";
        return !(blacklist.contains(full) || blacklist.contains(wildcard));
    }

    private static int getColor(String name, String category, String defaultValue, String comment) {
        Pattern colorPattern = Pattern.compile("(0x)?[0-9a-fA-F]{6}");
        String str = config.getString(name, category, defaultValue, comment, colorPattern);
        str = str.replace("0x", "");
        try {
            return Integer.parseInt(str, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return Integer.parseInt(defaultValue, 16);
        }
    }

    private static void registerWatchService() throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        configFile.toPath().toAbsolutePath().getParent().register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }
}
