package com.github.nitrogen2oxygen.SaveFileSync.client;

import com.formdev.flatlaf.*;

public class Themes {

    public static Theme getDefault() {
        return Theme.DARK;
    }

    public static String getThemeName(Theme theme) {
        switch (theme) {
            case DARK:
                return "Dark";
            case LIGHT:
                return "Light";
            default:
                return null;
        }
    }

    public static FlatLaf getThemeClass(Theme theme) {
        switch (theme) {
            case DARK:
                return new FlatDarkLaf();
            case LIGHT:
                return new FlatLightLaf();
            default:
                return null;
        }
    }
}
