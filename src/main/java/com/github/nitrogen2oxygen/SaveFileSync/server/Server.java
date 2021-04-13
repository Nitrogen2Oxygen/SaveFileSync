package com.github.nitrogen2oxygen.SaveFileSync.server;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Server implements java.io.Serializable {
    private static final long serialVersionUID = 5800662342970946726L;

    public Server() {
    }

    /* Used to identify the type in most cases */
    public abstract String serverDisplayName();

    /* Gets the name of the host server */
    public abstract String getHostName();

    /* Sets all the data necessary for the server */
    public abstract void setData(HashMap<String, String> args);

    /* Gets the data in the same form its able to be set in */
    public abstract HashMap<String, String> getData();

    /* Lists the names of all saves on the server */
    public abstract ArrayList<String> getSaveNames();

    /* Gets the raw data of the ZIP file from a given name */
    public abstract byte[] getSaveData(String name);

    /* Uploads a zip file with the save file contents */
    public abstract void uploadSaveData(String name, byte[] data) throws Exception;

    /* Verifies if the server is working properly */
    public abstract Boolean verifyServer();

    /* Create a server object */
    public static Server ServerFactory(String type) {
        switch (type) {
            case "WebDAV":
                return new WebDavServer();
            case "Google Drive":
                return new GoogleDriveServer();
            case "Dropbox":
                return new DropboxServer();
            default:
                return null;
        }
    }
}
