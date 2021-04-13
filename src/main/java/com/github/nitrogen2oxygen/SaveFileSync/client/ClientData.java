package com.github.nitrogen2oxygen.SaveFileSync.client;

import com.github.nitrogen2oxygen.SaveFileSync.server.Server;
import com.github.nitrogen2oxygen.SaveFileSync.utils.DataManager;

import java.io.File;
import java.util.HashMap;

public class ClientData implements java.io.Serializable {

    private static final long serialVersionUID = 7470750048460931688L;
    private Server server = null; // Everything we need server wise is located here
    private final HashMap<String, Save> saves = new HashMap<>(); // Saves can either be retrieved from the server OR created locally.

    public ClientData() {}

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
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
