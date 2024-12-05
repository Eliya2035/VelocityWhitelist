package me.Eliya.velocityWhitelist.Commands;

import com.google.gson.JsonObject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.Eliya.velocityWhitelist.Managers.StatusManager;
import me.Eliya.velocityWhitelist.Managers.WhitelistManager;
import me.Eliya.velocityWhitelist.Utilities.ConfigProperties;
import me.Eliya.velocityWhitelist.VelocityWhitelist;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WhitelistCMD implements SimpleCommand {
    private final ConfigProperties config;

    public WhitelistCMD(ConfigProperties configProperties) {
        this.config = configProperties;
    }

    @Override
    public void execute(Invocation invocation) {

        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if (!sender.hasPermission((String) config.get("permission")))
            return;

        switch (args[0]) {
            case "on":
                handleOnCMD(sender);
                break;
            case "off":
                handleOffCMD(sender);
                break;
            case "add":
                handleAddCMD(sender, args);
                break;
            case "remove":
                handleRemoveCMD(sender, args);
                break;
            case "list":
                handleListCMD(sender);
                break;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> completions = new ArrayList<>();

        if (args.length == 2 && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
            for (Player player : VelocityWhitelist.getProxy().getAllPlayers()) {
                completions.add(player.getUsername());
            }
        }
        if (args.length == 1) {
            completions.add("on");
            completions.add("off");
            completions.add("list");
        }

        return completions;
    }


    protected void handleAddCMD(CommandSource sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(color(String.valueOf(Component.text((String) config.get("usage-message")))));
            return;
        }

        String playerName = args[1];

        if (WhitelistManager.isPlayerWhitelisted(playerName)) {
            String message = (String) config.get("alreadywhitelisted-message");
            sender.sendMessage(color(String.valueOf(Component.text(message.replace("{player}", playerName)))));
            return;
        }

        UUID playerUUID = WhitelistManager.resolveUUIDFromMojangAPI(playerName);

        if (playerUUID != null) {
            WhitelistManager.addPlayerToWhitelist(playerName);
            String message = (String) config.get("add-message");
            sender.sendMessage(color(String.valueOf(Component.text(message.replace("{player}", playerName)))));
        } else {
            String message = (String) config.get("player-doesntexist");
            sender.sendMessage(color(String.valueOf(Component.text(message.replace("{player}", playerName)))));
        }
    }



    protected void handleRemoveCMD(CommandSource sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(color(String.valueOf(Component.text((String) config.get("usage-message")))));
            return;
        }

        String playerName = args[1];

        if (!WhitelistManager.isPlayerWhitelisted(playerName)) {
            String message = (String) config.get("player-not-whitelisted-message");
            sender.sendMessage(color(String.valueOf(Component.text(message.replace("{player}", playerName)))));
            return;
        }
        WhitelistManager.removePlayerFromWhitelist(playerName);
        String message = (String) config.get("remove-message");
        sender.sendMessage(color(String.valueOf(Component.text(message.replace("{player}", playerName)))));
    }

    protected void handleListCMD(CommandSource sender) {
        JsonObject whitelistData = WhitelistManager.loadWhitelistData();

        if (whitelistData.isEmpty()) {
            String message = (String) config.get("empty-list-message");
            sender.sendMessage(color(String.valueOf(Component.text(message))));
            return;
        }

        String message = (String) config.get("list-message");
        StringBuilder whitelistedPlayers = new StringBuilder(String.valueOf(color(message)));

        for (String playerName : whitelistData.keySet()) {
            whitelistedPlayers.append(color("#FFE300"+playerName)).append(color("#FFE300"+"\n"));
        }

        sender.sendMessage(Component.text(whitelistedPlayers.toString()));
    }

    protected void handleOnCMD(CommandSource sender) {
        if (!StatusManager.isWhitelistEnabled()) {
            StatusManager.setWhitelistEnabled(true);
            String message = (String) config.get("whitelist-enabled");
            sender.sendMessage(color(String.valueOf(Component.text(message))));
            return;
        }
        String message = (String) config.get("whitelist-already-on");
        sender.sendMessage(color(String.valueOf(Component.text(message))));
    }

    protected void handleOffCMD(CommandSource sender) {
        if (StatusManager.isWhitelistEnabled()) {
            StatusManager.setWhitelistEnabled(false);
            String message = (String) config.get("whitelist-disabled");
            sender.sendMessage(color(String.valueOf(Component.text(message))));
            return;
        }
        String message = (String) config.get("whitelist-already-off");
        sender.sendMessage(color(String.valueOf(Component.text(message))));
    }


    protected static Component color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
