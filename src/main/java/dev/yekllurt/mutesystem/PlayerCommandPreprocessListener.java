package dev.yekllurt.mutesystem;

import dev.yekllurt.mutesystem.api.MuteEntry;
import dev.yekllurt.mutesystem.api.MuteEntryType;
import dev.yekllurt.mutesystem.core.languagesystem.LanguageSystem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocessListener implements Listener {

    private final LanguageSystem languageSystem;

    public PlayerCommandPreprocessListener(LanguageSystem languageSystem) {
        this.languageSystem = languageSystem;
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        MuteEntry muteEntry = MuteSystem.getMuteSystemAPI().getMuteEntry(event.getPlayer().getUniqueId());
        if (muteEntry == null) return;
        if (muteEntry.getType() == MuteEntryType.MUTE) {
            String message = languageSystem.getMessage("command.execution_not_allowed_when_muted", true);
            event.getPlayer().sendMessage(message);
            event.setCancelled(true);
            return;
        }
        if (muteEntry.getType() == MuteEntryType.TEMP_MUTE) {
            if (TimeUtility.getCurrentDate().before(muteEntry.getEndAsDate())) {
                String message = languageSystem.getMessage("command.execution_not_allowed_when_muted", true);
                message = TimeUtility.replaceTimeVariables(languageSystem, muteEntry.getEndAsCalendar(), message);
                event.getPlayer().sendMessage(message);
                event.setCancelled(true);
            }
            return;
        }
    }

}
