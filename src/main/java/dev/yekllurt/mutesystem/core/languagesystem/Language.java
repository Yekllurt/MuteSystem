package dev.yekllurt.mutesystem.core.languagesystem;

import java.util.Map;

public class Language {

    private final String language;
    private final String regionDerivation;
    private final Map<String, String> messages;

    public Language(String language, Map<String, String> messages) {
        this.language = language.split("_")[0];
        this.regionDerivation = language.split("_")[1];
        this.messages = messages;
    }

    public String getLanguage() {
        return this.language;
    }

    public Map<String, String> getMessages() {
        return this.messages;
    }

    public String getRegionDerivation() {
        return this.regionDerivation;
    }

    public String getMessage(String message) {
        return this.messages.get(message);
    }

}
