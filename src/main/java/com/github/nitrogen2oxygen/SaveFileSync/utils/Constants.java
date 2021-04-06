package com.github.nitrogen2oxygen.SaveFileSync.utils;

import java.nio.file.Paths;

public class Constants {
    public static String APP_NAME = "Save File Sync";
    public static String VERSION = "v0.1.2-alpha";

    public static String dataFile() {
        return Paths.get(dataDirectory(), "data.json").toString();
    }

    public static String dataDirectory() {
        return Paths.get(System.getProperty("user.home"), "SaveFileSync").toString();
    }
}
