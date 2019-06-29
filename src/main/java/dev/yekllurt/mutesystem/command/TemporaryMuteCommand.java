package dev.yekllurt.mutesystem.command;

import dev.yekllurt.mutesystem.MuteSystem;
import dev.yekllurt.mutesystem.TimeUtility;
import dev.yekllurt.mutesystem.core.MojangAPI;
import dev.yekllurt.mutesystem.core.languagesystem.LanguageSystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Calendar;
import java.util.UUID;
import java.util.regex.Pattern;

public class TemporaryMuteCommand implements CommandExecutor {

    private final JavaPlugin javaPlugin;
    private final LanguageSystem languageSystem;

    public TemporaryMuteCommand(JavaPlugin javaPlugin, LanguageSystem languageSystem) {
        this.javaPlugin = javaPlugin;
        this.languageSystem = languageSystem;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] arguments) {
        if (commandSender instanceof Player && !commandSender.hasPermission(Permission.TEMP_MUTE)) {
            commandSender.sendMessage(languageSystem.getMessage("command.not_enough_permissions", true));
            return true;
        }
        if (arguments.length < 2) {
            commandSender.sendMessage(languageSystem.getMessage("command.temporary_mute.wrong_syntax", true));
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
                if (result == true) {
                    String message = languageSystem.getMessage("command.player_is_already_muted", true);
                    message = message.replaceAll("%user%", arguments[0]);
                    commandSender.sendMessage(message);
                    return;
                }
                String executor = commandSender instanceof Player ? ((Player) commandSender).getUniqueId().toString()
                        : "CONSOLE";
                UUID target = uuid;
                String timeString = arguments[1];

                Calendar currentCalendar = TimeUtility.getCurrentCalendar();
                int lastCharacter = 0;
                for (int index = 0; index < timeString.length(); index++) {
                    if (Pattern.matches("[^0-9]", String.valueOf(timeString.charAt(index)))) {
                        char c = timeString.charAt(index);
                        if ("yMwdhm".indexOf(c) == -1) {
                            String message = languageSystem.getMessage("command.temporary_mute.unknown_time_unit",
                                    true);
                            message = message.replaceAll("%time_unit%", String.valueOf(c));
                            commandSender.sendMessage(message);
                            return;
                        }
                        int value = Integer.parseInt(timeString.substring(lastCharacter, index));
                        switch (c) {
                            case 'm':
                                currentCalendar.add(Calendar.MINUTE, value);
                                break;
                            case 'h':
                                currentCalendar.add(Calendar.HOUR_OF_DAY, value);
                                break;
                            case 'd':
                                currentCalendar.add(Calendar.DAY_OF_YEAR, value);
                                break;
                            case 'w':
                                currentCalendar.add(Calendar.WEEK_OF_YEAR, value);
                                break;
                            case 'M':
                                currentCalendar.add(Calendar.MONTH, value);
                                break;
                            case 'y':
                                currentCalendar.add(Calendar.YEAR, value);
                                break;
                        }
                        lastCharacter = index + 1;
                    }
                }

                StringBuilder reason = new StringBuilder();
                if (arguments.length > 2) {
                    for (int index = 2; index < arguments.length; index++) {
                        reason.append(arguments[index]);
                        if (index < arguments.length - 1) {
                            reason.append(" ");
                        }
                    }
                }
                MuteSystem.getMuteSystemAPI().mute(target, executor, reason.toString(), currentCalendar.getTime());
                String message = languageSystem.getMessage("command.temporary_mute.success", true);
                message = message.replaceAll("%user%", arguments[0]);
                message = TimeUtility.replaceTimeVariables(languageSystem, currentCalendar, message);
                commandSender.sendMessage(message);
            });
        });
        return true;
    }

}