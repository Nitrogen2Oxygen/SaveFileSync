package com.github.nitrogen2oxygen.SaveFileSync;

import com.github.nitrogen2oxygen.SaveFileSync.client.ClientData;
import com.github.nitrogen2oxygen.SaveFileSync.ui.ProgressBar;
import com.github.nitrogen2oxygen.SaveFileSync.utils.Constants;
import com.github.nitrogen2oxygen.SaveFileSync.utils.DataManager;
import com.github.nitrogen2oxygen.SaveFileSync.ui.SaveFileSync;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class App {

    public static void main(String[] args) {
        /* Create a progress bar */
        ProgressBar pb = new ProgressBar("Loading " + Constants.APP_NAME + "...");
        pb.start();
       /* Obtain the client data from <USER_HOME>/SaveFileSync */
        File clientDataFolder = new File(Constants.getDataDirectory());
        if (!clientDataFolder.isDirectory() || !clientDataFolder.exists()) {
            boolean mkdir = clientDataFolder.mkdir();
               if (!mkdir) {
                   JOptionPane.showMessageDialog(null,
                           "Cannot create data folder!", "Error!", JOptionPane.ERROR_MESSAGE); // In case the directory can't be made for some reason, throw an error to the user
                   System.exit(1);
               }
        }
        pb.setPercent(25);

        /* Create the data object. This object stores any kind of persistent data on the client */
        ClientData data = DataManager.load();
        data.getSettings().apply(); // Apply the settings object at startup
        pb.setPercent(50);

        /* Finally, creating the actual UI frame. Communication between front and backend is iffy but we make do */
        JFrame frame = new JFrame(Constants.APP_NAME + " - " + Constants.VERSION);
        frame.setContentPane(new SaveFileSync(data).getRootPanel()); // The UI required a ClientData object to update the lists and such
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
        pb.setPercent(75);

        /* Set the frame visible and load the UI */
        frame.setVisible(true);
        pb.setPercent(100);
        pb.finish();
    }
}
