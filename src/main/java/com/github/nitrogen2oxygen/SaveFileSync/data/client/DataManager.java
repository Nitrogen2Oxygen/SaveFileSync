package com.github.nitrogen2oxygen.SaveFileSync.data.client;

import com.github.nitrogen2oxygen.SaveFileSync.ui.ShowError;
import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataManager {

    /* Saves data to the SaveFileSync/data.ser. This function should be called anytime there's change in data */
    public static void save(ClientData data, File directory) {
        File file = new File(Paths.get(directory.toString(), "data.json").toString());
        try {
            String json = data.toJson();
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            ShowError.main("An error has occurred when loading data from " + file.toString());
        }
    }

    /* Loads data from the SaveFileSync/data.ser */
    public static ClientData load(File directory) {
        File file = new File(Paths.get(directory.toString(), "data.json").toString());
        if (!file.exists()) return new ClientData(directory);
        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(file.toPath());
            return gson.fromJson(reader, ClientData.class);
        } catch (Exception e) {
            e.printStackTrace();
            ShowError.main("An error has occurred when loading data from " + file.toString()); // TODO: Finish handling the error by actually fixing the error
            return null;
        }
    }
}
