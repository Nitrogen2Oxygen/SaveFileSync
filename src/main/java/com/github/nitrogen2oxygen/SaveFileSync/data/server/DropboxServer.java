package com.github.nitrogen2oxygen.SaveFileSync.data.server;

import java.util.ArrayList;
import java.util.HashMap;

public class DropboxServer extends Server {
    private static final long serialVersionUID = 8175793937439456805L;

    @Override
    public String serverDisplayName() {
        return "Dropbox";
    }

    @Override
    public String getHostName() {
        return "dropbox.com";
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
        return null;
    }

    @Override
    public byte[] getSaveData(String name) {
        return new byte[0];
    }

    @Override
    public void uploadSaveData(String name, byte[] data) throws Exception {

    }

    @Override
    public Boolean verifyServer() {
        return null;
    }
}
