package dev.yekllurt.mutesystem.api;

public enum MuteEntryType {

    MUTE("mute"), TEMP_MUTE("tempmute"), UNMUTE("unmute");

    private String databaseName;

    MuteEntryType(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public static MuteEntryType fromDatabaseName(String databaseName) {
        for (MuteEntryType type : values()) {
            if (type.getDatabaseName().equals(databaseName)) {
                return type;
            }
        }
        return null;
    }

}
