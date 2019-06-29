package dev.yekllurt.mutesystem.command;

import dev.yekllurt.mutesystem.MuteSystem;
import dev.yekllurt.mutesystem.TimeUtility;
import dev.yekllurt.mutesystem.api.MuteEntry;
import dev.yekllurt.mutesystem.api.MuteEntryType;
import dev.yekllurt.mutesystem.core.MojangAPI;
import dev.yekllurt.mutesystem.core.languagesystem.LanguageSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuteStatusCommand implements CommandExecutor {

    private LanguageSystem languageSystem;

    public MuteStatusCommand(LanguageSystem languageSystem) {
        this.languageSystem = languageSystem;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        if (commandSender instanceof Player && !commandSender.hasPermission(Permission.MUTE_STATUS)) {
            commandSender.sendMessage(languageSystem.getMessage("command.not_enough_permissions", true));
            return true;
        }
        if (arguments.length != 1) {
            commandSender.sendMessage(languageSystem.getMessage("command.mute_status.wrong_syntax", true));
            return true;
        }
        MojangAPI.getUUID(arguments[0], (uuid) -> {
            if (uuid == null) {
                String message = languageSystem.getMessage("command.user_does_not_exist", true);
                message = message.replaceAll("%user%", arguments[0]);
                commandSender.sendMessage(message);
                return;
            }
            MuteSystem.getMuteSystemAPI().getMuteEntry(uuid, (result) -> {
                if (isMuted(result) == false) {
                    String message = languageSystem.getMessage("command.mute_status.is_not_muted", true);
                    message = message.replaceAll("%user%", arguments[0]);
                    commandSender.sendMessage(message);
                    return;
                }
                if (result.getType() == MuteEntryType.MUTE) {
                    String message = languageSystem.getMessage("command.mute_status.permanent_mute", true);
                    message = message.replaceAll("%user%", arguments[0]);
                    commandSender.sendMessage(message);
                    return;
                }
                if (result.getType() == MuteEntryType.TEMP_MUTE) {
                    String message = languageSystem.getMessage("command.mute_status.temporary_mute", true);
                    message = message.replaceAll("%user%", arguments[0]);
                    message = TimeUtility.replaceTimeVariables(languageSystem, result.getEndAsCalendar(), message);
                    commandSender.sendMessage(message);
                    return;
                }
            });
        });
        return true;
    }

    private boolean isMuted(MuteEntry muteEntry) {
        if (muteEntry == null) {
            return false;
        }
        if (muteEntry.getType() == MuteEntryType.UNMUTE) {
            return false;
        }
        if (muteEntry.getType() == MuteEntryType.MUTE) {
            return true;
        }
        if (muteEntry.getType() == MuteEntryType.TEMP_MUTE && TimeUtility.getCurrentDate().before(muteEntry.getEndAsDate())) {
            return true;
        }
        return false;
    }

}