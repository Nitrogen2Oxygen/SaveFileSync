package com.github.nitrogen2oxygen.SaveFileSync.data.client;

import com.github.nitrogen2oxygen.SaveFileSync.data.server.Server;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClientData {

    public File directory; // The save directory cuz why not
    public Server server; // Everything we need server wise is located here
    public List<Save> saves = new ArrayList<>(); // Saves can either be retrieved from the server OR created locally.

    public ClientData(File directory) {
        this.directory = directory;
        DataManager.save(this, directory);
    }

    public void addSave(Save save) throws Exception {
        // Check for duplicates FIRST
        for (Save currentSave : this.saves) {
            if (currentSave.name.equals(save.name)) throw new Exception("Duplicate name detected. Please use unique identifiers for each save file.");
            if (currentSave.file.toString().equals(save.file.toString())) throw new Exception("Duplicate location detected. This save file is already in the system");
            if (currentSave.file.getPath().contains(save.file.getPath())
                    || save.file.getPath().contains(currentSave.file.getPath())) {
                throw new Exception("One location includes another. Please do not sync files in the same path");
            }
        }
        saves.add(save);
        DataManager.save(this, directory);
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
