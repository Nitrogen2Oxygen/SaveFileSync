package com.github.nitrogen2oxygen.SaveFileSync.ui;

import com.github.nitrogen2oxygen.SaveFileSync.data.client.ClientData;
import com.github.nitrogen2oxygen.SaveFileSync.utils.DataManager;
import com.github.nitrogen2oxygen.SaveFileSync.data.client.Save;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/* TODO: Create delete save file button */
public class SaveFileSyncUI {
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

    public Thread reloadThread;
    private final ClientData data;
    private final String[] header = new String[]{
            "Name",
            "Location",
            "Status"
    };

    public SaveFileSyncUI(ClientData userData) {
        data = userData;

        /* Create blank data table */
        DefaultTableModel dtm = new DefaultTableModel();
        dtm.setColumnIdentifiers(header);
        saveList.setModel(dtm);

        // User input to backend
        newSaveFile.addActionListener(e -> {
            Save save = NewSaveFile.main();
            if (save == null) return;
            try {
                data.addSave(save);
            } catch (Exception ee) {
                JOptionPane.showMessageDialog(SwingUtilities.getRoot((Component) e.getSource()),
                        ee.getMessage(),
                        "Error Creating New Save File",
                        JOptionPane.ERROR_MESSAGE);
            }
            reloadUI();
        });

        /* Action listeners */
        manageServerButton.addActionListener(e -> {
            data.server = ServerManagerUI.main(data);
            DataManager.save(data);
            reloadUI();
        });

        exportButton.addActionListener(e -> {
            if (data.server == null || !data.server.verifyServer()) {
                JOptionPane.showMessageDialog(SwingUtilities.getRoot((Component) e.getSource()), "Cannot export files without a working data server!", "Export Error!", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int[] rows = saveList.getSelectedRows();
            if (rows.length == 0) return;
            for (int i : rows) {
                String name = (String) saveList.getValueAt(i, 0);
                Save save = data.saves.get(name);
                try {
                    byte[] rawData = save.toZipFile();
                    if (!Arrays.equals(rawData, new byte[0])) {
                        data.server.uploadSaveData(save.name, rawData);
                    } else {
                        JOptionPane.showMessageDialog(SwingUtilities.getRoot((Component) e.getSource()), "Cannot export an empty save file!", name + " Export Error!", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                    JOptionPane.showMessageDialog(SwingUtilities.getRoot((Component) e.getSource()), "There was en error uploading a file! Aborting export!", name + " Export Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            reloadUI();
            JOptionPane.showMessageDialog(SwingUtilities.getRoot((Component) e.getSource()), "Successfully uploaded files(s)!", "Success!", JOptionPane.INFORMATION_MESSAGE);
        });
        importButton.addActionListener(e -> {
            if (data.server == null || !data.server.verifyServer()) {
                JOptionPane.showMessageDialog(SwingUtilities.getRoot((Component) e.getSource()), "Cannot import files without a working data server!", "Import Error!", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int[] rows = saveList.getSelectedRows();
            if (rows.length == 0) return;
            for (int i : rows) {
                String name = (String) saveList.getValueAt(i, 0);
                Save save = data.saves.get(name);
                try {
                    byte[] remoteSaveData = data.server.getSaveData(save.name);
                    save.overwriteData(remoteSaveData);
                } catch (Exception ee) {
                    ee.printStackTrace();
                    JOptionPane.showMessageDialog(SwingUtilities.getRoot((Component) e.getSource()), "There was en error importing a file! Aborting import!", name + " Import Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            reloadUI();
            JOptionPane.showMessageDialog(SwingUtilities.getRoot((Component) e.getSource()), "Successfully downloaded files(s)!", "Success!", JOptionPane.INFORMATION_MESSAGE);
        });
        saveList.getSelectionModel().addListSelectionListener(e -> {
            // Enable/disable buttons
            importButton.setEnabled(saveList.getSelectedRows().length != 0);
            exportButton.setEnabled(saveList.getSelectedRows().length != 0);
            removeButton.setEnabled(saveList.getSelectedRows().length == 1);
            editButton.setEnabled(saveList.getSelectedRows().length == 1);
        });
        importFromServerButton.addActionListener(e -> {
            ArrayList<String> newSaves = new ArrayList<>();
            ArrayList<String> serverSaveNames = data.server.getSaveNames();
            ArrayList<String> localSaveNames = new ArrayList<>();
            Set<String> localKeys = data.saves.keySet();
            for (String key : localKeys) {
                localSaveNames.add(data.saves.get(key).name);
            }
            /* Check for any new saves on the server that aren't in the local file system */
            for (String serverName : serverSaveNames) {
                if (!localSaveNames.contains(serverName)) {
                    newSaves.add(serverName);
                }
            }
            try {
                Save save = ServerImport.main(data.server, newSaves);
                if (save != null) {
                    data.addSave(save);
                }
            } catch (Exception ee) {
                JOptionPane.showMessageDialog(SwingUtilities.getRoot((Component) e.getSource()),
                        "Cannot import save data! If this continues, please submit an issue on the GitHub!",
                        "Error!",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        removeButton.addActionListener(e -> {
            int selected = saveList.getSelectedRow();
            String name = (String) saveList.getValueAt(selected, 0);
            /* Remove save file */
            Save save = data.saves.remove(name);
            reloadUI();
        });
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    /* We reload the UI on a separate thread to prevent any kind of freezing */
    public void reloadUI() {
        if (reloadThread != null && reloadThread.isAlive())
            reloadThread.interrupt();

        reloadThread = new Thread(() -> {
            serverStatus.setText("Connecting...");
            serverStatus.setForeground(Color.white);

            // Reload the saves table
            setTable(data.saves);

            // Set server status
            if (data.server != null) {
                Boolean status = data.server.verifyServer();
                if (status == null) {
                    serverStatus.setText("None");
                    serverStatus.setForeground(Color.white);
                } else if (status) {
                    serverStatus.setText("Online");
                    serverStatus.setForeground(Color.green);
                } else {
                    serverStatus.setText("Offline");
                    serverStatus.setForeground(Color.red);
                }
            } else {
                serverStatus.setText("None");
                serverStatus.setForeground(Color.white);
            }
        });

        reloadThread.start();
    }

    private void setTable(HashMap<String, Save> saves) {
        DefaultTableModel dtm = new DefaultTableModel();
        dtm.setColumnIdentifiers(header);
        for (String saveName : saves.keySet()) {
            Save save = saves.get(saveName);
            String status;
            if (data.server != null) {
                try {
                    byte[] remoteSave = data.server.getSaveData(save.name);
                    byte[] localSave = save.toZipFile();
                    if (Arrays.equals(localSave, remoteSave)) {
                        status = "Synced";
                    } else {
                        status = "Not Synced";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    status = "Error";
                }
            } else {
                status = "No Server";
            }
            dtm.addRow(new Object[]{
                    save.name,
                    save.file,
                    status
            });
        }
        saveList.setModel(dtm);
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
        panel3.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 16, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Status");
        panel3.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverStatus = new JLabel();
        Font serverStatusFont = this.$$$getFont$$$(null, -1, 16, serverStatus.getFont());
        if (serverStatusFont != null) serverStatus.setFont(serverStatusFont);
        serverStatus.setText("None");
        panel3.add(serverStatus, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label11 = new JLabel();
        Font label11Font = this.$$$getFont$$$(null, -1, 24, label11.getFont());
        if (label11Font != null) label11.setFont(label11Font);
        label11.setText("Data Server");
        panel3.add(label11, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        importFromServerButton = new JButton();
        importFromServerButton.setEnabled(true);
        importFromServerButton.setText("Import Save From Server");
        panel3.add(importFromServerButton, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        manageServerButton = new JButton();
        manageServerButton.setText("Manage Server");
        manageServerButton.setToolTipText("Manages the status and location of the data server");
        panel3.add(manageServerButton, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        rootPanel.add(scrollPane1, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        saveList = new JTable();
        saveList.setSelectionBackground(new Color(-13487566));
        scrollPane1.setViewportView(saveList);
        removeButton = new JButton();
        removeButton.setEnabled(false);
        removeButton.setText("Remove");
        rootPanel.add(removeButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editButton = new JButton();
        editButton.setEnabled(false);
        editButton.setText("Edit");
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
