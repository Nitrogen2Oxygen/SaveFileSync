package com.github.nitrogen2oxygen.SaveFileSync.utils;

import java.nio.file.Paths;

public class Constants {
    public static String dataDirectory() {
        return Paths.get(System.getProperty("user.home"), appName()).toString();
    }

    public static String appName() {
        return "SaveFileSync";
    }
}
