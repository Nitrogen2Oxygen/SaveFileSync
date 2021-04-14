package com.github.nitrogen2oxygen.SaveFileSync.utils;

import com.github.nitrogen2oxygen.SaveFileSync.client.ClientData;
import com.github.nitrogen2oxygen.SaveFileSync.client.Save;
import com.github.nitrogen2oxygen.SaveFileSync.client.Settings;
import com.github.nitrogen2oxygen.SaveFileSync.server.Server;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;

public class DataManager {

    public static void save(ClientData data) {
        Server server = data.getServer();
        Settings settings = data.getSettings();
        HashMap<String, Save> saves = data.getSaves();

        /* Save the settings to config.properties */
        try {
            Properties props = settings.toProperties();
            File configFile = new File(Constants.getConfigFile());
            configFile.createNewFile();
            FileOutputStream stream = new FileOutputStream(configFile);
            props.store(stream, "");
            stream.close();

            /* Save the server to server.ser */
            File serverFile = new File(Constants.getServerFile());
            serverFile.createNewFile();
            ObjectOutputStream serverStream = new ObjectOutputStream(new FileOutputStream(serverFile));
            serverStream.writeObject(server);
            serverStream.close();


            /* Save the saves to /saves */
            File savesDirectory = new File(Constants.getSaveDirectory());
            savesDirectory.mkdirs();
            FileUtils.cleanDirectory(savesDirectory); // Clear any deleted save files or renamed ones
            for (String key : saves.keySet()) {
                File saveFile = new File(savesDirectory, key + ".json");
                saveFile.createNewFile();
                Save save = saves.get(key);
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
        Server server = null;
        HashMap<String, Save> saves = new HashMap<>();


        try {
            /* Get settings */
            File configFile = new File(Constants.getConfigFile());
            if (configFile.exists()) {
                Properties props = new Properties();
                FileInputStream stream = new FileInputStream(configFile);
                props.load(stream);
                stream.close();
                settings = new Settings(props);
            } else {
                settings = new Settings();
            }

            /* Get server */
            File serverFile = new File(Constants.getServerFile());
            if (serverFile.exists()) {
                ObjectInputStream stream = new ObjectInputStream(new FileInputStream(serverFile));
                server = (Server) stream.readObject();
            }

            File saveDirectory = new File(Constants.getSaveDirectory());
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
        } catch (IOException | ClassNotFoundException | JSONException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Cannot load data. If this persists, please submit an issue on GitHub.",
                    "Load Error!",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        return new ClientData(server, settings, saves);
    }
}
