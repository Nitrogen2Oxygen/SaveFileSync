package com.github.nitrogen2oxygen.SaveFileSync.utils;

import java.nio.file.Paths;

public class Constants {
    public static final String VERSION = "v0.3.2-alpha";
    public static final String APP_NAME = "Save File Sync";
    public static final String DROPBOX_APP_ID = "i136jjbqxg4aaci";

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
