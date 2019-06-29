package dev.yekllurt.mutesystem.core.database;

import dev.yekllurt.mutesystem.api.MuteEntryType;

public class SQLQueries {

    public static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `#_mutesystem` ( `id` INT NOT NULL AUTO_INCREMENT , `type` VARCHAR(16) NOT NULL, `target` VARCHAR(36) NOT NULL , `executor` VARCHAR(36) NOT NULL , `reason` VARCHAR(2048) NULL , `start` DATETIME NULL , `end` DATETIME NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;";

    public static String INSERT_PERMANENT = "INSERT INTO `#_mutesystem`(`type`, `target`, `executor`, `reason`, `start`) VALUES ('" + MuteEntryType.MUTE.getDatabaseName() + "', ?, ?, ?, ?);";
    public static String INSERT_TEMPORARY = "INSERT INTO `#_mutesystem`(`type`, `target`, `executor`, `reason`, `start`, `end`) VALUES ('" + MuteEntryType.TEMP_MUTE.getDatabaseName() + "', ?, ?, ?, ?, ?);";
    public static String INSERT_UNMUTE = "INSERT INTO `#_mutesystem`(`type`, `target`, `executor`, `start`) VALUES ('" + MuteEntryType.UNMUTE.getDatabaseName() + "', ?, ?, ?);";

    public static String GET_LATEST = "SELECT * FROM `#_mutesystem` WHERE `id` = (SELECT MAX(`id`) FROM `#_mutesystem` WHERE `target` = ?);";
    public static String GET_ALL = "SELECT * FROM `#_mutesystem` WHERE `target` = ? ORDER BY `id` ASC;";
    public static String GET_ALL_LIMIT = "SELECT * FROM (SELECT * FROM `#_mutesystem` WHERE `target` = ? ORDER BY `id` DESC LIMIT ?)result ORDER BY `id` ASC;";

}
