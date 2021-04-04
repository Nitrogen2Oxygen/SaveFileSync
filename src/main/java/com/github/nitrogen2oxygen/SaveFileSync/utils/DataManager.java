package com.github.nitrogen2oxygen.SaveFileSync.utils;

import com.github.nitrogen2oxygen.SaveFileSync.data.client.ClientData;
import com.github.nitrogen2oxygen.SaveFileSync.data.server.ServerDeserializer;
import com.github.nitrogen2oxygen.SaveFileSync.data.server.ServerSerializer;
import com.github.nitrogen2oxygen.SaveFileSync.data.server.Server;
import com.github.nitrogen2oxygen.SaveFileSync.ui.ShowError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class DataManager {

    /* Saves data to the SaveFileSync/data.json. This function should be called anytime there's change in data */
    public static void save(ClientData data) {
        File file = new File(Constants.dataFile());
        try {
            Gson gson = dataGson();
            String json = gson.toJson(data);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            ShowError.main("An error has occurred when loading data from " + file.toString());
        }
    }

    /* Loads data from the SaveFileSync/data.json */
    public static ClientData load() {
        File file = new File(Constants.dataFile());
        if (!file.exists()) return new ClientData();
        try {
            Gson gson = dataGson();
            Reader reader = Files.newBufferedReader(file.toPath());
            return gson.fromJson(reader, ClientData.class);
        } catch (Exception e) {
            e.printStackTrace();
            ShowError.main("An error has occurred when loading data from " + file.toString());
            return null;
        }
    }


    public static Gson dataGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Server.class, new ServerDeserializer())
                .registerTypeAdapter(Server.class, new ServerSerializer())
                .create();
    }
}
