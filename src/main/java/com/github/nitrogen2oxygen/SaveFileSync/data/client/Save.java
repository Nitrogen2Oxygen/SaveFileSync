package com.github.nitrogen2oxygen.SaveFileSync.data.client;

import java.io.File;

public class Save {
    public File file;
    public String name;

    public Save(String name, File file) {
        this.name = name;
        this.file = file;
    }
}
