package com.github.nitrogen2oxygen.SaveFileSync.data.server;

public abstract class Server {
    public Server() {

    }

    // The server type
    public String serverTypeName() {
        return null;
    }

    // The display name for the server type
    public String serverDisplayName() {
        return null;
    }
}
