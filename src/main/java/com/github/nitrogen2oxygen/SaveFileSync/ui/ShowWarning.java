package com.github.nitrogen2oxygen.SaveFileSync.ui;

import javax.swing.*;
import java.awt.event.*;

public class ShowWarning extends JDialog {
    private JPanel contentPane;
    private JButton buttonContinue;
    private JButton buttonCancel;
    private JLabel warningMessage;

    public Boolean cont;

    public ShowWarning(String message) {
        setContentPane(contentPane);
        setModal(true);
        setTitle("Warning!");
        getRootPane().setDefaultButton(buttonContinue);
        warningMessage.setText(message);
        buttonContinue.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        cont = true;
        dispose();
    }

    private void onCancel() {
        cont = false;
        dispose();
    }

    public static boolean main(String message) {
        ShowWarning dialog = new ShowWarning(message);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return dialog.cont;
    }
}
