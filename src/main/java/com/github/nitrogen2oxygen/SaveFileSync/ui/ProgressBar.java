package com.github.nitrogen2oxygen.SaveFileSync.ui;

import javax.swing.*;
import java.awt.*;

public class ProgressBar {
    public JFrame frame;
    public JProgressBar progressBar;

    public ProgressBar(String title) {
        frame = new JFrame(title);
        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);
        frame.setLayout(new FlowLayout());
        frame.getContentPane().add(progressBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO: Customize this
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    public void start() {
        frame.setVisible(true);
    }

    public void setPercent(int percent) {
        if (percent < 0 || percent > 100) return;
        progressBar.setValue(percent);
    }

    public void finish() {
        frame.dispose();
    }
}
