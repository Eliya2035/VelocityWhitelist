package me.Eliya.velocityWhitelist.Managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WhitelistManager {

    private static final File whitelistFile = new File("plugins/velocitywhitelist/whitelist.json");
    private static final Gson gson = new Gson();

    public static void addPlayerToWhitelist(String playerName) {
        JsonObject whitelistData = loadWhitelistData();
        String playerNameLower = playerName.toLowerCase();
        UUID playerUUID = getPlayerUUIDByName(playerNameLower);
        if (playerUUID == null) {
            playerUUID = resolveUUIDFromMojangAPI(playerName);
            if (playerUUID != null) {
                whitelistData.addProperty(playerNameLower, playerUUID.toString());
                saveWhitelistData(whitelistData);
            }
        }
    }

    public static void removePlayerFromWhitelist(String playerName) {
        JsonObject whitelistData = loadWhitelistData();
        String playerNameLower = playerName.toLowerCase();
        if (whitelistData.has(playerNameLower)) {
            whitelistData.remove(playerNameLower);
            saveWhitelistData(whitelistData);
        }
    }

    public static UUID getPlayerUUIDByName(String playerName) {
        JsonObject whitelistData = loadWhitelistData();
        String playerNameLower = playerName.toLowerCase();
        if (whitelistData.has(playerNameLower)) {
            String uuidString = whitelistData.get(playerNameLower).getAsString();
            if (!uuidString.isEmpty()) {
                return UUID.fromString(uuidString);
            }
        }
        return null;
    }

    public static boolean isPlayerWhitelisted(String playerName) {
        return getPlayerUUIDByName(playerName) != null;
    }

    public static List<String> getWhitelistedPlayers() {
        List<String> whitelistedPlayers = new ArrayList<>();

        JsonObject whitelistData = loadWhitelistData();
        for (String playerName : whitelistData.keySet()) {
            whitelistedPlayers.add(playerName);
        }

        return whitelistedPlayers;
    }

    public static JsonObject loadWhitelistData() {
        JsonObject whitelistData = new JsonObject();
        if (!whitelistFile.getParentFile().exists()) {
            whitelistFile.getParentFile().mkdirs();
        }

        if (!whitelistFile.exists()) {
            try {
                whitelistFile.createNewFile();
                saveWhitelistData(whitelistData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (FileReader reader = new FileReader(whitelistFile)) {
            whitelistData = gson.fromJson(reader, JsonObject.class);
            if (whitelistData == null) {
                whitelistData = new JsonObject();
            }
        } catch (IOException e) {
            e.printStackTrace();
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

            if (connection.getResponseCode() == 404) {
                return null;
            }

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String uuidString = json.get("id").getAsString();

            return UUID.fromString(uuidString.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
