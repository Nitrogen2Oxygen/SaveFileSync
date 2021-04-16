package com.github.nitrogen2oxygen.savefilesync.client.themes;

import com.formdev.flatlaf.*;

import java.awt.*;

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

    public static Color getColor(Theme theme, ThemeColor color) {
        switch(theme) {
            case DARK:
                switch (color) {
                    case ERROR:
                        return Color.ORANGE;
                    case DEFAULT:
                        return Color.WHITE;
                    case OFFLINE:
                        return Color.RED;
                    case SUCCESS:
                        return Color.GREEN;
                    case WARNING:
                        return Color.YELLOW;
                }
            case LIGHT:
                switch (color) {
                    case ERROR:
                        return new Color(255, 140, 0);
                    case DEFAULT:
                        return Color.BLACK;
                    case OFFLINE:
                        return Color.RED;
                    case SUCCESS:
                        return new Color(0, 150, 0);
                    case WARNING:
                        return new Color(218, 165, 32);
                }
        }
        return null;
    }
}
