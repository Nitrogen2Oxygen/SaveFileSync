package com.github.nitrogen2oxygen.savefilesync.ui;

import com.github.nitrogen2oxygen.savefilesync.client.ClientData;
import com.github.nitrogen2oxygen.savefilesync.client.theme.ThemeColor;
import com.github.nitrogen2oxygen.savefilesync.client.theme.Themes;
import com.github.nitrogen2oxygen.savefilesync.server.DataServers;
import com.github.nitrogen2oxygen.savefilesync.ui.renderer.SaveStatusCellRenderer;
import com.github.nitrogen2oxygen.savefilesync.client.Save;
import com.github.nitrogen2oxygen.savefilesync.util.FileUtilities;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipFile;

public class MainPanel {
    private JPanel rootPanel;
    private JButton newSaveFile;
    private JButton importButton;
    private JButton exportButton;
    private JTable saveList;
    private JButton manageServerButton;
    private JLabel dataServerLabel;
    private JLabel serverStatus;
    private JButton importFromServerButton;
    private JButton removeButton;
    private JButton editButton;
    private JLabel hostNameField;
    private JLabel serverTypeField;
    private JButton settingsButton;
    private JLabel statusLabel;
    private JLabel hostLabel;
    private JLabel typeLabel;
    private JButton restoreBackupButton;

    private static Thread reloadThread;
    private final ClientData data;
    private static final Font dataLabelFont = new Font("Segoe", Font.PLAIN, 16);
    private static final Font dataServerLabelFont = new Font("Segoe", Font.PLAIN, 24);
    private static final String[] saveListHeaders = new String[]{
            "Name",
            "Location",
            "Status"
    };

    public MainPanel(ClientData userData) {
        data = userData;
        data.getSettings().apply(); // Apply the settings object at startup

        /* Set fonts (due to weird theming bug) */
        dataServerLabel.setFont(dataServerLabelFont);
        typeLabel.setFont(dataLabelFont);
        hostLabel.setFont(dataLabelFont);
        statusLabel.setFont(dataLabelFont);
        serverStatus.setFont(dataLabelFont);
        serverTypeField.setFont(dataLabelFont);
        hostNameField.setFont(dataLabelFont);


        /* Create blank data table */
        DefaultTableModel dtm = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        }; // Prevents the cells from being moved
        dtm.setColumnIdentifiers(saveListHeaders);
        saveList.setModel(dtm);
        saveList.getColumnModel().getColumn(2).setCellRenderer(new SaveStatusCellRenderer(data.getSettings().getTheme())); // Render the status column with color
        saveList.getTableHeader().setReorderingAllowed(false); // Prevents the table columns from being reordered

        /* Load the UI */
        reloadUI();

        /* Standard action listeners */
        saveList.getSelectionModel().addListSelectionListener(e -> {
            // Enable/disable buttons
            if (saveList.getSelectedRows().length != 0) {
                boolean canExport = false;
                for (int i : saveList.getSelectedRows()) {
                    String status = (String) saveList.getValueAt(i, 2);
                    if (status.equals("Not Synced")) {
                        canExport = true;
                        break;
                    }
                }
                importButton.setEnabled(canExport);
                exportButton.setEnabled(canExport);
            }
            if (saveList.getSelectedRows().length == 1) {
                removeButton.setEnabled(true);
                editButton.setEnabled(true);
                int selected = saveList.getSelectedRow();
                String name = (String) saveList.getValueAt(selected, 0);
                Save save = data.getSaves().get(name);
                restoreBackupButton.setEnabled(FileUtilities.hasBackup(save));
            }
        });

        /* Button events */
        newSaveFile.addActionListener(e -> ButtonEvents.newSaveFile(data, this));
        manageServerButton.addActionListener(e -> ButtonEvents.manageServer(data, this));
        exportButton.addActionListener(e -> ButtonEvents.exportSaves(data, this));
        importButton.addActionListener(e -> ButtonEvents.importSaves(data, this));
        importFromServerButton.addActionListener(e -> ButtonEvents.serverImport(data, this));
        removeButton.addActionListener(e -> ButtonEvents.removeSave(data, this));
        editButton.addActionListener(e -> ButtonEvents.editSave(data, this));
        settingsButton.addActionListener(e -> ButtonEvents.changeSettings(data, this));
        restoreBackupButton.addActionListener(e -> ButtonEvents.restoreBackup(data, this));
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public JTable getSaveList() {
        return saveList;
    }

