package com.github.nitrogen2oxygen.SaveFileSync.ui;

import com.github.nitrogen2oxygen.SaveFileSync.data.client.ClientData;
import com.github.nitrogen2oxygen.SaveFileSync.data.server.Server;
import com.github.nitrogen2oxygen.SaveFileSync.data.server.ServerManager;

import javax.swing.*;
import java.awt.event.*;

public class ServerManagerUI extends JDialog {
    private JPanel contentPane;
    private JButton saveButton;
    private JButton buttonCancel;
    private JComboBox<String> serverTypeSelector;

    private ClientData clientData;
    private Server server;

    public ServerManagerUI(Server currentServer) {
        this.server = currentServer;

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

        saveButton.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        serverTypeSelector.addActionListener(e -> {
            // TODO: Warn the user before changing this
            String option = (String) serverTypeSelector.getSelectedItem();
            if (option == null || option.equals("None")) {
                server = null; // Removes any kind of server aspect if the server is "none"
            } else {
                server = ServerManager.ServerFactory(option); // Creates a new empty server object when changing the type
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static Server main(ClientData data) {
        ServerManagerUI dialog = new ServerManagerUI(data.server);
        dialog.pack();
        dialog.setVisible(true);
        return dialog.server;
    }
}
