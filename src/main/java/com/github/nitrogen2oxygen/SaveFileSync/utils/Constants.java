package com.github.nitrogen2oxygen.SaveFileSync.utils;

import java.nio.file.Paths;

public class Constants {
    public static final String VERSION = "v0.3.0-alpha";
    public static final String APP_NAME = "Save File Sync";
    public static final String DROPBOX_APP_ID = "i136jjbqxg4aaci";


    public static String getDataFile() {
        return Paths.get(getDataDirectory(), "data.ser").toString();
    }

    public static String getDataDirectory() {
        return Paths.get(System.getProperty("user.home"), "SaveFileSync").toString();
    }
}
