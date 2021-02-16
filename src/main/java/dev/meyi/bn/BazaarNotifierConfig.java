package dev.meyi.bn;

import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.ModuleList;
import dev.meyi.bn.modules.ModuleName;
import dev.meyi.bn.utilities.Utils;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

public class BazaarNotifierConfig {
    public static final String OLD_PRIVATE_API_KEY_CONFIG_NAME = "api";
    public static final String PRIVATE_API_KEY_CONFIG_NAME = "privateApiKey";
    public static final String VERSION_CONFIG_NAME = "version";
    public static final String MODULES_CONFIG_NAME = "modules";

    private static File configFile;
    // TODO : Expose just the module jsonConfig and then make this private
    public static JSONObject jsonConfig = null;

    public static String version = BazaarNotifier.VERSION;
    public static String privateApiKey = "";

    public static String getCurrentConfig() {
        return new JSONObject().put(PRIVATE_API_KEY_CONFIG_NAME, privateApiKey)
                .put(VERSION_CONFIG_NAME, version)
                .put(MODULES_CONFIG_NAME, BazaarNotifier.modules.getCurrentModuleConfigs())
                .toString();
    }

    /**
     * This method:
     * 1. Loads the configuration from the Forge-provided suggested configuration file, if present.
     * 2. Check that the configuration is for the current version of the mod.
     * 3. Generates a new configuration if the config isn't present or is the wrong version.
     * 4. Creates and configures the module list using the loaded or created config.
     *
     * @param event The pre-initialization event info from Forge
     */
    public static void doConfigPreInit(FMLPreInitializationEvent event)
    {
        configFile = event.getSuggestedConfigurationFile();
        String privateApiKeyFromPreviousConfigVersion = "";

        try {
            if (configFile.isFile()) {
                String configFileContents;
                configFileContents = new String(Files.readAllBytes(Paths.get(configFile.getPath())));
                if (!Utils.isValidJSONObject(configFileContents)) {
                    throw new IOException("Configuration file is not JSON");
                }

                JSONObject loadedJsonConfig = new JSONObject(configFileContents);
                String loadedJsonConfigVersion = loadedJsonConfig.getString(VERSION_CONFIG_NAME);
                if (!loadedJsonConfigVersion.equalsIgnoreCase(BazaarNotifier.VERSION)) {
                    BazaarNotifier.logger.warn("Mod version change detected, resetting config. " +
                            "Config version: " + loadedJsonConfigVersion + "Mod version: " + BazaarNotifier.VERSION);

                    // attempt to persist the old API key
                    if (loadedJsonConfig.has(OLD_PRIVATE_API_KEY_CONFIG_NAME)) {
                        privateApiKeyFromPreviousConfigVersion =
                                loadedJsonConfig.getString(OLD_PRIVATE_API_KEY_CONFIG_NAME);
                    } else if (loadedJsonConfig.has(PRIVATE_API_KEY_CONFIG_NAME)) {
                        privateApiKeyFromPreviousConfigVersion =
                                loadedJsonConfig.getString(PRIVATE_API_KEY_CONFIG_NAME);
                    }
                } else {
                    jsonConfig = loadedJsonConfig;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Continue on and generate a new configuration
        }

        if (jsonConfig == null) {
            jsonConfig = initializeConfig(privateApiKeyFromPreviousConfigVersion);
        } else {
            if (jsonConfig.has(PRIVATE_API_KEY_CONFIG_NAME)) {
                privateApiKey = jsonConfig.getString(PRIVATE_API_KEY_CONFIG_NAME);
            }
        }
    }

    public static JSONObject initializeConfig(String previousPrivateApiKey) {
        JSONObject newConfig = new JSONObject()
                .put(PRIVATE_API_KEY_CONFIG_NAME, previousPrivateApiKey)
                .put(VERSION_CONFIG_NAME, BazaarNotifier.VERSION);

        JSONArray modules = new JSONArray();

        for (ModuleName value : ModuleName.values()) {
            Module m = value.returnDefaultModule();
            if (m != null) {
                modules.put(m.getCurrentModuleConfig());
            }
        }

        return newConfig.put(MODULES_CONFIG_NAME, modules);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveConfigFile() {
        try {
            if (!configFile.isFile()) {
                configFile.createNewFile();
            }
            Files.write(Paths.get(configFile.getAbsolutePath()),
                    getCurrentConfig().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
