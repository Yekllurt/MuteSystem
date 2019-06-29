package dev.yekllurt.mutesystem;

import dev.yekllurt.mutesystem.api.MuteSystemAPI;
import dev.yekllurt.mutesystem.command.*;
import dev.yekllurt.mutesystem.core.database.SQLDatabase;
import dev.yekllurt.mutesystem.core.database.SQLQueries;
import dev.yekllurt.mutesystem.core.languagesystem.LanguageSystem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MuteSystem extends JavaPlugin {

    private static MuteSystemAPI MUTE_SYSTEM_API;

    private SQLDatabase sqlDatabase;
    private LanguageSystem languageSystem;

    @Override
    public void onEnable() {
        FileManager fileManager = new FileManager(this);
        fileManager.copyReadme();
        fileManager.createConfiguration();
        fileManager.copyLanguages();

        FileConfiguration fileConfiguration = fileManager.getFileConfiguration();
        if (!fileConfiguration.getBoolean("configured")) {
            getLogger().severe("Shutting down MuteSytem. Reason: The configuration isn't completed.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        sqlDatabase = new SQLDatabase(this, fileConfiguration.getString("database.host"),
                fileConfiguration.getInt("database.port"), fileConfiguration.getString("database.database"),
                fileConfiguration.getString("database.username"), fileConfiguration.getString("database.password"),
                fileConfiguration.getBoolean("database.use_ssl"), fileConfiguration.getBoolean("database.require_ssl"));
        SQLDatabase.ConnectionAttemptResult result = sqlDatabase.connect();
        if (!SQLDatabase.successfulConnectionAttemptResult(result)) {
            getLogger().severe("Shutting down MuteSystem. Reason: Could not establish a database connection (" + result.toString() + ").");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        updateSQLQueries(fileConfiguration.getString("database.table_prefix"));
        executeDefaultQueries();

        languageSystem = new LanguageSystem(this, fileConfiguration.getString("language"));
        languageSystem.loadLanguages();

        MUTE_SYSTEM_API = new MuteSystemAPI(this, sqlDatabase, fileConfiguration.getLong("cache.cache_time"),
                fileConfiguration.getBoolean("cache.automatic_cache_clearing.activated"),
                fileConfiguration.getLong("cache.automatic_cache_clearing.interval"));
        MUTE_SYSTEM_API.stopAutomaticCacheClearing();

        Bukkit.getPluginManager().registerEvents(new AsyncPlayerChatListener(languageSystem), this);
        if(fileConfiguration.getBoolean("block_command_execution_when_muted")) {
            Bukkit.getPluginManager().registerEvents(new PlayerCommandPreprocessListener(languageSystem), this);
        }

        this.getCommand("mute").setExecutor(new MuteCommand(this, this.languageSystem));
        this.getCommand("unmute").setExecutor(new UnmuteCommand(this, languageSystem));
        this.getCommand("mutestatus").setExecutor(new MuteStatusCommand(this.languageSystem));
        this.getCommand("tempmute").setExecutor(new TemporaryMuteCommand(this, this.languageSystem));
        this.getCommand("mutehistory").setExecutor(new MuteHistoryCommand(this.languageSystem, fileConfiguration));
    }

    @Override
    public void onDisable() {
        if (languageSystem != null) languageSystem = null;
        if (MUTE_SYSTEM_API != null) MUTE_SYSTEM_API.stopAutomaticCacheClearing();

        if (sqlDatabase != null) {
            sqlDatabase.disconnect();
            sqlDatabase = null;
        }
    }

    public static MuteSystemAPI getMuteSystemAPI() {
        return MUTE_SYSTEM_API;
    }

    private void updateSQLQueries(String tablePrefix) {
        SQLQueries.CREATE_TABLE = SQLQueries.CREATE_TABLE.replaceAll("#", tablePrefix);

        SQLQueries.INSERT_PERMANENT = SQLQueries.INSERT_PERMANENT.replaceAll("#", tablePrefix);
        SQLQueries.INSERT_TEMPORARY = SQLQueries.INSERT_TEMPORARY.replaceAll("#", tablePrefix);
        SQLQueries.INSERT_UNMUTE = SQLQueries.INSERT_UNMUTE.replaceAll("#", tablePrefix);

        SQLQueries.GET_LATEST = SQLQueries.GET_LATEST.replaceAll("#", tablePrefix);
        SQLQueries.GET_ALL = SQLQueries.GET_ALL.replaceAll("#", tablePrefix);
        SQLQueries.GET_ALL_LIMIT = SQLQueries.GET_ALL_LIMIT.replaceAll("#", tablePrefix);
    }

    private void executeDefaultQueries() {
        this.sqlDatabase.executeUpdate(SQLQueries.CREATE_TABLE, new Object[0]);
    }

}
