package me.Eliya.velocityWhitelist.Listeners;


import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import me.Eliya.velocityWhitelist.Managers.StatusManager;
import me.Eliya.velocityWhitelist.Managers.WhitelistManager;
import me.Eliya.velocityWhitelist.Utilities.ConfigProperties;
import net.kyori.adventure.text.Component;

public class PlayerLoginEvent {

    private final ConfigProperties config;

    public PlayerLoginEvent(ConfigProperties config) {
        this.config = config;
    }

    @Subscribe
    public void onPlayerLogin(LoginEvent event) {
        Player player = event.getPlayer();

        if (StatusManager.isWhitelistEnabled()) {
            if (!WhitelistManager.isPlayerWhitelisted(player.getUsername()) && !player.hasPermission(config.get("bypass-permission"))) {
                event.setResult(ResultedEvent.ComponentResult.denied(Component.text(config.get("kick-message"))));
                return;
            }
            event.setResult(ResultedEvent.ComponentResult.allowed());
        }
    }
}
