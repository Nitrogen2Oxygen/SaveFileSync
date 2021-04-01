package com.github.nitrogen2oxygen.SaveFileSync.data.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

public abstract class Server {
    public Server() {}

    /* Used to identify the type in most cases */
    public abstract String serverDisplayName();

    /* Sets all the data necessary for the server */
    public abstract void setData(HashMap<String, String> args);

    /* Gets the data in the same form its able to be set in */
    public abstract HashMap<String, String> getData();

    /* Initialize the server OR if its already initialized, just return */
    public abstract void initialize() throws Exception;

    /* Lists the names of all saves on the server */
    public abstract String[] getSaveNames();

    /* Gets the raw data of the ZIP file from a given name */
    public abstract byte[] getSaveData(String name);

    /* Uploads a zip file with the save file contents */
    public abstract void uploadSaveData(byte[] data);

    /* Gets the data.json file as a string */
    public abstract String getServerData();

    /* Upload new server data.json file */
    public abstract void setServerData();

    /* Verifies if the server is working properly */
    public abstract Boolean verifyServer();
}
