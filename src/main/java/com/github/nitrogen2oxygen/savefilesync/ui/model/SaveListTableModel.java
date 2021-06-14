package com.github.nitrogen2oxygen.savefilesync.ui.model;

import com.github.nitrogen2oxygen.savefilesync.client.ClientData;
import com.github.nitrogen2oxygen.savefilesync.client.save.Save;
import com.github.nitrogen2oxygen.savefilesync.ui.MainPanel;
import com.github.nitrogen2oxygen.savefilesync.util.FileUtilities;
import org.apache.commons.io.FileUtils;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipFile;

public class SaveListTableModel extends DefaultTableModel {

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void setStatuses(ClientData data) {
        ExecutorService service = Executors.newCachedThreadPool();
        for (int i = 0; i < getRowCount(); i++) {
            int finalI = i;
            service.execute(() -> setStatus(finalI, data));
        }
    }

    private void setStatus(int row, ClientData data) {
        // Get save
        String name = (String) getValueAt(row, 0);
        Save save = data.getSave(name);

        // Set the status
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
            byte[] localSave = save.toZipData();
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
        setValueAt(status, row, 3);
    }

    public void addSave(Save save) {
        String fileSize;
        try {
            fileSize = FileUtils.byteCountToDisplaySize(FileUtils.sizeOf(save.getFile()));
        }  catch (RuntimeException e) {
            e.printStackTrace();
            fileSize = "0 bytes";
        }

        addRow(new Object[] {
                save.getName(),
                save.getFile(),
                fileSize,
                "Checking...",
                "Checking..."
        });
    }

    public void noServerFound() {
        for (int i = 0; i < getRowCount(); i++) {
            setValueAt("No Server", i, MainPanel.SYNC_STATUS_COLUMN);
        }
    }

    public void serverOffline() {
        for (int i = 0; i < getRowCount(); i++) {
            setValueAt("Offline", i, MainPanel.SYNC_STATUS_COLUMN);
        }
    }
}
