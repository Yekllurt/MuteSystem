package dev.yekllurt.mutesystem.command;

import dev.yekllurt.mutesystem.MuteSystem;
import dev.yekllurt.mutesystem.core.MojangAPI;
import dev.yekllurt.mutesystem.core.languagesystem.LanguageSystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class UnmuteCommand implements CommandExecutor {

    private final JavaPlugin javaPlugin;
    private final LanguageSystem languageSystem;

    public UnmuteCommand(JavaPlugin javaPlugin, LanguageSystem languageSystem) {
        this.javaPlugin = javaPlugin;
        this.languageSystem = languageSystem;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        if (commandSender instanceof Player && !commandSender.hasPermission(Permission.UNMUTE)) {
            commandSender.sendMessage(languageSystem.getMessage("command.not_enough_permissions", true));
            return true;
        }
        if (arguments.length != 1) {
            commandSender.sendMessage(languageSystem.getMessage("command.unmute.wrong_syntax", true));
            return true;
        }
        MojangAPI.getUUID(arguments[0], (uuid) -> {
            if (uuid == null) {
                String message = languageSystem.getMessage("command.user_does_not_exist", true);
                message = message.replaceAll("%user%", arguments[0]);
                commandSender.sendMessage(message);
                return;
            }
            MuteSystem.getMuteSystemAPI().isMuted(uuid, (result) -> {
                if (result == false) {
                    String message = languageSystem.getMessage("command.player_is_not_muted", true);
                    message = message.replaceAll("%user%", arguments[0]);
                    commandSender.sendMessage(message);
                    return;
                }
                String executor = commandSender instanceof Player ? ((Player) commandSender).getUniqueId().toString()
                        : "CONSOLE";
                UUID target = uuid;
                MuteSystem.getMuteSystemAPI().unmute(target, executor);
                String message = languageSystem.getMessage("command.unmute.success", true);
                message = message.replaceAll("%user%", arguments[0]);
                commandSender.sendMessage(message);
            });
        });
        return true;
    }

}
