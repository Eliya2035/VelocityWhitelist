package me.Eliya.velocityWhitelist;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.Eliya.velocityWhitelist.Commands.WhitelistCMD;
import me.Eliya.velocityWhitelist.Listeners.PlayerLoginEvent;
import me.Eliya.velocityWhitelist.Listeners.ProxyPingEvent;
import me.Eliya.velocityWhitelist.Managers.StatusManager;
import me.Eliya.velocityWhitelist.Utilities.ConfigProperties;

import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin
        (id = "velocitywhitelist",
        name = "VelocityWhitelist",
        version = "1.0",
        authors = {"Eliya2035"}
        )

public class VelocityWhitelist {

    private static ProxyServer proxy;
    private final ConfigProperties configProperties;
    private final EventManager eventManager;
    private StatusManager statusManager;


    @Inject
    private Logger logger;

    @Inject
    public VelocityWhitelist(ProxyServer proxy, @DataDirectory Path dataDirectory, EventManager eventManager) {
        VelocityWhitelist.proxy = proxy;
        this.configProperties = new ConfigProperties(dataDirectory.resolve("config.yml"));
        this.eventManager = eventManager;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.configProperties.loadConfig();
        proxy.getCommandManager().register("vwhitelist", new WhitelistCMD(this.configProperties));
        eventManager.register(this, new PlayerLoginEvent(this.configProperties));
        eventManager.register(this, new ProxyPingEvent(this.configProperties));

        Object whitelistStatusObj = configProperties.get("whitelist-status");

        boolean whitelistStatus = false;

        if (whitelistStatusObj instanceof Boolean) {
            whitelistStatus = (Boolean) whitelistStatusObj;
        } else if (whitelistStatusObj instanceof String) {
            whitelistStatus = Boolean.parseBoolean((String) whitelistStatusObj);
        }

        statusManager = new StatusManager(this.configProperties, whitelistStatus);
    }

    public static ProxyServer getProxy() {
        return proxy;
    }
}
