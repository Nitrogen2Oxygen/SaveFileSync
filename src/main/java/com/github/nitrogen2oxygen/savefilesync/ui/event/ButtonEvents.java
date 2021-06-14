package com.github.nitrogen2oxygen.savefilesync.ui.event;

import com.github.nitrogen2oxygen.savefilesync.client.ClientData;
import com.github.nitrogen2oxygen.savefilesync.client.Settings;
import com.github.nitrogen2oxygen.savefilesync.save.Save;
import com.github.nitrogen2oxygen.savefilesync.server.DataServer;
import com.github.nitrogen2oxygen.savefilesync.ui.MainPanel;
import com.github.nitrogen2oxygen.savefilesync.ui.dialog.ChangeSettings;
import com.github.nitrogen2oxygen.savefilesync.ui.dialog.SaveFileManager;
import com.github.nitrogen2oxygen.savefilesync.ui.dialog.ServerImport;
import com.github.nitrogen2oxygen.savefilesync.ui.dialog.ServerOptions;
import com.github.nitrogen2oxygen.savefilesync.util.DataManager;
import com.github.nitrogen2oxygen.savefilesync.util.FileUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ButtonEvents {
    private final ClientData data;
    private final MainPanel ui;

    public ButtonEvents(ClientData data, MainPanel ui) {
        this.data = data;
        this.ui = ui;
    }

    public void newSaveFile(ActionEvent event) {
        Save save = SaveFileManager.main();
        if (save == null) return;
        try {
            data.addSave(save);
        } catch (Exception err) {
            JOptionPane.showMessageDialog(ui.getRootPanel(),
                    err.getMessage(),
                    "Error!",
                    JOptionPane.ERROR_MESSAGE);
        }
        DataManager.save(data);
        ui.reload();
    }

    public void manageServer(ActionEvent e) {
        DataServer newDataServer = ServerOptions.main(data);
        data.setServer(newDataServer);

        /* Save and reload */
        DataManager.save(data);
        ui.reload();
    }

    public void exportSaves(ActionEvent event) {
        if (data.getServer() == null) {
            JOptionPane.showMessageDialog(ui.getRootPanel(),
                    "Cannot export files without a data server! (This message should not be seen)",
                    "Export Error!",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int[] rows = ui.getSaveList().getSelectedRows();
        List<Save> exportingSaves = new ArrayList<>();
        if (rows.length == 0) return;
        for (int i : rows) {
            String name = (String) ui.getSaveList().getValueAt(i, 0);
            exportingSaves.add(data.getSave(name));
        }
        // Export each
        for (Save save : exportingSaves) {
            try {
                byte[] rawData = save.toZipData();
                if (!Arrays.equals(rawData, new byte[0])) {
                    data.getServer().uploadSaveData(save.getName(), rawData);
                } else {
                    JOptionPane.showMessageDialog(ui.getRootPanel(),
                            "Cannot export an empty save file! Skipping file!",
                            save.getName() + " Export Error!",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(ui.getRootPanel(),
                        "There was en error uploading a file! Aborting export!",
                        save.getName() + " Export Error!",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        ui.reload();
        JOptionPane.showMessageDialog(ui.getRootPanel(),
                "Successfully exported files(s)!",
                "Success!",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void importSaves(ActionEvent event) {
        if (data.getServer() == null) {
            JOptionPane.showMessageDialog(ui.getRootPanel(),
                    "Cannot import files without a data server! (This message should not be seen)",
                    "Import Error!",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int[] rows = ui.getSaveList().getSelectedRows();
        List<Save> importingSaves = new ArrayList<>();
        if (rows.length == 0) return;
        for (int i : rows) {
            String name = (String) ui.getSaveList().getValueAt(i, 0);
            importingSaves.add(data.getSave(name));
        }

        for (Save save : importingSaves) {
            try {
                byte[] remoteSaveData = data.getServer().getSaveData(save.getName());
                save.writeData(remoteSaveData, data.getSettings().shouldMakeBackups(), data.getSettings().shouldForceOverwrite());
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(ui.getRootPanel(),
                        "There was an error importing a file! Aborting import!",
                        save.getName() + " Import Error!",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        ui.reload();
        JOptionPane.showMessageDialog(ui.getRootPanel(),
                "Successfully downloaded files(s)!",
                "Success!",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void serverImport(ActionEvent event) {
        List<String> newSaves = new ArrayList<>();
        List<String> serverSaveNames = data.getServer().getSaveNames();
        List<String> localSaveNames = new ArrayList<>();
        List<Save> localSaves = data.getSaveList();
        for (Save save : localSaves) {
            localSaveNames.add(save.getName());
        }
        /* Check for any new saves on the server that aren't in the local file system */
        for (String serverName : serverSaveNames) {
            if (!localSaveNames.contains(serverName)) {
                newSaves.add(serverName);
            }
        }
        try {
            Save save = ServerImport.main(data.getServer(), newSaves);
            if (save != null) {
                data.addSave(save);
                ui.reload();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ui.getRootPanel(),
                    "Cannot import save data! If this continues, please submit an issue on the GitHub!",
                    "Error!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void removeSave(ActionEvent event) {
        int selected = ui.getSaveList().getSelectedRow();
        String name = (String) ui.getSaveList().getValueAt(selected, 0);
        /* Remove save file */
        data.removeSave(name);

        /* Save and reload */
        DataManager.save(data);
        ui.reload();
    }

    public void editSave(ActionEvent event) {
        int selected = ui.getSaveList().getSelectedRow();
        String name = (String) ui.getSaveList().getValueAt(selected, 0);
        Save save = data.getSave(name);
        String oldName = save.getName();
        Save newSave = SaveFileManager.edit(save.getName(), save.getFile().getPath());
        if (newSave == null) return;
        try {
            data.removeSave(oldName);
            data.addSave(newSave);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ui.getRootPanel(),
                    "Error editing save file",
                    "Error!",
                    JOptionPane.ERROR_MESSAGE);
            try {
                data.addSave(save);
            } catch (Exception ignored) {
            }
        }

        /* Save and reload */
        DataManager.save(data);
        ui.reload();
    }

    public void changeSettings(ActionEvent event) {
        Settings newSettings = ChangeSettings.main(data.getSettings());
        if (newSettings == null) return;
        data.setSettings(newSettings);
        DataManager.save(data);
        ui.reload();
    }

    public void restoreBackup(ActionEvent event) {
        // Get current save
        int selected = ui.getSaveList().getSelectedRow();
        String name = (String) ui.getSaveList().getValueAt(selected, 0);
        Save save = data.getSave(name);

        // Check if there's a backup (just in case)
        if (!FileUtilities.hasBackup(save)) return;

        // Restore
        try {
            FileUtilities.restoreBackup(save, data.getSettings());
            JOptionPane.showMessageDialog(ui.getRootPanel(), "Successfully restored backup data! Old data saved to backup directory.", "Success!", JOptionPane.INFORMATION_MESSAGE);
            ui.reload();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(ui.getRootPanel(), "Could not restore backup data!", "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void createBackup(ActionEvent event) {
        // Get current save
        int selected = ui.getSaveList().getSelectedRow();
        String name = (String) ui.getSaveList().getValueAt(selected, 0);
        Save save = data.getSave(name);

        if (FileUtilities.hasBackup(save)) {
            int cont = JOptionPane.showConfirmDialog(ui.getRootPanel(),
                    "Creating a backup will overwrite the previous backup. Would you like to continue?",
                    "Warning!",
                    JOptionPane.YES_NO_OPTION);
            if (cont != 0) {
                return;
            }
        }

        try {
            FileUtilities.makeBackup(save);
            JOptionPane.showMessageDialog(ui.getRootPanel(), "Successfully created a backup!", "Success!", JOptionPane.INFORMATION_MESSAGE);
            ui.reload();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(ui.getRootPanel(), "An error occurred when creating a backup!", "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }
}
