package com.github.nitrogen2oxygen.savefilesync.client;

import com.github.nitrogen2oxygen.savefilesync.server.DataServer;
import com.github.nitrogen2oxygen.savefilesync.util.DataManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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

    public ArrayList<Save> getSaveList() {
        ArrayList<Save> saveList = new ArrayList<>();
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
        this.saves.put(save.getName(), save);
        DataManager.save(this);
    }
}
