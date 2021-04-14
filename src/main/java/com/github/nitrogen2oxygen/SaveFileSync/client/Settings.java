package com.github.nitrogen2oxygen.SaveFileSync.client;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;

public class Settings implements java.io.Serializable {
    private static final long serialVersionUID = 6143707056688916780L;
    private Theme theme;

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
}
