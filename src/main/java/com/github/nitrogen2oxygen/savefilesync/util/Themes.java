package com.github.nitrogen2oxygen.savefilesync.util;

import com.formdev.flatlaf.*;
import com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.*;
import com.github.nitrogen2oxygen.savefilesync.client.theme.Theme;
import com.github.nitrogen2oxygen.savefilesync.client.theme.ThemeColor;

import java.awt.*;

public class Themes {

    public static Theme getDefault() {
        return Theme.DARK;
    }

    public static String getName(Theme theme) {
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
            case CYAN_LIGHT:
                return "Cyan Light";
            case DARK_PURPLE:
                return "Dark Purple";
            default:
                return null;
        }
    }

    public static FlatLaf getTheme(Theme theme) {
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
            case CYAN_LIGHT:
                return new FlatCyanLightIJTheme();
            case DARK_PURPLE:
                return new FlatDarkPurpleIJTheme();
            default:
                return null;
        }
    }

    public static Color getColor(Theme theme, ThemeColor color) {
        switch(theme) {
            case DARK:
            case MATERIAL_DARK:
            case MATERIAL_OCEAN:
            case CARBON:
            case ONE_DARK:
            case DARK_PURPLE:
                return defaultDark(color);
            case LIGHT:
            case MATERIAL_LIGHT:
            case CYAN_LIGHT:
                return defaultLight(color);
        }
        return null;
    }

    private static Color defaultDark(ThemeColor color) {
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
        return null;
    }

    private static Color defaultLight(ThemeColor color) {
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
        return null;
    }
}
