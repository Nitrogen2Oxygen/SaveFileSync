package com.github.nitrogen2oxygen.savefilesync.ui.renderer;

import com.github.nitrogen2oxygen.savefilesync.client.ClientData;
import com.github.nitrogen2oxygen.savefilesync.client.Save;
import com.github.nitrogen2oxygen.savefilesync.client.theme.ThemeColor;
import com.github.nitrogen2oxygen.savefilesync.util.Themes;
import com.github.nitrogen2oxygen.savefilesync.util.FileUtilities;
import org.ocpsoft.prettytime.PrettyTime;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class BackupStatusCellRenderer extends DefaultTableCellRenderer {
    private final ClientData data;

    public BackupStatusCellRenderer(ClientData data) {
        super();
        this.data = data;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

        // Get Save
        String name = (String) table.getValueAt(row, 0);
        Save save = data.getSaves().get(name);

        label.setForeground(Themes.getColor(data.getSettings().getTheme(), ThemeColor.DEFAULT));
            // Get backup file
            File backup = FileUtilities.getBackup(save);
            if (backup == null) {
                label.setText("No backup");
            } else {
                try {
                    BasicFileAttributes attr = Files.readAttributes(backup.toPath(), BasicFileAttributes.class);
                    FileTime fileTime = attr.creationTime();
                    PrettyTime prettyTime = new PrettyTime();
                    label.setText(prettyTime.format(fileTime.toInstant()));
                } catch (IOException e) {
                    e.printStackTrace();
                    label.setText("Error");
                }
            }

        return label;
    }
}
