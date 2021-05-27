package com.github.nitrogen2oxygen.savefilesync.util;

import java.nio.file.Paths;

public class FileLocations {
    public static String getConfigFile(String parent) {
        return Paths.get(parent, "config.properties").toString();
    }

    public static String getServerFile(String parent) {
        return Paths.get(parent, "server.ser").toString();
    }

    public static String getSaveDirectory(String parent) {
        return Paths.get(parent, "saves").toString();
    }

    public static String getBackupDirectory(String parent) {
        return Paths.get(parent, "backups").toString();
    }

    public static String getDataDirectory() {
        return Paths.get(System.getProperty("user.home"), ".savefileSync").toString();
    }

    public static String getOldDataDirectory() {
        return Paths.get(System.getProperty("user.home"), "SaveFileSync").toString();
    }
}
