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
        if (StatusManager.isWhitelistEnabled()) {
            event.setPing(event.getPing().asBuilder()
                    .version(new ServerPing.Version(-99999, ""))
                    .maximumPlayers(0)
                    .description(MiniMessage.miniMessage().deserialize(config.get("motd-message")))
                    .build()
            );
        }
    }
}