    /* We reload the UI on a separate thread to prevent any kind of freezing */
    public void reloadUI() {
        if (reloadThread != null && reloadThread.isAlive()) {
            reloadThread.interrupt();
        }

        reloadThread = new Thread(() -> {
            serverStatus.setText("Connecting...");
            serverStatus.setForeground(Themes.getColor(data.getSettings().getTheme(), ThemeColor.DEFAULT));
            hostNameField.setText(data.getServer() != null ? data.getServer().getHostName() : "None");
            serverTypeField.setText(data.getServer() != null ? DataServers.getDisplayName(data.getServer().getServerType()) : "None");
            // Set server status
            Boolean serverOnline;
            if (data.getServer() != null) {
                boolean status = data.getServer().verifyServer();
                if (status) {
                    serverOnline = true;
                    serverStatus.setText("Online");
                    serverStatus.setForeground(Themes.getColor(data.getSettings().getTheme(), ThemeColor.SUCCESS));
                } else {
                    serverOnline = false;
                    serverStatus.setText("Offline");
                    serverStatus.setForeground(Themes.getColor(data.getSettings().getTheme(), ThemeColor.OFFLINE));
                }
            } else {
                serverOnline = null;
                serverStatus.setText("None");
                serverStatus.setForeground(Themes.getColor(data.getSettings().getTheme(), ThemeColor.DEFAULT));
            }

            // Reload the saves table
            setTable(data.getSaves(), serverOnline);
        });

        reloadThread.start();
    }

