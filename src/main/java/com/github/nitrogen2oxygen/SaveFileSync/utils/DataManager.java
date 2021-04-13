package com.github.nitrogen2oxygen.SaveFileSync.utils;

import com.github.nitrogen2oxygen.SaveFileSync.client.ClientData;

import javax.swing.*;
import java.io.*;
import java.util.Objects;

public class DataManager {

    /* Saves data to the SaveFileSync/data.ser. This function should be called anytime there's change in data */
    public static void save(ClientData data) {
        File file = new File(Constants.getDataFile());
        ObjectOutputStream objectOutputStream = null;
        try {
            file.createNewFile();
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            objectOutputStream.writeObject(data);
        } catch (IOException e) { // If the file isn't found, try again but create a new file.
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error has occurred when saving data to " + Constants.getDataFile(), "Save Error!", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                Objects.requireNonNull(objectOutputStream).close();
            } catch (IOException ignored) {}
        }
    }

    /* Loads data from the SaveFileSync/data.ser */
    public static ClientData load() {
        File file = new File(Constants.getDataFile());
        if (!file.exists()) return new ClientData();
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
            return (ClientData) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error has occurred when saving data to " + Constants.getDataFile(), "Save Error!", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

}
