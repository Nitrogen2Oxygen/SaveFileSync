package com.github.nitrogen2oxygen.savefilesync.server;

import java.util.ArrayList;
import java.util.HashMap;

public class GoogleDriveServer extends Server {
    private static final long serialVersionUID = -5783086008172637458L;

    public GoogleDriveServer() {
        super();
    }

    @Override
    public String serverDisplayName() {
        return "Google Drive";
    }

    @Override
    public String getHostName() {
        return "drive.google.com";
    }

    @Override
    public void setData(HashMap<String, String> args) {

    }

    @Override
    public HashMap<String, String> getData() {
        return null;
    }

    @Override
    public ArrayList<String> getSaveNames() {
        return new ArrayList<>();
    }

    @Override
    public byte[] getSaveData(String name) {
        return new byte[0];
    }

    @Override
    public void uploadSaveData(String name, byte[] data) {

    }

    @Override
    public Boolean verifyServer() {
        return null;
    }
}
