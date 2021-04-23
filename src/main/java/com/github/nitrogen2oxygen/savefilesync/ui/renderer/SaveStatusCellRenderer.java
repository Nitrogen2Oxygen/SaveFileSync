package com.github.nitrogen2oxygen.savefilesync.ui.renderer;

import com.github.nitrogen2oxygen.savefilesync.client.theme.Theme;
import com.github.nitrogen2oxygen.savefilesync.client.theme.ThemeColor;
import com.github.nitrogen2oxygen.savefilesync.util.Themes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SaveStatusCellRenderer extends DefaultTableCellRenderer {
    private final Theme theme;

    public SaveStatusCellRenderer(Theme theme) {
        super();
        this.theme = theme;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
            switch ((String) tableModel.getValueAt(row, col)) {
                case "Offline":
                    l.setForeground(Themes.getColor(theme, ThemeColor.OFFLINE));
                    break;
                case "Not Synced":
                    l.setForeground(Themes.getColor(theme, ThemeColor.WARNING));
                    break;
                case "Synced":
                    l.setForeground(Themes.getColor(theme, ThemeColor.SUCCESS));
                    break;
                case "Error":
                    l.setForeground(Themes.getColor(theme, ThemeColor.ERROR));
                    break;
                default:
                    l.setForeground(Themes.getColor(theme, ThemeColor.DEFAULT));
            }

        return l;
    }
}
