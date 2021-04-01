package com.github.nitrogen2oxygen.SaveFileSync.ui;

import com.github.nitrogen2oxygen.SaveFileSync.data.client.ClientData;
import com.github.nitrogen2oxygen.SaveFileSync.data.server.Server;
import com.github.nitrogen2oxygen.SaveFileSync.utils.ServerManager;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;

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
    private JTextField webdavUsernameTextField;
    private JTextField webdavUriTextField;
    private JCheckBox webdavUseAuthenticationBox;
    private JPasswordField webdavPasswordField;

    private ClientData clientData;
    private Server server;
    private final Server serverOld; // In case the user cancels, we want to return the exact same server we started with
    public Boolean cancelled = false;

    public ServerManagerUI(Server currentServer) {
        this.server = currentServer;
        this.serverOld = currentServer;

        /* Create logic for the UI */
        DefaultComboBoxModel<String> defaultComboBoxModel = new DefaultComboBoxModel<>();
        defaultComboBoxModel.addElement("None");
        defaultComboBoxModel.addElement("WebDav");
        defaultComboBoxModel.addElement("Google Drive");
        defaultComboBoxModel.setSelectedItem(currentServer != null ? currentServer.serverDisplayName() : "None");
        serverTypeSelector.setModel(defaultComboBoxModel);

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
        webdavUseAuthenticationBox.addActionListener(e -> {
            boolean authentication = webdavUseAuthenticationBox.isSelected();
            if (authentication) {
                webdavUsernameTextField.setEnabled(true);
                webdavPasswordField.setEnabled(true);
            } else {
                webdavUsernameTextField.setEnabled(false);
                webdavPasswordField.setEnabled(false);
                webdavUsernameTextField.setText("");
                webdavPasswordField.setText("");
            }
        });
    }

    private void reloadUI() {
        CardLayout cl = (CardLayout) optionsPanel.getLayout();
        if (server == null) {
            cl.show(optionsPanel, "0");
        } else {
            HashMap<String, String> serverData = server.getData();
            switch (server.serverDisplayName()) {
                case "WebDav":
                    cl.show(optionsPanel, "1");
                    webdavUriTextField.setText(serverData.get("uri"));
                    if (serverData.get("username") != null) {
                        webdavUsernameTextField.setEnabled(true);
                        webdavPasswordField.setEnabled(true);
                        webdavUseAuthenticationBox.setSelected(true);
                        webdavUsernameTextField.setText(serverData.get("username"));
                        webdavPasswordField.setText(serverData.get("password"));
                    } else {
                        webdavUsernameTextField.setEnabled(false);
                        webdavPasswordField.setEnabled(false);
                        webdavUseAuthenticationBox.setSelected(false);
                    }
                    break;
                case "Google Drive":
                    cl.show(optionsPanel, "2");
                    break;
            }
        }


        pack();
    }

    private void onOK() {
        cancelled = false;

        /* Take any front end data and send it to the server object before returning */
        if (server != null) {
                switch (server.serverDisplayName()) {
                    case "WebDav":
                        HashMap<String, String> newData = new HashMap<>();
                        newData.put("uri", webdavUriTextField.getText());
                        if (webdavUseAuthenticationBox.isSelected()) {
                            if (webdavUsernameTextField.getText().length() > 0 && webdavPasswordField.getPassword().length > 0) {
                                newData.put("username", webdavUsernameTextField.getText());
                                newData.put("password", new String(webdavPasswordField.getPassword()));
                            } else {
                                ShowWarning.main("A username AND password is required if you're using WebDav authentication!");
                                return;
                            }
                        }
                        server.setData(newData);
                    case "Google Drive":
                        break;
            }
            Boolean isValid = server.verifyServer();
            if (isValid == null || !isValid) {
                JOptionPane.showMessageDialog(this,
                        "Something is wrong with your config. Check to make sure that all the credentials are correct.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        panel2.add(saveButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        serverTypeSelector = new JComboBox();
        panel3.add(serverTypeSelector, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new CardLayout(0, 0));
        optionsPanel.setEnabled(true);
        panel3.add(optionsPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        webdavPanel = new JPanel();
        webdavPanel.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        optionsPanel.add(webdavPanel, "1");
        final JLabel label1 = new JLabel();
        label1.setText("Use Authentication?");
        webdavPanel.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Username");
        webdavPanel.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Password");
        webdavPanel.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        webdavUsernameTextField = new JTextField();
        webdavPanel.add(webdavUsernameTextField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("URI");
        webdavPanel.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        webdavUriTextField = new JTextField();
        webdavPanel.add(webdavUriTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        webdavLabel = new JLabel();
        webdavLabel.setText("WebDav Setup");
        webdavPanel.add(webdavLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        webdavUseAuthenticationBox = new JCheckBox();
        webdavUseAuthenticationBox.setText("CheckBox");
        webdavPanel.add(webdavUseAuthenticationBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        webdavPasswordField = new JPasswordField();
        webdavPanel.add(webdavPasswordField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        googleDrivePanel = new JPanel();
        googleDrivePanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        optionsPanel.add(googleDrivePanel, "2");
        googleDriveLabel = new JLabel();
        googleDriveLabel.setText("Google Drive Setup");
        googleDrivePanel.add(googleDriveLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        googleDrivePanel.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        emptyPanel = new JPanel();
        emptyPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        optionsPanel.add(emptyPanel, "0");
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
