package me.Eliya.velocityWhitelist.Listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.server.ServerPing;
import me.Eliya.velocityWhitelist.Managers.StatusManager;
import me.Eliya.velocityWhitelist.Utilities.ConfigProperties;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ProxyPingEvent {
    private final ConfigProperties config;

    public ProxyPingEvent(ConfigProperties config) {
        this.config = config;
    }

    @Subscribe
    public void onProxyPing(com.velocitypowered.api.event.proxy.ProxyPingEvent event) {
        Boolean disablePlayerCount = (Boolean) config.get("disable-playercount");
        Boolean enabled = (Boolean) config.get("motd-enabled");
        if (disablePlayerCount == null) {
            disablePlayerCount = false;
        }
        if (enabled == null)
            enabled = false;

        if (StatusManager.isWhitelistEnabled() && enabled) {
            if (disablePlayerCount) {
                event.setPing(event.getPing().asBuilder()
                        .version(new ServerPing.Version(-99999, (String) config.get("ping-message")))
                        .maximumPlayers(0)
                        .description(MiniMessage.miniMessage().deserialize((String) config.get("motd-message")))
                        .build()
                );
            }
            else {
                event.setPing(event.getPing().asBuilder()
                        .maximumPlayers(0)
                        .description(MiniMessage.miniMessage().deserialize((String) config.get("motd-message")))
                        .build()
                );
            }
        }
    }
}
