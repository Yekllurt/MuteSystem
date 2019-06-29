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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MuteHistoryCommand implements CommandExecutor {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private LanguageSystem languageSystem;
    private FileConfiguration fileConfiguration;

    public MuteHistoryCommand(LanguageSystem languageSystem, FileConfiguration fileConfiguration) {
        this.languageSystem = languageSystem;
        this.fileConfiguration = fileConfiguration;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        if (commandSender instanceof Player && !commandSender.hasPermission(Permission.MUTE_HISTORY)) {
            commandSender.sendMessage(languageSystem.getMessage("command.not_enough_permissions", true));
            return true;
        }
        if (arguments.length != 1) {
            commandSender.sendMessage(languageSystem.getMessage("command.mute_history.wrong_syntax", true));
            return true;
        }
        MojangAPI.getUUID(arguments[0], (uuid) -> {
            if (uuid == null) {
                String message = languageSystem.getMessage("command.user_does_not_exist", true);
                message = message.replaceAll("%user%", arguments[0]);
                commandSender.sendMessage(message);
                return;
            }
            MuteSystem.getMuteSystemAPI().getAllMuteEntries(uuid, fileConfiguration.getInt("command.mute_history.display_limit"),
                    (result) -> {
                        String messageHeader = languageSystem.getMessage("command.mute_history.result.header", true);
                        messageHeader = messageHeader.replaceAll("%count%", String.valueOf(result.size()));
                        commandSender.sendMessage(messageHeader);
                        if (result.size() > 0) {
                            String messageTarget = languageSystem.getMessage("command.mute_history.result.target",
                                    true);
                            messageTarget = messageTarget.replaceAll("%target%", arguments[0]);
                            commandSender.sendMessage(messageTarget);
                            commandSender
                                    .sendMessage(languageSystem.getMessage("command.mute_history.result.format", true));
                            EXECUTOR_SERVICE.execute(() -> {
                                for (MuteEntry muteEntry : result) {
                                    String executor = muteEntry.getExecutor();
                                    if (executor.equals("CONSOLE")) {
                                        commandSender.sendMessage(generateMessage(muteEntry, executor));
                                    } else {
                                        commandSender.sendMessage(generateMessage(muteEntry, MojangAPI.getName(UUID.fromString(executor))));
                                    }
                                }
                            });
                        }
                    });
        });
        return true;
    }

    private String generateMessage(MuteEntry muteEntry, String executor) {
        String message = null;
        switch (muteEntry.getType()) {
            case MUTE:
                message = languageSystem.getMessage("command.mute_history.result.mute", true);
                message = message.replaceAll("%action%", languageSystem.getMessage("type.mute", true));
                break;
            case TEMP_MUTE:
                message = languageSystem.getMessage("command.mute_history.result.temporary_mute", true);
                message = message.replaceAll("%action%", languageSystem.getMessage("type.temporary_mute", true));
                break;
            case UNMUTE:
                message = languageSystem.getMessage("command.mute_history.result.unmute", true);
                message = message.replaceAll("%action%", languageSystem.getMessage("type.unmute", true));
                break;
        }
        message = message.replaceAll("%id%", String.valueOf(muteEntry.getId()));
        message = TimeUtility.replaceTimeVariables(languageSystem, muteEntry.getStartAsCalendar(), message);
        message = message.replaceAll("%executor%", executor);
        return message;
    }

}
