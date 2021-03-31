package com.github.nitrogen2oxygen.SaveFileSync.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ShowError extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel errorMessage;

    public ShowError(String message) {
        setContentPane(contentPane);
        setModal(true);
        setTitle("An Error has Occurred!");
        errorMessage.setText(message);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> System.exit(1));
    }

    public static void main(String message) {
        ShowError dialog = new ShowError(message);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
