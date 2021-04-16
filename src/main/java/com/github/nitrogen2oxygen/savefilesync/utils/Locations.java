package com.github.nitrogen2oxygen.savefilesync.utils;

import java.nio.file.Paths;

public class Locations {
    public static String getConfigFile() {
        return Paths.get(getDataDirectory(), "config.properties").toString();
    }

    public static String getServerFile() {
        return Paths.get(getDataDirectory(), "server.ser").toString();
    }

    public static String getSaveDirectory() {
        return Paths.get(getDataDirectory(), "saves").toString();
    }

    public static String getDataDirectory() {
        return Paths.get(System.getProperty("user.home"), "SaveFileSync").toString();
    }
}
