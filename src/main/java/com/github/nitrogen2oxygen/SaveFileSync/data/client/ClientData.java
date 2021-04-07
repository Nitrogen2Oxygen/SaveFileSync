package com.github.nitrogen2oxygen.SaveFileSync.data.client;

import com.github.nitrogen2oxygen.SaveFileSync.data.server.Server;
import com.github.nitrogen2oxygen.SaveFileSync.utils.DataManager;

import java.util.HashMap;

public class ClientData implements java.io.Serializable {

    private static final long serialVersionUID = 7470750048460931688L;
    public Server server = null; // Everything we need server wise is located here
    public HashMap<String, Save> saves = new HashMap<>(); // Saves can either be retrieved from the server OR created locally.

    public ClientData() {}

    public void addSave(Save save) throws Exception {
        // Check for duplicates FIRST
        for (String saveName : (this.saves.keySet())) {
            Save currentSave = this.saves.get(saveName);
            if (currentSave.name.equals(save.name)) throw new Exception("Duplicate name detected. Please use unique identifiers for each save file.");
            if (currentSave.file.toString().equals(save.file.toString())) throw new Exception("Duplicate location detected. This save file is already in the system");
            if (currentSave.file.getPath().contains(save.file.getPath())
                    || save.file.getPath().contains(currentSave.file.getPath())) {
                throw new Exception("One location includes another. Please do not sync files in the same path");
            }
        }
        this.saves.put(save.name, save);
        DataManager.save(this);
    }
}
