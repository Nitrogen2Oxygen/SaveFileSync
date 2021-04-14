package com.github.nitrogen2oxygen.SaveFileSync.client;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class Settings {
    private Theme theme;

    public Settings(Properties properties) {
        theme = Theme.valueOf(properties.getProperty("theme"));
    }

    public Settings() {
        theme = Themes.getDefault();
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Theme getTheme() {
        return theme;
    }

    /* Applies the settings to the client */
    public void apply() {
        // Apply theme
            EventQueue.invokeLater(() -> {
                try {
                    FlatAnimatedLafChange.showSnapshot();

                    // Change look and feel
                    UIManager.setLookAndFeel(Themes.getThemeClass(theme));

                    // Reload the UI
                    FlatLaf.updateUI();
                    FlatAnimatedLafChange.hideSnapshotWithAnimation();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    public Properties toProperties() {
        Properties prop = new Properties();
        prop.setProperty("theme", String.valueOf(theme));
        return prop;
    }
}
