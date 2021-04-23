package com.github.nitrogen2oxygen.savefilesync.ui.dialog;

import com.github.nitrogen2oxygen.savefilesync.client.ClientData;
import com.github.nitrogen2oxygen.savefilesync.server.*;
import com.github.nitrogen2oxygen.savefilesync.util.Constants;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class ServerOptions extends JDialog {
    // General
    private JPanel contentPane;
    private JButton saveButton;
    private JButton buttonCancel;
    private JComboBox<ServerType> serverTypeSelector;
    private JPanel optionsPanel;
    private JPanel emptyPanel;

    // WebDAV
    private JPanel webdavPanel;
    private JPanel dropboxPanel;
    private JLabel webdavLabel;
    private JTextField webdavUsernameTextField;
    private JTextField webdavUriTextField;
    private JCheckBox webdavUseAuthenticationBox;
    private JPasswordField webdavPasswordField;

    // Dropbox
    private JTextField dropboxCodeField;
    private JButton dropboxLinkButton;

    // OneDrive
    private JPanel oneDrivePanel;
    private JButton oneDriveLinkButton;
    private JTextField oneDriveApiCode;

    private DataServer dataServer;
    private final DataServer oldDataServer;
    public Boolean cancelled = false;
    private String dropboxVerifier;

    public ServerOptions(DataServer currentDataServer) {
        this.dataServer = currentDataServer;
        this.oldDataServer = currentDataServer;

        /* Create logic for the UI */
        DefaultComboBoxModel<ServerType> defaultComboBoxModel = new DefaultComboBoxModel<>();
        for (ServerType type : ServerType.values()) {
            defaultComboBoxModel.addElement(type);
        }
        defaultComboBoxModel.setSelectedItem(currentDataServer != null ? currentDataServer.getServerType() : ServerType.NONE);
        serverTypeSelector.setModel(defaultComboBoxModel);
        serverTypeSelector.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel();
            label.setText(DataServers.getDisplayName(value));
            label.setOpaque(true);
            if (isSelected) {
                label.setForeground(list.getSelectionForeground());
                label.setBackground(list.getSelectionBackground());
            } else {
                label.setForeground(list.getForeground());
                label.setBackground(list.getBackground());
            }
            return label;
        });

        /* Do all the lame UI stuff */
        setContentPane(contentPane);
        setTitle(Constants.APP_NAME + " - Manage Server");
        setLocationRelativeTo(null);
        setModal(true);
        getRootPane().setDefaultButton(saveButton);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        /* Actions ig */
        saveButton.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        serverTypeSelector.addActionListener(e -> {
            if (dataServer != null) {
                int opt = JOptionPane.showConfirmDialog(null, "Changing this option will reset your Server config. Continue?", "Warning!", JOptionPane.YES_NO_OPTION);
                if (opt == 1) {
                    serverTypeSelector.setSelectedItem(dataServer == null ? ServerType.NONE : dataServer.getServerType());
                    return;
                }
            }
            ServerType option = (ServerType) serverTypeSelector.getSelectedItem();
            if (option == null) {
                dataServer = null; // Removes any kind of server aspect if the server is "none"
            } else {
                dataServer = DataServers.ServerFactory(option); // Creates a new empty DataServer object when changing the type
            }
            reloadUI();
        });
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
        dropboxLinkButton.addActionListener(e -> {
            try {
                if (dropboxVerifier == null) dropboxVerifier = DropboxDataServer.generateVerifier();
                String url = "https://www.dropbox.com/oauth2/authorize" +
                        "?response_type=code&token_access_type=offline" +
                        "&redirect_uri=http://localhost:" + Constants.CALLBACK_PORT +
                        "&client_id=" + Constants.DROPBOX_APP_ID +
                        "&code_challenge=" + DropboxDataServer.getChallenge(dropboxVerifier) +
                        "&code_challenge_method=S256";
                Desktop.getDesktop().browse(new URI(url));
                dropboxCodeField.setText(DropboxDataServer.getApiKey());
            } catch (IOException | URISyntaxException | NoSuchAlgorithmException ee) {
                ee.printStackTrace();
            }
        });
        oneDriveLinkButton.addActionListener(e -> {
            try {
                String url = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize" +
                        "?client_id=" + Constants.ONEDRIVE_APP_ID +
                        "&scope=" + String.join("%20", OneDriveDataServer.SCOPES) +
                        "&response_type=code";
                Desktop.getDesktop().browse(new URI(url));
                oneDriveApiCode.setText(OneDriveDataServer.getApiKey());
            } catch (IOException | URISyntaxException ee) {
                ee.printStackTrace();
            }
        });

        // Reload the UI before rendering
        reloadUI();
    }

    private void reloadUI() {
        CardLayout cl = (CardLayout) optionsPanel.getLayout();
        if (dataServer == null) {
            cl.show(optionsPanel, "0");
        } else {
            HashMap<String, String> serverData = dataServer.getData();
            ServerType serverType = dataServer.getServerType();
            if (serverType == null) {
                cl.show(optionsPanel, "0");
                return;
            }
            switch (serverType) {
                case WEBDAV:
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
                case DROPBOX:
                    cl.show(optionsPanel, "3");
                    if (serverData.get("apiKey") != null) {
                        dropboxCodeField.setText(serverData.get("apiKey"));
                    }
                    if (serverData.get("verifier") != null) {
                        dropboxVerifier = serverData.get("verifier");
                    }
                    break;
                case ONEDRIVE:
                    cl.show(optionsPanel, "2");
                    if (serverData.get("apiKey") != null) {
                        oneDriveApiCode.setText(serverData.get("apiKey"));
                    }
                    break;
            }
        }


        pack();
    }

    private void onOK() {
        cancelled = false;

        /* Take any front end data and send it to the dataServer object before returning */
        if (dataServer != null) {
            switch (dataServer.getServerType()) {
                case WEBDAV:
                    HashMap<String, String> newData = new HashMap<>();
                    if (webdavUriTextField.getText().length() == 0) {
                        JOptionPane.showMessageDialog(null, "A WebDAV server URL must be specified!", "Error!", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    newData.put("uri", webdavUriTextField.getText());
                    if (webdavUseAuthenticationBox.isSelected()) {
                        if (webdavUsernameTextField.getText().length() > 0 && webdavPasswordField.getPassword().length > 0) {
                            newData.put("username", webdavUsernameTextField.getText());
                            newData.put("password", new String(webdavPasswordField.getPassword()));
                        } else {
                            JOptionPane.showMessageDialog(null, "A username AND password is required if you're using WebDav authentication!", "Error!", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    dataServer.setData(newData);
                    break;
                case DROPBOX:
                    HashMap<String, String> dropboxData = new HashMap<>();
                    if (dropboxCodeField.getText().length() == 0) {
                        JOptionPane.showMessageDialog(null, "A valid key is required to continue", "Error!", JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        dropboxData.put("apiKey", dropboxCodeField.getText());
                        dropboxData.put("verifier", dropboxVerifier);
                    }

                    dataServer.setData(dropboxData);
                    break;
                case ONEDRIVE:
                    HashMap<String, String> oneDriveData = new HashMap<>();
                    if (oneDriveApiCode.getText().length() == 0) {
                        JOptionPane.showMessageDialog(null, "A valid key is required to continue", "Error!", JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        oneDriveData.put("apiKey", oneDriveApiCode.getText());
                    }

                    dataServer.setData(oneDriveData);
                    break;
                default:
                    dataServer = null;
            }
            boolean isValid = dataServer == null || dataServer.verifyServer();
            if (!isValid) {
                JOptionPane.showMessageDialog(this,
                        "Cannot verify connection to data server! Double check your credentials.",
                        "Error!",
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

    public DataServer getServer() {
        return dataServer;
    }

    public DataServer getOldServer() {
        return oldDataServer;
    }

    public static DataServer main(ClientData data) {
        ServerOptions dialog = new ServerOptions(data.getServer());
        dialog.pack();
        dialog.setVisible(true);
        if (dialog.cancelled) return dialog.getOldServer();
        return dialog.getServer();
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
        dropboxPanel = new JPanel();
        dropboxPanel.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        optionsPanel.add(dropboxPanel, "3");
        final JLabel label5 = new JLabel();
        label5.setText("Click here and login with dropbox:");
        dropboxPanel.add(label5, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("A code will be pasted here on completion:");
        dropboxPanel.add(label6, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dropboxCodeField = new JTextField();
        dropboxCodeField.setEditable(false);
        dropboxPanel.add(dropboxCodeField, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        dropboxLinkButton = new JButton();
        dropboxLinkButton.setText("Login with Dropbox");
        dropboxPanel.add(dropboxLinkButton, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        emptyPanel = new JPanel();
        emptyPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        optionsPanel.add(emptyPanel, "0");
        oneDrivePanel = new JPanel();
        oneDrivePanel.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        optionsPanel.add(oneDrivePanel, "2");
        final JLabel label7 = new JLabel();
        label7.setText("Click here to login with Microsoft:");
        oneDrivePanel.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        oneDrivePanel.add(spacer2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        oneDriveLinkButton = new JButton();
        oneDriveLinkButton.setText("Login With Microsoft");
        oneDrivePanel.add(oneDriveLinkButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("A code will be pasted here on completion:");
        oneDrivePanel.add(label8, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        oneDriveApiCode = new JTextField();
        oneDriveApiCode.setEditable(false);
        oneDrivePanel.add(oneDriveApiCode, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
