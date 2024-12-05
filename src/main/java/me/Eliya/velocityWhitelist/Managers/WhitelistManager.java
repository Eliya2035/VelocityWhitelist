package me.Eliya.velocityWhitelist.Managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class WhitelistManager {

    private static final File whitelistFile = new File("plugins/VelocityWhitelist/whitelist.json");
    private static final Gson gson = new Gson();

    public static void addPlayerToWhitelist(String playerName) {
        JsonObject whitelistData = loadWhitelistData();

        whitelistData.addProperty(playerName, "");

        saveWhitelistData(whitelistData);
    }

    public static void removePlayerFromWhitelist(String playerName) {
        JsonObject whitelistData = loadWhitelistData();

        if (whitelistData.has(playerName)) {
            whitelistData.remove(playerName);
            saveWhitelistData(whitelistData);
        }
    }

    public static UUID getPlayerUUIDByName(String playerName) {
        JsonObject whitelistData = loadWhitelistData();

        if (whitelistData.has(playerName)) {
            String uuidString = whitelistData.get(playerName).getAsString();
            if (uuidString.isEmpty()) {
                UUID resolvedUUID = resolveUUIDFromMojangAPI(playerName);
                if (resolvedUUID != null) {
                    whitelistData.addProperty(playerName, resolvedUUID.toString());
                    saveWhitelistData(whitelistData);
                    return resolvedUUID;
                }
            } else {
                return UUID.fromString(uuidString);
            }
        }
        return null;
    }

    public static boolean isPlayerWhitelisted(String playerName) {
        return getPlayerUUIDByName(playerName) != null;
    }

    public static JsonObject loadWhitelistData() {
        JsonObject whitelistData = new JsonObject();
        if (whitelistFile.exists()) {
            try (FileReader reader = new FileReader(whitelistFile)) {
                whitelistData = gson.fromJson(reader, JsonObject.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return whitelistData;
    }

    private static void saveWhitelistData(JsonObject whitelistData) {
        try (FileWriter writer = new FileWriter(whitelistFile)) {
            gson.toJson(whitelistData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static UUID resolveUUIDFromMojangAPI(String playerName) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            if (connection.getResponseCode() == 404) {
                return null;
            }

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String uuidString = json.get("id").getAsString();

            return UUID.fromString(uuidString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
