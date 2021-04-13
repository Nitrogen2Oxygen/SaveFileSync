package com.github.nitrogen2oxygen.SaveFileSync.client;

import com.formdev.flatlaf.FlatLaf;
import com.github.nitrogen2oxygen.SaveFileSync.App;

import javax.swing.*;
import java.awt.*;

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
                    UIManager.setLookAndFeel(Themes.getThemeClass(theme));
                    FlatLaf.updateUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }
}
