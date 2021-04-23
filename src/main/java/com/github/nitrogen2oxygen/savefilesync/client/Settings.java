package com.github.nitrogen2oxygen.savefilesync.client;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.github.nitrogen2oxygen.savefilesync.client.theme.Theme;
import com.github.nitrogen2oxygen.savefilesync.util.Themes;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class Settings {
    private Theme theme;
    private boolean makeBackups;
    private boolean forceOverwriteSave;

    public Settings(Properties properties) {
        try {
            theme = Theme.valueOf(properties.getProperty("theme"));
            makeBackups = Boolean.parseBoolean(properties.getProperty("make-backups"));
            forceOverwriteSave = Boolean.parseBoolean(properties.getProperty("force-overwrite"));
        } catch (Exception e) {
            int response = JOptionPane.showConfirmDialog(null, "There was an error reading the config file. Would you like to reset it?", "Error!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (response == 0) {
                setDefaults();
            } else {
                System.exit(1);
            }
        }
    }

    public Settings() {
        setDefaults();
    }

    public void setDefaults() {
        theme = Themes.getDefault();
        makeBackups = true;
        forceOverwriteSave = false;
    }

    public boolean shouldMakeBackups() {
        return makeBackups;
    }

    public void setMakeBackups(boolean makeBackups) {
        this.makeBackups = makeBackups;
    }

    public boolean shouldForceOverwrite() {
        return forceOverwriteSave;
    }

    public void setForceOverwrites(boolean forceOverwriteSave) {
        this.forceOverwriteSave = forceOverwriteSave;
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
        prop.setProperty("make-backups", String.valueOf(makeBackups));
        prop.setProperty("force-overwrite", String.valueOf(forceOverwriteSave));
        return prop;
    }
}
