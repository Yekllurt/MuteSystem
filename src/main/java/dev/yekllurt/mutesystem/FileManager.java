package dev.yekllurt.mutesystem;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class FileManager {

    public static final String PLUGIN_DIRECTORY = "plugins/MuteSystem";
    public static final String CONFIGURATION_FILE = PLUGIN_DIRECTORY + "/config.yml";
    public static final String LANGUAGE_DIRECTORY = PLUGIN_DIRECTORY + "/languages";
    public static final String[] DEFAULT_SUPPORTED_LANGUAGES = {"en_US", "de_DE", "lu_LU"};

    private final JavaPlugin javaPlugin;

    public FileManager(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    private File getConfigurationFile() {
        return new File(CONFIGURATION_FILE);
    }

    public FileConfiguration getFileConfiguration() {
        return YamlConfiguration.loadConfiguration(getConfigurationFile());
    }

    public void createConfiguration() {
        FileConfiguration fileConfiguration = getFileConfiguration();
        fileConfiguration.options().copyDefaults(true);
        fileConfiguration.addDefault("configured", false);

        fileConfiguration.addDefault("database.host", "localhost");
        fileConfiguration.addDefault("database.port", 3306);
        fileConfiguration.addDefault("database.username", "root");
        fileConfiguration.addDefault("database.password", "");
        fileConfiguration.addDefault("database.database", "database");
        fileConfiguration.addDefault("database.table_prefix", "mutesystem");
        fileConfiguration.addDefault("database.use_ssl", true);
        fileConfiguration.addDefault("database.require_ssl", false);

        fileConfiguration.addDefault("cache.cache_time", 20);
        fileConfiguration.addDefault("cache.automatic_cache_clearing.activated", true);
        fileConfiguration.addDefault("cache.automatic_cache_clearing.interval", 300);

        fileConfiguration.addDefault("language", "en_US");

        fileConfiguration.addDefault("command.mute_history.display_limit", 10);

        fileConfiguration.addDefault("block_command_execution_when_muted", false);
        try {
            fileConfiguration.save(getConfigurationFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getLanguageFileDirectory() {
        return new File(LANGUAGE_DIRECTORY);
    }

    public void copyReadme() {
        this.javaPlugin.saveResource("read.me", true);
    }

    public void copyLanguages() {
        File directory = getLanguageFileDirectory();
        directory.mkdirs();
        for (String language : DEFAULT_SUPPORTED_LANGUAGES) {
            this.javaPlugin.saveResource("languages/" + language + ".json", true);
        }
    }

}
