package com.github.nitrogen2oxygen.SaveFileSync.ui;

import com.github.nitrogen2oxygen.SaveFileSync.data.client.ClientData;
import com.github.nitrogen2oxygen.SaveFileSync.data.client.Save;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SaveFileSyncUI {
    private JPanel rootPanel;
    private JButton newSaveFile;
    private JButton importButton;
    private JButton exportButton;
    private JTable saveList;

    private final ClientData data;
    private final String[] header = new String[] {
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
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public void reloadUI() {
        // Reload the saves table
        setTable(data.saves);
    }

    private void setTable(List<Save> saves) {
        DefaultTableModel dtm = new DefaultTableModel();
        dtm.setColumnIdentifiers(header);
        for (Save save : saves) {
            dtm.addRow(new Object[] {
                    save.name,
                    save.file,
                    "Something"
            });
        }
        saveList.setModel(dtm);
    }


}
