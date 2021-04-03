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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class SaveFileSyncUI {
    private JPanel rootPanel;
    private JButton newSaveFile;
    private JButton importButton;
    private JButton exportButton;
    private JTable saveList;
    private JButton manageServerButton;
    private JLabel label11;
    private JLabel serverStatus;

    private final ClientData data;
    private final String[] header = new String[]{
            "Name",
            "Location",
            "Status"
    };

    public SaveFileSyncUI(ClientData userData) {
        data = userData;
        setTable(data.saves);

        // User input to backend
        newSaveFile.addActionListener(e -> {
            Save save = NewSaveFile.main();
            // TODO: Fix messaging when input is empty
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
        manageServerButton.addActionListener(e -> {
            data.server = ServerManagerUI.main(data);
            DataManager.save(data);
            reloadUI();
        });

        reloadUI();
        exportButton.addActionListener(e -> {
            if (data.server == null || !data.server.verifyServer()) {
                JOptionPane.showMessageDialog(null, "Cannot export files without a working data server!", "Export Error!", JOptionPane.ERROR_MESSAGE);
            }
            int[] rows = saveList.getSelectedRows();
            for (int i : rows) {
                String name = (String) saveList.getValueAt(i, 0);
                Save save = data.saves.get(name);
                try {
                    data.server.uploadSaveData(save.name, save.toRawFile());
                } catch (Exception ee) {
                    ee.printStackTrace();
                    JOptionPane.showMessageDialog(null, "There was en error uploading a file! Aborting export!", "Export Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            reloadUI();
            JOptionPane.showMessageDialog(null, "Successfully uploaded files(s)!", "Success!", JOptionPane.INFORMATION_MESSAGE);
        });
        importButton.addActionListener(e -> {
            if (data.server == null || !data.server.verifyServer()) {
                JOptionPane.showMessageDialog(null, "Cannot import files without a working data server!", "Import Error!", JOptionPane.ERROR_MESSAGE);
            }
            int[] rows = saveList.getSelectedRows();
            for (int i : rows) {
                String name = (String) saveList.getValueAt(i, 0);
                Save save = data.saves.get(name);
                byte[] remoteSaveData = data.server.getSaveData(save.name);
                try {
                    save.overwriteData(remoteSaveData);
                } catch (Exception ee) {
                    ee.printStackTrace();
                    JOptionPane.showMessageDialog(null, "There was en error importing a file! Aborting import!", "import Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            reloadUI();
            JOptionPane.showMessageDialog(null, "Successfully downloaded files(s)!", "Success!", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public void reloadUI() {
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
                    byte[] localSave = save.toRawFile();
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
        rootPanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        newSaveFile = new JButton();
        newSaveFile.setText("New Save File");
        rootPanel.add(newSaveFile, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        exportButton = new JButton();
        exportButton.setEnabled(true);
        exportButton.setText("Export");
        exportButton.setToolTipText("Backs up selected file(s) to the server");
        panel1.add(exportButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        importButton = new JButton();
        importButton.setEnabled(true);
        importButton.setText("Import");
        importButton.setToolTipText("Imports selected files from the server");
        panel1.add(importButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        manageServerButton = new JButton();
        manageServerButton.setText("Manage Server");
        manageServerButton.setToolTipText("Manages the status and location of the data server");
        panel3.add(manageServerButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel4.add(spacer1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 16, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Status");
        panel4.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        serverStatus = new JLabel();
        Font serverStatusFont = this.$$$getFont$$$(null, -1, 16, serverStatus.getFont());
        if (serverStatusFont != null) serverStatus.setFont(serverStatusFont);
        serverStatus.setText("None");
        panel4.add(serverStatus, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label11 = new JLabel();
        Font label11Font = this.$$$getFont$$$(null, -1, 24, label11.getFont());
        if (label11Font != null) label11.setFont(label11Font);
        label11.setText("Data Server");
        panel4.add(label11, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        rootPanel.add(scrollPane1, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        saveList = new JTable();
        saveList.setSelectionBackground(new Color(-13487566));
        scrollPane1.setViewportView(saveList);
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
