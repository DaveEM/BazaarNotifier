package dev.meyi.bn;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HypixelApiWrapper {
    // Change this if an api key is ever required to access the bazaar again.
    public static boolean apiKeyRequiredForBazaar = false;

    private static String privateApiKey = "";
    private static boolean validPrivateApiKey = false;

    public static boolean isPrivateApiAvailable() { return validPrivateApiKey; }
    public static boolean isBazaarApiAvailable() { return true; }

    /**
     * Sets a new API key for the Hypixel private API including validating that it works.
     * @param newApiKey The new API key
     * @return true if the new API key is valid or was the same as the old one
     */
    public static boolean setPrivateApiKey(String newApiKey) {
        try {
            if (privateApiKey != "" && privateApiKey == newApiKey) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                        BazaarNotifier.prefix + EnumChatFormatting.RED
                                + "The new key is the same as your existing key"));
                return true;
            }

            if (isPrivateApiKeyValid(newApiKey)) {
                privateApiKey = newApiKey;
                return true;
            } else {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                        BazaarNotifier.prefix + EnumChatFormatting.RED
                                + "The new key is the same as your existing key"));
                return false;
            }
        }
        catch (IOException ioException) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                    BazaarNotifier.prefix + EnumChatFormatting.RED
                            + "Exception validating API key: " +
                            ioException.toString()
                    ));
            return false;
        }
    }

    private static boolean isPrivateApiKeyValid(String apiKey) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            return false;
        }

        return new JSONObject(IOUtils.toString(new BufferedReader
                (new InputStreamReader(
                        HttpClientBuilder.create().build().execute(new HttpGet(
                                "https://api.hypixel.net/key?key=" + apiKey)).getEntity()
                                .getContent())))).getBoolean("success");
    }

    public static JSONObject getBazaarData() throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        String apiBit = "";
        if (apiKeyRequiredForBazaar) {
            apiBit = "?key=" + privateApiKey;
        }
        HttpGet request = new HttpGet(
                "https://api.hypixel.net/skyblock/bazaar" + apiBit);
        HttpResponse response = client.execute(request);

        String result = IOUtils.toString(new BufferedReader
                (new InputStreamReader(
                        response.getEntity().getContent())));

        return new JSONObject(result).getJSONObject("products");
    }
}
