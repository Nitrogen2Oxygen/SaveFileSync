package com.github.nitrogen2oxygen.SaveFileSync;

import com.github.nitrogen2oxygen.SaveFileSync.data.client.ClientData;
import com.github.nitrogen2oxygen.SaveFileSync.data.client.DataManager;
import com.github.nitrogen2oxygen.SaveFileSync.ui.SaveFileSyncUI;
import com.github.nitrogen2oxygen.SaveFileSync.ui.ShowError;

import javax.swing.*;
import java.io.File;
import java.nio.file.Paths;

public class App {

    public static void main(String[] args) {
        /* Obtain the client data from <USER_HOME>/SaveFileSync */
        File clientDataFolder = new File(Paths.get(System.getProperty("user.home"), "SaveFileSync").toString());
        if (!clientDataFolder.isDirectory() || !clientDataFolder.exists()) {
            boolean mkdir = clientDataFolder.mkdir();
               if (!mkdir) ShowError.main("Cannot create data directory " + clientDataFolder); // In case the directory can't be made for some reason, throw an error to the user
        }

        /* Create the data object. This object stores any kind of persistent data on the client */
        ClientData data = DataManager.load(clientDataFolder);


        /* Finally, creating the actual UI frame. Communication between front and backend is iffy but we make do */
        JFrame frame = new JFrame("Save File Sync");
        frame.setContentPane(new SaveFileSyncUI(data).getRootPanel()); // The UI required a ClientData object to update the lists and such
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
