package com.github.nitrogen2oxygen.SaveFileSync.utils;

import java.nio.file.Paths;

public class Constants {
    public static final String VERSION = "v0.3.0-alpha";
    public static final String APP_NAME = "Save File Sync";

    public static String dataFile() {
        return Paths.get(dataDirectory(), "data.ser").toString();
    }

    public static String dataDirectory() {
        return Paths.get(System.getProperty("user.home"), "SaveFileSync").toString();
    }
}
