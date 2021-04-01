package com.github.nitrogen2oxygen.SaveFileSync.ui;

import com.github.nitrogen2oxygen.SaveFileSync.data.client.Save;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class NewSaveFile extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameTextField;
    private JTextField locationTextField;
    private JLabel nameLabel;
    private JButton browseButton;
    private JLabel locationLabel;

    private Save save;
    public Boolean saveChanges = true;

    public NewSaveFile() {
        setContentPane(contentPane);
        setLocationRelativeTo(null);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonCancel.addActionListener(e -> onCancel());
        buttonOK.addActionListener(e -> {
            File file = new File(locationTextField.getText());
            String name = nameTextField.getText();
            save = new Save(name, file);
            dispose();
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        browseButton.addActionListener(e -> {
            /* We want to choose a file or folder to use. Multi-select files are not allowed, so either 1 folder or 1 file */
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); // Accept files AND directories
            int chooseFile = fileChooser.showOpenDialog(this);
            if (chooseFile == JFileChooser.APPROVE_OPTION) {
                locationTextField.setText(fileChooser.getSelectedFile().toString());
            }
        });
    }

    private void onCancel() {
        saveChanges = false;
        dispose();
    }

    public static Save main() {
        NewSaveFile dialog = new NewSaveFile();
        if (!dialog.saveChanges) return null;
        dialog.pack();
        dialog.setVisible(true);
        return dialog.save;
    }
}
