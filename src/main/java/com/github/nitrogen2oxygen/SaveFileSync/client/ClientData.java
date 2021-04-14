package com.github.nitrogen2oxygen.SaveFileSync.client;

import com.github.nitrogen2oxygen.SaveFileSync.server.Server;
import com.github.nitrogen2oxygen.SaveFileSync.utils.DataManager;

import java.io.File;
import java.util.HashMap;

public class ClientData {
    private Server server; // Everything we need server wise is located here
    private Settings settings; // The client settings includes any kind of extra data the user manually sets
    private final HashMap<String, Save> saves; // Saves can either be retrieved from the server OR created locally.

    /* Load already created data */
    public ClientData(Server server, Settings settings, HashMap<String, Save> saves) {
        this.server = server;
        this.settings = settings;
        this.saves = saves;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Settings getSettings() {
        if (settings == null) settings = new Settings();
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
        settings.apply();
    }

    public HashMap<String, Save> getSaves() {
        return saves;
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
