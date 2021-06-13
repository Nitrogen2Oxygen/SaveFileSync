package com.github.nitrogen2oxygen.savefilesync.save;

import java.io.File;

public abstract class Save {
    private final String name;
    private final File file;

    public Save(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public abstract byte[] toZipData() throws Exception;
    public abstract void writeData(byte[] data, boolean makeBackup, boolean forceOverwrite) throws Exception;
    public abstract String toJSON();
}
