package me.Eliya.velocityWhitelist.Managers;

import com.velocitypowered.api.proxy.Player;
import me.Eliya.velocityWhitelist.Utilities.ConfigProperties;
import me.Eliya.velocityWhitelist.VelocityWhitelist;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class StatusManager {

    private static ConfigProperties config;
    private static boolean whitelistEnabled;

    public StatusManager(ConfigProperties config, boolean whitelistEnabled) {
        StatusManager.config = config;
        StatusManager.whitelistEnabled = whitelistEnabled;
    }
    public static void setWhitelistEnabled(boolean whitelistEnabled) {
        if (config == null) {
            throw new IllegalStateException("Config not initialized");
        }

        config.set("whitelist-status", whitelistEnabled);
        config.saveConfig();
        StatusManager.whitelistEnabled = whitelistEnabled;

        if (whitelistEnabled) {
            for (Player player : VelocityWhitelist.getProxy().getAllPlayers()) {
                if (!WhitelistManager.isPlayerWhitelisted(player.getUsername()) && !player.hasPermission((String) config.get("bypass-permission"))) {
                    player.disconnect(color((String) config.get("kick-message")));
                }
            }
        }
    }

    public static boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    protected static Component color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
