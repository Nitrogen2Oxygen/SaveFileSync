package com.github.nitrogen2oxygen.SaveFileSync.ui;

import com.github.nitrogen2oxygen.SaveFileSync.data.client.ClientData;
import com.github.nitrogen2oxygen.SaveFileSync.data.server.Server;
import com.github.nitrogen2oxygen.SaveFileSync.data.server.ServerManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ServerManagerUI extends JDialog {
    private JPanel contentPane;
    private JButton saveButton;
    private JButton buttonCancel;
    private JComboBox<String> serverTypeSelector;
    private JPanel optionsPanel;
    private JPanel webdavPanel;
    private JPanel googleDrivePanel;
    private JPanel emptyPanel;
    private JLabel webdavLabel;
    private JLabel googleDriveLabel;

    private ClientData clientData;
    private Server server;
    private final Server serverOld;
    public Boolean cancelled = false;

    public ServerManagerUI(Server currentServer) {
        this.server = currentServer;
        this.serverOld = currentServer;

        /* Create logic for the UI */
        DefaultComboBoxModel<String> dcbm = new DefaultComboBoxModel<>();
        dcbm.addElement("None");
        dcbm.addElement("WebDav");
        dcbm.addElement("Google Drive");
        dcbm.setSelectedItem(currentServer != null ? currentServer.serverDisplayName() : "None");
        serverTypeSelector.setModel(dcbm);

        /* Do all the lame UI stuff */
        setContentPane(contentPane);
        setTitle("Manage Server Settings");
        setLocationRelativeTo(null);
        setModal(true);
        getRootPane().setDefaultButton(saveButton);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        /* Actions ig */
        saveButton.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        serverTypeSelector.addActionListener(e -> {
            if (server != null) {
                boolean cont = ShowWarning.main("Changing this option will RESET your server config. Please only do this if you're actually changing data servers!");
                if (!cont) {
                    serverTypeSelector.setSelectedItem(server == null ? "None" : server.serverDisplayName());
                    return;
                }
            }
            String option = (String) serverTypeSelector.getSelectedItem();
            if (option == null || option.equals("None")) {
                server = null; // Removes any kind of server aspect if the server is "none"
            } else {
                server = ServerManager.ServerFactory(option); // Creates a new empty server object when changing the type
            }
            reloadUI();
        });

        // Reload the UI before rendering
        reloadUI();
    }

    private void reloadUI() {
        CardLayout cl = (CardLayout) optionsPanel.getLayout();
        if (server == null) {
            cl.show(optionsPanel, "0");
        } else {
            switch (server.serverDisplayName()) {
                case "WebDav":
                    cl.show(optionsPanel, "1");
                    break;
                case "Google Drive":
                    cl.show(optionsPanel, "2");;
                    break;
            }
        }

        pack();
    }

    private void onOK() {
        cancelled = false;
        dispose();
    }

    private void onCancel() {
        cancelled = true;
        dispose();
    }

    public static Server main(ClientData data) {
        ServerManagerUI dialog = new ServerManagerUI(data.server);
        dialog.pack();
        dialog.setVisible(true);
        if (dialog.cancelled) return dialog.serverOld;
        return dialog.server;
    }
}
