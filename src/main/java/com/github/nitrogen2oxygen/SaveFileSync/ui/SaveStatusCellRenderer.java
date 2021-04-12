package com.github.nitrogen2oxygen.SaveFileSync.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SaveStatusCellRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 6933813177684306960L; // So my IDE stops yelling

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
            switch ((String) tableModel.getValueAt(row, col)) {
                case "Offline":
                    l.setForeground(Color.ORANGE);
                    break;
                case "Not Synced":
                    l.setForeground(Color.YELLOW);
                    break;
                case "Synced":
                    l.setForeground(Color.GREEN);
                    break;
                case "Error":
                    l.setForeground(Color.RED);
                    break;
                case "No Server":
                    l.setForeground(Color.WHITE);
                default:
                    l.setForeground(Color.LIGHT_GRAY);
            }

        return l;
    }
}
