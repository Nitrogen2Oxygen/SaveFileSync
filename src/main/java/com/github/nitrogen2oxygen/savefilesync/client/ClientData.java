package com.github.nitrogen2oxygen.savefilesync.client;

import com.github.nitrogen2oxygen.savefilesync.client.save.Save;
import com.github.nitrogen2oxygen.savefilesync.server.DataServer;
import com.github.nitrogen2oxygen.savefilesync.util.Constants;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientData {
    private DataServer server; // Everything we need server wise is located here
    private Settings settings; // The client settings includes any kind of extra data the user manually sets
    private final HashMap<String, Save> saves; // Saves can either be retrieved from the dataServer OR created locally.

    /* Load already created data */
    public ClientData(DataServer dataServer, Settings settings, HashMap<String, Save> saves) {
        this.server = dataServer;
        this.settings = settings;
        this.saves = saves;
    }

    public DataServer getServer() {
        return server;
    }

    public void setServer(DataServer dataServer) {
        this.server = dataServer;
    }

    public Settings getSettings() {
        if (settings == null) settings = new Settings();
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
        settings.apply();
    }

    public Save getSave(String name) {
        return saves.get(name);
    }

    public List<Save> getSaveList() {
        List<Save> saveList = new ArrayList<>();
        for (String saveName : saves.keySet()) {
            Save save = saves.get(saveName);
            saveList.add(save);
        }
        return saveList;
    }

    public void removeSave(String name) {
        saves.remove(name);
    }

    public void addSave(Save save) throws Exception {
        // Check for duplicates FIRST
        for (String saveName : (this.saves.keySet())) {
            Save currentSave = this.saves.get(saveName);
            String name = currentSave.getName();
            File file = currentSave.getFile();
            if (name.equals(save.getName())) throw new Exception("Duplicate name detected. Please use unique identifiers for each save file.");
            if (file.getPath().equals(save.getFile().getPath())) throw new Exception("Duplicate location detected. This save file is already in the system");
            if (file.getPath().contains(save.getFile().getPath())
                    || save.getFile().getPath().contains(currentSave.getFile().getPath())) {
                throw new Exception("One location includes another. Please do not sync files in the same path");
            }
        }
        // Too large warning
        long saveSize = FileUtils.sizeOf(save.getFile());
        if (saveSize > Constants.FILE_SIZE_WARNING_THRESHOLD) {
            int res = JOptionPane.showConfirmDialog(null, "Warning, save is over 8MB. Large saves take a long time to import, export, backup and check. Would you like to continue?", "Warning!", JOptionPane.YES_NO_OPTION);
            if (res != 0) return;
        }
        this.saves.put(save.getName(), save);
    }
}
