package com.github.nitrogen2oxygen.savefilesync.save;

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
        return null;
    }
}
