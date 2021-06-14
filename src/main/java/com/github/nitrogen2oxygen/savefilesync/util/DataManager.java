package com.github.nitrogen2oxygen.savefilesync.util;

import com.github.nitrogen2oxygen.savefilesync.client.ClientData;
import com.github.nitrogen2oxygen.savefilesync.client.Settings;
import com.github.nitrogen2oxygen.savefilesync.client.save.Save;
import com.github.nitrogen2oxygen.savefilesync.server.DataServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class DataManager {

    public static void init() {
        init(FileLocations.getDataDirectory());
    }
    public static void init(String location) {
        File clientDataFolder = new File(location);
        if (!clientDataFolder.isDirectory() || !clientDataFolder.exists()) {
            boolean mkdir = clientDataFolder.mkdir();
            if (!mkdir) {
                JOptionPane.showMessageDialog(null,
                        "Cannot create data folder!", "Error!", JOptionPane.ERROR_MESSAGE); // In case the directory can't be made for some reason, throw an error to the user
                System.exit(1);
            }
        }
    }

    public static void save(ClientData data) {
        save(data, FileLocations.getDataDirectory());
    }
    public static void save(ClientData data, String directory) {
        DataServer dataServer = data.getServer();
        Settings settings = data.getSettings();
        List<Save> saves = data.getSaveList();

        /* Save the settings to config.properties */
        try {
            Properties props = settings.toProperties();
            File configFile = new File(FileLocations.getConfigFile(directory));
            configFile.createNewFile();
            FileOutputStream stream = new FileOutputStream(configFile);
            props.store(stream, "");
            stream.close();

            /* Save the dataServer to dataServer.ser */
            File serverFile = new File(FileLocations.getServerFile(directory));
            serverFile.createNewFile();
            ObjectOutputStream serverStream = new ObjectOutputStream(new FileOutputStream(serverFile));
            serverStream.writeObject(dataServer);
            serverStream.close();


            /* Save the saves to /saves */
            File savesDirectory = new File(FileLocations.getSaveDirectory(directory));
            savesDirectory.mkdirs();
            FileUtils.cleanDirectory(savesDirectory); // Clear any deleted save files or renamed ones
            for (Save save : saves) {
                File saveJsonFile = new File(savesDirectory, save.getName() + ".json");
                saveJsonFile.createNewFile();
                String saveData = save.toJSON();
                FileOutputStream saveStream = new FileOutputStream(saveJsonFile);
                saveStream.write(saveData.getBytes(StandardCharsets.UTF_8));
                saveStream.close();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static ClientData load() {
        updateSaveLocation();
        return load(FileLocations.getDataDirectory());
    }

    public static ClientData load(String directory) {
        updateSaveLocation();
        Settings settings = null;
        DataServer dataServer = null;
        HashMap<String, Save> saves = new HashMap<>();


        try {
            /* Get settings */
            File configFile = new File(FileLocations.getConfigFile(directory));
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
            File serverFile = new File(FileLocations.getServerFile(directory));
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

            File saveDirectory = new File(FileLocations.getSaveDirectory(directory));
            if (saveDirectory.exists()) {
                File[] fileList = saveDirectory.listFiles();
                if (fileList != null) {
                    for (File file : fileList) {
                        FileInputStream stream = new FileInputStream(file);
                        String saveString = IOUtils.toString(stream, StandardCharsets.UTF_8);
                        stream.close();
                        JSONObject object = new JSONObject(saveString);
                        String name = object.getString("name");
                        try {
                            saves.put(name, Saves.buildFromJSON(object));
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(null, e.getMessage(), "Load error", JOptionPane.ERROR_MESSAGE);
                            System.exit(1);
                        }
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

    private static void updateSaveLocation() {
        File oldDir = new File(FileLocations.getOldDataDirectory());
        File newDir = new File(FileLocations.getDataDirectory());
        try {
            if (oldDir.isDirectory() && oldDir.exists()) {
                Files.walk(Paths.get(oldDir.toString()))
                        .forEach(source -> {
                            Path destination = Paths.get(newDir.toString(), source.toString()
                                    .substring(oldDir.toString().length()));
                            try {
                                Files.copy(source, destination);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                FileUtils.cleanDirectory(oldDir);
                FileUtils.deleteDirectory(oldDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
