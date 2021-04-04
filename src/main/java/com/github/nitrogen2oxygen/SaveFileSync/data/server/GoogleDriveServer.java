package com.github.nitrogen2oxygen.SaveFileSync.data.server;

import java.util.ArrayList;
import java.util.HashMap;

public class GoogleDriveServer extends Server {
    public GoogleDriveServer() {
        super();
    }

    @Override
    public String serverDisplayName() {
        return "Google Drive";
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
