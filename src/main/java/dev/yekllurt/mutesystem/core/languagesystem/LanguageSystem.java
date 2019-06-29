package dev.yekllurt.mutesystem.core.languagesystem;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.yekllurt.mutesystem.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class LanguageSystem {

    private static final JsonParser JSON_PARSER = new JsonParser();

    private final JavaPlugin javaPlugin;

    private final String defaultLanguage;
    private Map<String, Language> languages;

    public LanguageSystem(JavaPlugin javaPlugin, String defaultLanguage) {
        this.javaPlugin = javaPlugin;
        this.defaultLanguage = defaultLanguage;
    }

    public void loadLanguages() {
        this.languages = new HashMap<>();
        try {
            FileManager fileManager = new FileManager(javaPlugin);
            for (File file : fileManager.getLanguageFileDirectory().listFiles()) {
                String language = file.getName().substring(0, file.getName().lastIndexOf("."));
                try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
                    JsonElement result = JSON_PARSER.parse(inputStreamReader);
                    Map<String, String> messages = new HashMap<>();
                    Set<Map.Entry<String, JsonElement>> entries = result.getAsJsonObject().entrySet();
                    for (Map.Entry<String, JsonElement> entry : entries) {
                        messages.put(entry.getKey(), entry.getValue().getAsString());
                    }
                    this.languages.put(language, new Language(language, messages));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String message, boolean colorCodeReplace) {
        return getMessage(defaultLanguage, message, colorCodeReplace);
    }

    public String getMessage(String language, String message, boolean colorCodeReplace) {
        String result = languages.get(language).getMessage(message);
        if (colorCodeReplace) {
            result = ChatColor.translateAlternateColorCodes('&', result);
        }
        return result;
    }

    public Set<String> getLanguages() {
        return this.languages.keySet();
    }

}