package com.github.nitrogen2oxygen.savefilesync.ui;

import com.github.nitrogen2oxygen.savefilesync.client.ClientData;
import com.github.nitrogen2oxygen.savefilesync.client.theme.ThemeColor;
import com.github.nitrogen2oxygen.savefilesync.server.DataServer;
import com.github.nitrogen2oxygen.savefilesync.ui.event.ButtonEvents;
import com.github.nitrogen2oxygen.savefilesync.util.Themes;
import com.github.nitrogen2oxygen.savefilesync.util.DataServers;
import com.github.nitrogen2oxygen.savefilesync.ui.model.SaveListTableModel;
import com.github.nitrogen2oxygen.savefilesync.ui.renderer.BackupStatusCellRenderer;
import com.github.nitrogen2oxygen.savefilesync.ui.renderer.SaveStatusCellRenderer;
import com.github.nitrogen2oxygen.savefilesync.client.Save;
import com.github.nitrogen2oxygen.savefilesync.util.FileUtilities;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.List;
import java.util.Locale;

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
    private JButton createBackupButton;

    private static Thread reloadThread;
    private final ClientData data;
    private static final Font dataLabelFont = new Font("Segoe", Font.PLAIN, 16);
    private static final Font dataServerLabelFont = new Font("Segoe", Font.PLAIN, 24);
    private static final String[] saveListHeaders = new String[]{
            "Name",
            "File Location",
            "Size",
            "Sync Status",
            "Backup"
    };
    public static int SYNC_STATUS_COLUMN = 3;
    public static int BACKUP_COLUMN = 4;

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

        /* Load the UI */
        reload();
        reloadSelection(null);

        /* Standard action listeners */
        saveList.getSelectionModel().addListSelectionListener(this::reloadSelection);

        /* Button events */
        ButtonEvents buttonEvents = new ButtonEvents(data, this);
        newSaveFile.addActionListener(buttonEvents::newSaveFile);
        manageServerButton.addActionListener(buttonEvents::manageServer);
        exportButton.addActionListener(buttonEvents::exportSaves);
        importButton.addActionListener(buttonEvents::importSaves);
        importFromServerButton.addActionListener(buttonEvents::serverImport);
        removeButton.addActionListener(buttonEvents::removeSave);
        editButton.addActionListener(buttonEvents::editSave);
        settingsButton.addActionListener(buttonEvents::changeSettings);
        restoreBackupButton.addActionListener(buttonEvents::restoreBackup);
        createBackupButton.addActionListener(buttonEvents::createBackup);
    }

    private void reloadSelection(ListSelectionEvent e) {
        // Check some re-used conditions
        boolean anySelected = saveList.getSelectedRows().length != 0;
        boolean multipleSelected = saveList.getSelectedRows().length > 1;
        boolean oneSelected = anySelected && !multipleSelected;

        // Export and import buttons
        DataServer server = data.getServer();
        importButton.setEnabled(server != null);
        exportButton.setEnabled(server != null);
        if (anySelected) {
            importButton.setText("Import Selected");
            exportButton.setText("Export Selected");
        } else {
            importButton.setText("Import All");
            exportButton.setText("Export All");
        }

        // Remove and edit buttons
        removeButton.setEnabled(oneSelected);
        editButton.setEnabled(oneSelected);

        // Create and restore backup buttons
        if (oneSelected) {
            int selected = saveList.getSelectedRow();
            String name = (String) saveList.getValueAt(selected, 0);
            Save save = data.getSave(name);
            restoreBackupButton.setEnabled(FileUtilities.hasBackup(save));
        }
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public JTable getSaveList() {
        return saveList;
    }

    public void reload() {
        if (reloadThread != null && reloadThread.isAlive()) {
            reloadThread.interrupt();
            reloadThread = null;
        }
        reloadThread = new Thread(() -> {
            DataServer server = data.getServer();
            boolean noServer = server == null;

            /* Set placeholders and metadata on the sidebar */
            serverStatus.setText("Connecting...");
            serverStatus.setForeground(Themes.getColor(data.getSettings().getTheme(), ThemeColor.DEFAULT));
            hostNameField.setText(noServer ? "None" : data.getServer().getHostName());
            serverTypeField.setText(noServer ? "None" : DataServers.getDisplayName(server.getServerType()));

            /* Create table model and add local saves */
            SaveListTableModel model = new SaveListTableModel();
            model.setColumnIdentifiers(saveListHeaders);
            List<Save> saves = data.getSaveList();
            for (Save save : saves) {
                model.addSave(save);
            }

            /* Set the new model */
            saveList.setModel(model);
            saveList.getColumnModel().getColumn(SYNC_STATUS_COLUMN).setCellRenderer(new SaveStatusCellRenderer(data.getSettings().getTheme()));
            saveList.getColumnModel().getColumn(BACKUP_COLUMN).setCellRenderer(new BackupStatusCellRenderer(data));
            saveList.getTableHeader().setReorderingAllowed(false);

            /* Check if the server is online and set statuses accordingly */
            if (noServer) {
                serverStatus.setText("None");
                serverStatus.setForeground(Themes.getColor(data.getSettings().getTheme(), ThemeColor.DEFAULT));
                model.noServerFound(); // Tells the model that no server was found
            } else {
                boolean serverOnline = data.getServer().verifyServer();
                if (serverOnline) {
                    serverStatus.setText("Online");
                    serverStatus.setForeground(Themes.getColor(data.getSettings().getTheme(), ThemeColor.SUCCESS));
                    model.setStatuses(data); // Tells the model to get the sync status for each save
                } else {
                    serverStatus.setText("Offline");
                    serverStatus.setForeground(Themes.getColor(data.getSettings().getTheme(), ThemeColor.OFFLINE));
                    model.serverOffline(); // Tells the model that the server is offline
                }
            }
        });

        reloadThread.start();
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
        removeButton.setEnabled(true);
        removeButton.setText("Remove");
        removeButton.setToolTipText("Removes a save file from the local list of saves");
        rootPanel.add(removeButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editButton = new JButton();
        editButton.setEnabled(true);
        editButton.setText("Edit");
        editButton.setToolTipText("Edits a save file's name and location (changing the name can cause desyncing)");
        rootPanel.add(editButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exportButton = new JButton();
        exportButton.setEnabled(true);
        exportButton.setText("Export");
        exportButton.setToolTipText("Backs up selected file(s) to the server");
        rootPanel.add(exportButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        importButton = new JButton();
        importButton.setEnabled(true);
        importButton.setText("Import");
        importButton.setToolTipText("Imports selected files from the server");
        rootPanel.add(importButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        settingsButton = new JButton();
        settingsButton.setText("Settings");
        rootPanel.add(settingsButton, new GridConstraints(1, 0, 4, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        restoreBackupButton = new JButton();
        restoreBackupButton.setEnabled(true);
        restoreBackupButton.setText("Restore Backup");
        rootPanel.add(restoreBackupButton, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createBackupButton = new JButton();
        createBackupButton.setEnabled(true);
        createBackupButton.setText("Create Backup");
        rootPanel.add(createBackupButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
