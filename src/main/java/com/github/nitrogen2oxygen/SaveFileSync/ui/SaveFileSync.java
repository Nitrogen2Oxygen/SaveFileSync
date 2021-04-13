package com.github.nitrogen2oxygen.SaveFileSync.ui;

import com.github.nitrogen2oxygen.SaveFileSync.client.ClientData;
import com.github.nitrogen2oxygen.SaveFileSync.client.ThemeColor;
import com.github.nitrogen2oxygen.SaveFileSync.client.Themes;
import com.github.nitrogen2oxygen.SaveFileSync.utils.ButtonEvents;
import com.github.nitrogen2oxygen.SaveFileSync.client.Save;
import com.github.nitrogen2oxygen.SaveFileSync.utils.FileUtilities;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipFile;

public class SaveFileSync {
    private JPanel rootPanel;
    private JButton newSaveFile;
    private JButton importButton;
    private JButton exportButton;
    private JTable saveList;
    private JButton manageServerButton;
    private JLabel label11;
    private JLabel serverStatus;
    private JButton importFromServerButton;
    private JButton removeButton;
    private JButton editButton;
    private JLabel hostNameField;
    private JLabel serverTypeField;
    private JButton settingsButton;

    private static Thread reloadThread;
    private final ClientData data;
    private static final String[] saveListHeaders = new String[]{
            "Name",
            "Location",
            "Status"
    };

    public SaveFileSync(ClientData userData) {
        data = userData;

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
            importButton.setEnabled(saveList.getSelectedRows().length != 0);
            exportButton.setEnabled(saveList.getSelectedRows().length != 0);
            removeButton.setEnabled(saveList.getSelectedRows().length == 1);
            editButton.setEnabled(saveList.getSelectedRows().length == 1);
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
            serverTypeField.setText(data.getServer() != null ? data.getServer().serverDisplayName() : "None");

            // Set server status
            Boolean serverOnline;
            if (data.getServer() != null) {
                Boolean status = data.getServer().verifyServer();
                if (status == null) {
                    serverStatus.setText("None");
                    serverOnline = null;
                    serverStatus.setForeground(Themes.getColor(data.getSettings().getTheme(), ThemeColor.DEFAULT));
                } else if (status) {
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
        rootPanel.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        newSaveFile = new JButton();
        newSaveFile.setText("New Save File");
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
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 16, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Status");
        panel3.add(label1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverStatus = new JLabel();
        Font serverStatusFont = this.$$$getFont$$$(null, -1, 16, serverStatus.getFont());
        if (serverStatusFont != null) serverStatus.setFont(serverStatusFont);
        serverStatus.setText("None");
        panel3.add(serverStatus, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label11 = new JLabel();
        Font label11Font = this.$$$getFont$$$(null, -1, 24, label11.getFont());
        if (label11Font != null) label11.setFont(label11Font);
        label11.setText("Data Server");
        panel3.add(label11, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        importFromServerButton = new JButton();
        importFromServerButton.setEnabled(true);
        importFromServerButton.setText("Import Save From Server");
        importFromServerButton.setToolTipText("Imports a save file from the data server");
        panel3.add(importFromServerButton, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        manageServerButton = new JButton();
        manageServerButton.setText("Manage Server");
        manageServerButton.setToolTipText("Manages the status and location of the data server");
        panel3.add(manageServerButton, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, -1, 16, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("Host");
        panel3.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hostNameField = new JLabel();
        Font hostNameFieldFont = this.$$$getFont$$$(null, -1, 16, hostNameField.getFont());
        if (hostNameFieldFont != null) hostNameField.setFont(hostNameFieldFont);
        hostNameField.setText("None");
        panel3.add(hostNameField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$(null, -1, 16, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText("Type");
        panel3.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverTypeField = new JLabel();
        Font serverTypeFieldFont = this.$$$getFont$$$(null, -1, 16, serverTypeField.getFont());
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
        settingsButton = new JButton();
        settingsButton.setText("Settings");
        rootPanel.add(settingsButton, new GridConstraints(1, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
