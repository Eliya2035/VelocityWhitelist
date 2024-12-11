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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

        if (args.length == 0) {
            sender.sendMessage(color((String) config.get("usage-message")));
            return;
        }


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
            default:
                sender.sendMessage(color((String) config.get("usage-message")));
                break;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> completions = new ArrayList<>();
        List<String> options = new ArrayList<>();

        if (!invocation.source().hasPermission((String) config.get("permission")))
            return completions;

        if (args.length == 0 || args.length == 1) {
            options.add("on");
            options.add("off");
            options.add("add");
            options.add("remove");
            options.add("list");

            String input = args.length > 0 ? args[0].toLowerCase() : "";
            for (String option : options) {
                if (option.toLowerCase().startsWith(input)) {
                    completions.add(option);
                }
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                for (Player player : VelocityWhitelist.getProxy().getAllPlayers()) {
                    if (!WhitelistManager.isPlayerWhitelisted(player.getUsername()))
                        completions.add(player.getUsername());
                }
                String input = args.length > 1 ? args[1].toLowerCase() : "";
                for (String option : options) {
                    if (option.startsWith(input)) {
                        completions.add(option);
                    }
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                for (String playerName : WhitelistManager.getWhitelistedPlayers()) {
                    options.add(playerName);
                }
                String input = args.length > 1 ? args[1].toLowerCase() : "";
                for (String option : options) {
                    if (option.startsWith(input)) {
                        completions.add(option);
                    }
                }
            }
        }

        return completions;
    }



    protected void handleAddCMD(CommandSource sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(color((String) config.get("usage-message")));
            return;
        }

        String playerName = args[1];

        if (WhitelistManager.isPlayerWhitelisted(playerName)) {
            String message = (String) config.get("alreadywhitelisted-message");
            sender.sendMessage(color((message.replace("{player}", playerName))));
            return;
        }

        UUID playerUUID = WhitelistManager.resolveUUIDFromMojangAPI(playerName);

        if (playerUUID != null) {
            WhitelistManager.addPlayerToWhitelist(playerName);
            String message = (String) config.get("add-message");
            sender.sendMessage(color(message.replace("{player}", playerName)));
        } else {
            String message = (String) config.get("player-doesntexist");
            sender.sendMessage(color(message.replace("{player}", playerName)));
        }
    }


    protected void handleRemoveCMD(CommandSource sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(color((String) config.get("usage-message")));
            return;
        }

        String playerName = args[1];

        if (!WhitelistManager.isPlayerWhitelisted(playerName)) {
            String message = (String) config.get("player-not-whitelisted-message");
            sender.sendMessage(color(message.replace("{player}", playerName)));
            return;
        }
        WhitelistManager.removePlayerFromWhitelist(playerName);
        String message = (String) config.get("remove-message");
        Optional<Player> optionalPlayer = VelocityWhitelist.getProxy().getPlayer(playerName);
        if (optionalPlayer.isPresent() && optionalPlayer.get().isActive() && StatusManager.isWhitelistEnabled())
           optionalPlayer.get().disconnect(color((String) config.get("kick-message")));
        sender.sendMessage(color(message.replace("{player}", playerName)));
    }

    protected void handleListCMD(CommandSource sender) {
        JsonObject whitelistData = WhitelistManager.loadWhitelistData();

        if (whitelistData.isEmpty()) {
            String message = (String) config.get("empty-list-message");
            sender.sendMessage(color(message));
            return;
        }

        String headerMessage = (String) config.get("list-message");
        Component header = Component.text(headerMessage + "\n");

        Component playerList = Component.empty();
        for (String playerName : whitelistData.keySet()) {
            playerList = playerList
                    .append(Component.text(playerName).color(NamedTextColor.YELLOW))
                    .append(Component.newline());
        }

        Component result = header.append(playerList);
        sender.sendMessage(result);
    }

    protected void handleOnCMD(CommandSource sender) {
        if (!StatusManager.isWhitelistEnabled()) {
            StatusManager.setWhitelistEnabled(true);
            String message = (String) config.get("whitelist-enabled");
            sender.sendMessage(color(message));
            return;
        }
        String message = (String) config.get("whitelist-already-on");
        sender.sendMessage(color(message));
    }

    protected void handleOffCMD(CommandSource sender) {
        if (StatusManager.isWhitelistEnabled()) {
            StatusManager.setWhitelistEnabled(false);
            String message = (String) config.get("whitelist-disabled");
            sender.sendMessage(color(message));
            return;
        }
        String message = (String) config.get("whitelist-already-off");
        sender.sendMessage(color(message));
    }



    protected static Component color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
