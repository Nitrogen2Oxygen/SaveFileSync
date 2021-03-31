package com.github.nitrogen2oxygen.SaveFileSync.ui;

import com.github.nitrogen2oxygen.SaveFileSync.data.client.ClientData;
import com.github.nitrogen2oxygen.SaveFileSync.data.server.Server;

import javax.swing.*;
import java.awt.event.*;

public class ServerManagerUI extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox comboBox1;

    private ClientData clientData;
    private final Server server;

    public ServerManagerUI(Server server) {
        this.server = server;
        setContentPane(contentPane);
        setLocationRelativeTo(null);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

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
