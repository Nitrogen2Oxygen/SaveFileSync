package com.github.nitrogen2oxygen.savefilesync.save;

import org.json.JSONObject;

import java.io.File;

public class SaveDirectory extends Save {
    public SaveDirectory(String name, File file) {
        super(name, file);
    }

    @Override
    public byte[] toZipData() throws Exception {
        return new byte[0];
    }

    @Override
    public void writeData(byte[] data, boolean makeBackup, boolean forceOverwrite) throws Exception {

    }

    @Override
    public String toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "directory");
        json.put("name", getName());
        json.put("location", getFile().getPath());
        return json.toString();
    }
}
