package com.github.nitrogen2oxygen.savefilesync.client.theme;

import com.formdev.flatlaf.*;
import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.*;

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
            case MATERIAL_DARK:
                return "Material Dark";
            case MATERIAL_OCEAN:
                return "Material Oceanic";
            case MATERIAL_LIGHT:
                return "Material Lighter";
            case CARBON:
                return "Carbon";
            case ONE_DARK:
                return "One Dark";
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
            case MATERIAL_DARK:
                return new FlatMaterialDarkerIJTheme();
            case MATERIAL_OCEAN:
                return new FlatMaterialOceanicIJTheme();
            case MATERIAL_LIGHT:
                return new FlatMaterialLighterIJTheme();
            case CARBON:
                return new FlatCarbonIJTheme();
            case ONE_DARK:
                return new FlatAtomOneDarkIJTheme();
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
                break;
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
                break;
            case MATERIAL_DARK:
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
                break;
            case MATERIAL_OCEAN:
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
                break;
            case MATERIAL_LIGHT:
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
                break;
            case CARBON:
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
                break;
            case ONE_DARK:
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
                break;
        }
        return null;
    }
}