    private void setTable(HashMap<String, Save> dataSaves, Boolean serverOnline) {
        DefaultTableModel dtm = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dtm.setColumnIdentifiers(saveListHeaders);
        saveList.setModel(dtm);
        saveList.getColumnModel().getColumn(2).setCellRenderer(new SaveStatusCellRenderer(data.getSettings().getTheme()));
        saveList.getTableHeader().setReorderingAllowed(false);

        ArrayList<Save> saves = new ArrayList<>();
        for (String saveName : dataSaves.keySet()) {
            Save save = dataSaves.get(saveName);
            saves.add(save);
            dtm.addRow(new Object[]{
                    save.getName(),
                    save.getFile(),
                    serverOnline == null ? "No Server" : (serverOnline ? "Checking..." : "Offline")
            });
        }
        if (serverOnline != null && serverOnline) {
            for (Save save : saves) {
                /* Get the server status */
                String status;
                File remoteSaveFile = null;
                File localSaveFile = null;
                ZipFile remoteZipFile = null;
                ZipFile localZipFile = null;
                try {
                    /* Create remote and local temp files */
                    remoteSaveFile = Files.createTempFile("SaveFileSync", ".zip").toFile();
                    remoteSaveFile.deleteOnExit();
                    localSaveFile = Files.createTempFile("SaveFileSync", ".zip").toFile();
                    localSaveFile.deleteOnExit();

                    /* Get the file data from server and save file */
                    byte[] remoteSave = data.getServer().getSaveData(save.getName());
                    byte[] localSave = save.toZipFile();
                    if (remoteSave == null || Arrays.equals(remoteSave, new byte[0])) {
                        status = "Not Synced";
                    } else if (localSave == null || Arrays.equals(localSave, new byte[0])) {
                        status = "Not Synced";
                    } else {
                        /* Write to zip files */
                        FileUtils.writeByteArrayToFile(remoteSaveFile, remoteSave);
                        remoteZipFile = new ZipFile(remoteSaveFile);
                        FileUtils.writeByteArrayToFile(localSaveFile, localSave);
                        localZipFile = new ZipFile(localSaveFile);

                        /* Compare the 2 using external script */
                        status = FileUtilities.ZipCompare(remoteZipFile, localZipFile) ? "Synced" : "Not Synced";

                        /* Cleanup */
                        remoteZipFile.close();
                        remoteSaveFile.delete();
                        localZipFile.close();
                        localSaveFile.delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        if (remoteZipFile != null) remoteZipFile.close();
                        if (localZipFile != null) localZipFile.close();
                    } catch (IOException ee) {
                        ee.printStackTrace();
                    } finally {
                        if (localSaveFile != null) localSaveFile.delete();
                        if (remoteSaveFile != null) remoteSaveFile.delete();
                        status = "Error";
                    }
                }
                /* Set the status on the table */
                for (int i = 0; i < dtm.getRowCount(); i++) {
                    if (dtm.getValueAt(i, 0).equals(save.getName())) {
                        // Set the status
                        dtm.setValueAt(status, i, 2);
                        break;
                    }
                }
            }
        }
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        newSaveFile = new JButton();
        newSaveFile.setText("New Save File");
        newSaveFile.setToolTipText("Creates a new instance of a save file");
        rootPanel.add(newSaveFile, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        statusLabel = new JLabel();
        Font statusLabelFont = this.$$$getFont$$$(null, -1, -1, statusLabel.getFont());
        if (statusLabelFont != null) statusLabel.setFont(statusLabelFont);
        statusLabel.setText("Status");
        panel3.add(statusLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverStatus = new JLabel();
        Font serverStatusFont = this.$$$getFont$$$(null, -1, -1, serverStatus.getFont());
        if (serverStatusFont != null) serverStatus.setFont(serverStatusFont);
        serverStatus.setText("None");
        panel3.add(serverStatus, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dataServerLabel = new JLabel();
        Font dataServerLabelFont = this.$$$getFont$$$(null, -1, -1, dataServerLabel.getFont());
        if (dataServerLabelFont != null) dataServerLabel.setFont(dataServerLabelFont);
        dataServerLabel.setText("Data Server");
        panel3.add(dataServerLabel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        importFromServerButton = new JButton();
        importFromServerButton.setEnabled(true);
        importFromServerButton.setText("Import Save From Server");
        importFromServerButton.setToolTipText("Imports a save file from the data server");
        panel3.add(importFromServerButton, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        manageServerButton = new JButton();
        manageServerButton.setText("Manage Server");
        manageServerButton.setToolTipText("Manages the status and location of the data server");
        panel3.add(manageServerButton, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hostLabel = new JLabel();
        Font hostLabelFont = this.$$$getFont$$$(null, -1, -1, hostLabel.getFont());
        if (hostLabelFont != null) hostLabel.setFont(hostLabelFont);
        hostLabel.setText("Host");
        panel3.add(hostLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hostNameField = new JLabel();
        Font hostNameFieldFont = this.$$$getFont$$$(null, -1, -1, hostNameField.getFont());
        if (hostNameFieldFont != null) hostNameField.setFont(hostNameFieldFont);
        hostNameField.setText("None");
        panel3.add(hostNameField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        typeLabel = new JLabel();
        Font typeLabelFont = this.$$$getFont$$$(null, -1, -1, typeLabel.getFont());
        if (typeLabelFont != null) typeLabel.setFont(typeLabelFont);
        typeLabel.setText("Type");
        panel3.add(typeLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverTypeField = new JLabel();
        Font serverTypeFieldFont = this.$$$getFont$$$(null, -1, -1, serverTypeField.getFont());
        if (serverTypeFieldFont != null) serverTypeField.setFont(serverTypeFieldFont);
        serverTypeField.setText("None");
        panel3.add(serverTypeField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        rootPanel.add(scrollPane1, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        saveList = new JTable();
        saveList.setSelectionBackground(new Color(-13487566));
        scrollPane1.setViewportView(saveList);
        removeButton = new JButton();
        removeButton.setEnabled(false);
        removeButton.setText("Remove");
        removeButton.setToolTipText("Removes a save file from the local list of saves");
        rootPanel.add(removeButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editButton = new JButton();
        editButton.setEnabled(false);
        editButton.setText("Edit");
        editButton.setToolTipText("Edits a save file's name and location (changing the name can cause desyncing)");
        rootPanel.add(editButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exportButton = new JButton();
        exportButton.setEnabled(false);
        exportButton.setText("Export");
        exportButton.setToolTipText("Backs up selected file(s) to the server");
        rootPanel.add(exportButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        importButton = new JButton();
        importButton.setEnabled(false);
        importButton.setText("Import");
        importButton.setToolTipText("Imports selected files from the server");
        rootPanel.add(importButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        restoreBackupButton = new JButton();
        restoreBackupButton.setEnabled(false);
        restoreBackupButton.setText("Restore Backup");
        rootPanel.add(restoreBackupButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        settingsButton = new JButton();
        settingsButton.setText("Settings");
        rootPanel.add(settingsButton, new GridConstraints(1, 0, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
