package com.github.nitrogen2oxygen.savefilesync;

import com.github.nitrogen2oxygen.savefilesync.client.ClientData;
import com.github.nitrogen2oxygen.savefilesync.util.Constants;
import com.github.nitrogen2oxygen.savefilesync.util.DataManager;
import com.github.nitrogen2oxygen.savefilesync.ui.MainPanel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class App {

    public static void main(String[] args) {
        /* Obtain the client data from <USER_HOME>/SaveFileSync */
        DataManager.init();
        ClientData data = DataManager.load();

        /* Create the actual UI frame */
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
        frame.setVisible(true);
    }
}
