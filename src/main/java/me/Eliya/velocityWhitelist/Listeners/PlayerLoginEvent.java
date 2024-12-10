package me.Eliya.velocityWhitelist.Listeners;


import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import me.Eliya.velocityWhitelist.Managers.StatusManager;
import me.Eliya.velocityWhitelist.Managers.WhitelistManager;
import me.Eliya.velocityWhitelist.Utilities.ConfigProperties;
import me.Eliya.velocityWhitelist.VelocityWhitelist;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class PlayerLoginEvent {

    private final ConfigProperties config;

    public PlayerLoginEvent(ConfigProperties config) {
        this.config = config;
    }

    @Subscribe
    public void onPlayerLogin(LoginEvent event) {
        Player player = event.getPlayer();

        if (StatusManager.isWhitelistEnabled()) {
            if (!WhitelistManager.isPlayerWhitelisted(player.getUsername()) && !player.hasPermission((String) config.get("bypass-permission"))) {
                event.setResult(ResultedEvent.ComponentResult.denied(Component.text((String) config.get("kick-message"))));
                for (Player players : VelocityWhitelist.getProxy().getAllPlayers()) {
                    if (players.hasPermission((String) config.get("join-notifies-permission"))) {
                        String message = (String) config.get("join-notify-message") ;
                        players.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace("{player}", event.getPlayer().getUsername())));
                    }
                }
                return;
            }
            event.setResult(ResultedEvent.ComponentResult.allowed());
        }
    }
}
