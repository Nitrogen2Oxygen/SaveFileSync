package com.github.nitrogen2oxygen.savefilesync.ui.dialog;

import com.github.nitrogen2oxygen.savefilesync.client.save.Save;
import com.github.nitrogen2oxygen.savefilesync.client.save.SaveDirectory;
import com.github.nitrogen2oxygen.savefilesync.client.save.SaveFile;
import com.github.nitrogen2oxygen.savefilesync.util.Constants;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SaveFileManager extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel directoryManager;
    private JPanel fileManager;
    private JComboBox<String> saveTypeComboBox;
    private JPanel noManager;
    private JPanel managerPanel;
    private JTextField fileNameField;
    private JTextField fileLocationField;
    private JButton fileBrowseButton;
    private JTextField directoryNameField;
    private JTextField directoryLocationField;
    private JButton directoryBrowseButton;

    private Save save;
    public Boolean saveChanges;

    public SaveFileManager() {
        this(null);
    }

    public SaveFileManager(Save save) {
        this.save = save;
        setContentPane(contentPane);
        setTitle(Constants.APP_NAME + " - New Save");
        setLocationRelativeTo(null);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        // TODO: Add edit save data to input fields

        buttonCancel.addActionListener(e -> onCancel());
        buttonOK.addActionListener(e -> onOK());

        // Initialize the save type combo box
        saveTypeComboBox.addItem("File or Directory?");
        saveTypeComboBox.addItem("File");
        saveTypeComboBox.addItem("Directory");
        CardLayout cl = (CardLayout) managerPanel.getLayout();
        saveTypeComboBox.addActionListener(e -> {
            String option = (String) saveTypeComboBox.getSelectedItem();
            switch (Objects.requireNonNull(option)) {
                case "File":
                    cl.show(managerPanel, "file");
                    break;
                case "Directory":
                    cl.show(managerPanel, "directory");
                    break;
                default:
                    cl.show(managerPanel, "none");
                    break;
            }
        });
        cl.show(managerPanel, "none");

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        fileBrowseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // Accept files ONLY
            int chooseFile = fileChooser.showOpenDialog(this);
            if (chooseFile == JFileChooser.APPROVE_OPTION) {
                fileLocationField.setText(fileChooser.getSelectedFile().toString());
            }
        });
        directoryBrowseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Accept directories ONLY
            int chooseFile = fileChooser.showOpenDialog(this);
            if (chooseFile == JFileChooser.APPROVE_OPTION) {
                directoryLocationField.setText(fileChooser.getSelectedFile().toString());
            }
        });
    }

    private void onCancel() {
        saveChanges = false;
        dispose();
    }

    private void onOK() {
        String option = (String) saveTypeComboBox.getSelectedItem();
        switch (Objects.requireNonNull(option)) {
            case "File":
                // Build save file with information
                String fileName = fileNameField.getText();
                if (!isValidName(fileName)) {
                    JOptionPane.showMessageDialog(this,
                            "Name cannot contain any special characters!", "Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                File file = new File(fileLocationField.getText());
                if (!file.isFile()) {
                    JOptionPane.showMessageDialog(this,
                            "Directories cannot be registered to single save files", "Create Save Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                save = new SaveFile(fileName, file);
                break;
            case "Directory":
                // Build save directory
                String directoryName = directoryNameField.getText();
                if (!isValidName(directoryName)) {
                    JOptionPane.showMessageDialog(this,
                            "Name cannot contain any special characters!", "Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                File directory = new File(directoryLocationField.getText());
                if (!directory.isDirectory()) {
                    JOptionPane.showMessageDialog(this,
                            "Directories cannot be registered to single save files", "Create Save Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                save = new SaveDirectory(directoryName, directory);
                // TODO: Add extra components to directory (exclusions and such)
                break;
            default:
                JOptionPane.showMessageDialog(this,
                        "Please choose a save type to begin!", "Create Save Error!", JOptionPane.ERROR_MESSAGE);
                return;
        }
        saveChanges = true;
        dispose();
    }

    private static boolean isValidName(String name) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9 ]*$");
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    public static Save main() {
        SaveFileManager dialog = new SaveFileManager();
        dialog.pack();
        dialog.setVisible(true);
        if (!dialog.saveChanges) return null;
        return dialog.save;
    }

    public static Save edit(String name, String path) {
        // TODO: add edit
        SaveFileManager dialog = new SaveFileManager();
        dialog.pack();
        dialog.setVisible(true);
        if (!dialog.saveChanges) return null;
        return dialog.save;
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
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        managerPanel = new JPanel();
        managerPanel.setLayout(new CardLayout(0, 0));
        contentPane.add(managerPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        directoryManager = new JPanel();
        directoryManager.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        managerPanel.add(directoryManager, "directory");
        final JLabel label1 = new JLabel();
        label1.setText("Name:");
        directoryManager.add(label1, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Directory:");
        directoryManager.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        directoryNameField = new JTextField();
        directoryNameField.setText("");
        directoryManager.add(directoryNameField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        directoryLocationField = new JTextField();
        directoryManager.add(directoryLocationField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        directoryBrowseButton = new JButton();
        directoryBrowseButton.setText("Browse...");
        directoryManager.add(directoryBrowseButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fileManager = new JPanel();
        fileManager.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        managerPanel.add(fileManager, "file");
        final JLabel label3 = new JLabel();
        label3.setText("Name:");
        fileManager.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("File:");
        fileManager.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fileNameField = new JTextField();
        fileManager.add(fileNameField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        fileLocationField = new JTextField();
        fileManager.add(fileLocationField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        fileBrowseButton = new JButton();
        fileBrowseButton.setText("Browse...");
        fileManager.add(fileBrowseButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noManager = new JPanel();
        noManager.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        managerPanel.add(noManager, "none");
        saveTypeComboBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        saveTypeComboBox.setModel(defaultComboBoxModel1);
        contentPane.add(saveTypeComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
