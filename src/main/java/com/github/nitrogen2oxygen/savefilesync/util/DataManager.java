package com.github.nitrogen2oxygen.savefilesync.util;

import com.github.nitrogen2oxygen.savefilesync.client.ClientData;
import com.github.nitrogen2oxygen.savefilesync.client.Save;
import com.github.nitrogen2oxygen.savefilesync.client.Settings;
import com.github.nitrogen2oxygen.savefilesync.server.DataServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class DataManager {

    public static void save(ClientData data) {
        DataServer dataServer = data.getServer();
        Settings settings = data.getSettings();
        ArrayList<Save> saves = data.getSaveList();
        // HashMap<String, Save> saves = data.getSaves();

        /* Save the settings to config.properties */
        try {
            Properties props = settings.toProperties();
            File configFile = new File(FileLocations.getConfigFile());
            configFile.createNewFile();
            FileOutputStream stream = new FileOutputStream(configFile);
            props.store(stream, "");
            stream.close();

            /* Save the dataServer to dataServer.ser */
            File serverFile = new File(FileLocations.getServerFile());
            serverFile.createNewFile();
            ObjectOutputStream serverStream = new ObjectOutputStream(new FileOutputStream(serverFile));
            serverStream.writeObject(dataServer);
            serverStream.close();


            /* Save the saves to /saves */
            File savesDirectory = new File(FileLocations.getSaveDirectory());
            savesDirectory.mkdirs();
            FileUtils.cleanDirectory(savesDirectory); // Clear any deleted save files or renamed ones
            for (Save save : saves) {
                File saveFile = new File(savesDirectory, save.getName() + ".json");
                saveFile.createNewFile();
                JSONObject saveObject = new JSONObject();
                saveObject.put("name", save.getName());
                saveObject.put("location", save.getFile().getPath());
                String saveString = saveObject.toString();
                FileOutputStream saveStream = new FileOutputStream(saveFile);
                saveStream.write(saveString.getBytes(StandardCharsets.UTF_8));
                saveStream.close();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static ClientData load() {
        Settings settings = null;
        DataServer dataServer = null;
        HashMap<String, Save> saves = new HashMap<>();


        try {
            /* Get settings */
            File configFile = new File(FileLocations.getConfigFile());
            if (configFile.exists()) {
                Properties props = new Properties();
                FileInputStream stream = new FileInputStream(configFile);
                props.load(stream);
                stream.close();
                settings = new Settings(props);
            } else {
                settings = new Settings();
            }

            /* Get dataServer */
            File serverFile = new File(FileLocations.getServerFile());
            if (serverFile.exists()) {
                try {
                    ObjectInputStream stream = new ObjectInputStream(new FileInputStream(serverFile));
                    dataServer = (DataServer) stream.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Cannot load dataServer data. Resetting...", "Load error", JOptionPane.ERROR_MESSAGE);
                    dataServer = null;
                }
            }

            File saveDirectory = new File(FileLocations.getSaveDirectory());
            if (saveDirectory.exists()) {
                File[] fileList = saveDirectory.listFiles();
                if (fileList != null) {
                    for (File file : fileList) {
                        FileInputStream stream = new FileInputStream(file);
                        String saveString = IOUtils.toString(stream, StandardCharsets.UTF_8);
                        stream.close();
                        JSONObject object = new JSONObject(saveString);
                        String name = object.getString("name");
                        String location = object.getString("location");
                        File saveFile = new File(location);
                        saves.put(name, new Save(name, saveFile));
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Cannot load data. If this persists, please submit an issue on GitHub.",
                    "Load Error!",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        return new ClientData(dataServer, settings, saves);
    }
}
