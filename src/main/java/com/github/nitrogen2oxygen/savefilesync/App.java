package com.github.nitrogen2oxygen.savefilesync;

import com.github.nitrogen2oxygen.savefilesync.client.ClientData;
import com.github.nitrogen2oxygen.savefilesync.util.Constants;
import com.github.nitrogen2oxygen.savefilesync.util.DataManager;
import com.github.nitrogen2oxygen.savefilesync.ui.MainPanel;
import com.github.nitrogen2oxygen.savefilesync.util.FileLocations;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class App {

    public static void main(String[] args) {
        /* Obtain the client data from <USER_HOME>/SaveFileSync */
        File clientDataFolder = new File(FileLocations.getDataDirectory());
        if (!clientDataFolder.isDirectory() || !clientDataFolder.exists()) {
            boolean mkdir = clientDataFolder.mkdir();
            if (!mkdir) {
                JOptionPane.showMessageDialog(null,
                        "Cannot create data folder!", "Error!", JOptionPane.ERROR_MESSAGE); // In case the directory can't be made for some reason, throw an error to the user
                System.exit(1);
            }
        }

        /* Create the data object. This object stores any kind of persistent data on the client */
        ClientData data = DataManager.load();
        data.getSettings().apply(); // Apply the settings object at startup

        /* Finally, creating the actual UI frame. Communication between front and backend is iffy but we make do */
        JFrame frame = new JFrame(Constants.APP_NAME + " - " + Constants.VERSION);
        frame.setContentPane(new MainPanel(data).getRootPanel()); // The UI required a ClientData object to update the lists and such
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DataManager.save(data);
                System.exit(0);
            }
        });
        frame.pack();
        frame.setLocationRelativeTo(null);

        /* Set the frame visible and load the UI */
        frame.setVisible(true);
    }
}
