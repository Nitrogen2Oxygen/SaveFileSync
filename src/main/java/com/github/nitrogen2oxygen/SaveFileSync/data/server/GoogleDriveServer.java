package com.github.nitrogen2oxygen.SaveFileSync.data.server;

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
    public String[] getSaveNames() {
        return new String[0];
    }

    @Override
    public byte[] getSaveData(String name) {
        return new byte[0];
    }

    @Override
    public void uploadSaveData(byte[] data) {

    }

    @Override
    public String getServerData() {
        return null;
    }

    @Override
    public void setServerData() {

    }

    @Override
    public Boolean verifyServer() {
        return null;
    }
}
