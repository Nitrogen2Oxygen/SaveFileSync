package com.github.nitrogen2oxygen.SaveFileSync;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.nitrogen2oxygen.SaveFileSync.data.client.ClientData;
import com.github.nitrogen2oxygen.SaveFileSync.utils.Constants;
import com.github.nitrogen2oxygen.SaveFileSync.utils.DataManager;
import com.github.nitrogen2oxygen.SaveFileSync.ui.SaveFileSyncUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class App {

    public static void main(String[] args) {
        FlatDarkLaf.install();

        /* Create a progress bar */
        JFrame loadingFrame = new JFrame("Loading Application...");
        JProgressBar pb = new JProgressBar();
        pb.setMinimum(0);
        pb.setMaximum(4);
        pb.setStringPainted(true);
        loadingFrame.setLayout(new FlowLayout());
        loadingFrame.getContentPane().add(pb);
        loadingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loadingFrame.pack();
        loadingFrame.setLocationRelativeTo(null);
        loadingFrame.setVisible(true);
       /* Obtain the client data from <USER_HOME>/SaveFileSync */
        File clientDataFolder = new File(Constants.dataDirectory());
        if (!clientDataFolder.isDirectory() || !clientDataFolder.exists()) {
            boolean mkdir = clientDataFolder.mkdir();
               if (!mkdir) {
                   JOptionPane.showMessageDialog(null,
                           "Cannot create data folder!", "Error!", JOptionPane.ERROR_MESSAGE); // In case the directory can't be made for some reason, throw an error to the user
                   System.exit(1);
               }
        }
        pb.setValue(1);

        /* Create the data object. This object stores any kind of persistent data on the client */
        ClientData data = DataManager.load();
        pb.setValue(2);

        /* Finally, creating the actual UI frame. Communication between front and backend is iffy but we make do */
        JFrame frame = new JFrame("Save File Sync - " + Constants.VERSION);
        frame.setContentPane(new SaveFileSyncUI(data).getRootPanel()); // The UI required a ClientData object to update the lists and such
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DataManager.save(data);
                System.exit(0);
            }
        });
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        pb.setValue(3);

        /* Set the frame visible and load the UI */
        frame.setVisible(true);
        loadingFrame.dispose();
    }
}
