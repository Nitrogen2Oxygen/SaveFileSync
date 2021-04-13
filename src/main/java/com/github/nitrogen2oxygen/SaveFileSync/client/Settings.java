package com.github.nitrogen2oxygen.SaveFileSync.client;

import javax.swing.*;

public class Settings implements java.io.Serializable {
    private static final long serialVersionUID = 6143707056688916780L;
    public Theme theme;

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
        try {
            UIManager.setLookAndFeel(Themes.getThemeClass(theme));
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

}
