package dev.keebler408.bn;

import dev.keebler408.bt.commands.BazaarToolsCommand;
import dev.keebler408.bt.handlers.ChestTickHandler;
import dev.keebler408.bt.handlers.EventHandler;
import dev.keebler408.bt.handlers.MouseHandler;
import dev.keebler408.bt.handlers.UpdateHandler;
import dev.keebler408.bt.modules.ModuleList;
import dev.keebler408.bt.utilities.Defaults;
import dev.keebler408.bt.utilities.ScheduledEvents;
import dev.keebler408.bt.utilities.Utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

@Mod(modid = BazaarTools.MODID, version = BazaarTools.VERSION)
public class BazaarTools {

  public static final String MODID = "BazaarTools";
  public static final String VERSION = "1.3.9";
  public static final String prefix =
      EnumChatFormatting.GOLD + "[" + EnumChatFormatting.YELLOW + "BN" + EnumChatFormatting.GOLD + "] " + EnumChatFormatting.RESET;
  public static String apiKey = "";

  public static DecimalFormat df = new DecimalFormat("#,###.0");
  public static DecimalFormat dfNoDecimal = new DecimalFormat("#,###");

  public static boolean activeBazaar = true;
  public static boolean inBazaar = false;
  public static boolean forceRender = false;
  public static boolean validApiKey = false;
  public static boolean apiKeyDisabled = true; // Change this if an api key is ever required to access the bazaar again.

  public static JSONArray orders = new JSONArray();
  public static JSONObject bazaarDataRaw = new JSONObject();
  public static JSONObject bazaarCache = new JSONObject();
  public static JSONArray bazaarDataFormatted = new JSONArray();

  public static JSONObject bazaarConversions = new JSONObject(
      new JSONTokener(BazaarTools.class.getResourceAsStream("/bazaarConversions.json")));
  public static JSONObject bazaarConversionsReversed = new JSONObject(
      new JSONTokener(BazaarTools.class.getResourceAsStream("/bazaarConversionsReversed.json")));

  public static File configFile;

  public static ModuleList modules;

  public static void resetMod() {
    modules.resetAll();
    orders = Defaults.DEFAULT_ORDERS_LAYOUT();
  }

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    configFile = event.getSuggestedConfigurationFile();
    String config = null;

    try {
      if (configFile.isFile()) {
        config = new String(Files.readAllBytes(Paths.get(configFile.getPath())));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (config != null && Utils.isValidJSONObject(config)) {
      modules = new ModuleList(
          new JSONObject(config));
    } else {
      modules = new ModuleList();
    }
  }

  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(new EventHandler());
    MinecraftForge.EVENT_BUS.register(new ChestTickHandler());
    MinecraftForge.EVENT_BUS.register(new MouseHandler());
    MinecraftForge.EVENT_BUS.register(new UpdateHandler());
    ClientCommandHandler.instance.registerCommand(new BazaarToolsCommand());
    ScheduledEvents.create();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> Utils.saveConfigFile(configFile, modules.generateConfig().toString())));
  }
}
