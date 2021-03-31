package com.github.nitrogen2oxygen.SaveFileSync.data.server;

public class ServerManager {

    public static Server ServerFactory(String name) {
        switch (name) {
            case "WebDav":
                return new WebDavServer();
            case "Google Drive":
                return new GoogleDriveServer();
            default:
                return null;
        }
    }

    public static Class<? extends Server> getServerClass(String name) {
        switch (name) {
            case "WebDav":
                return WebDavServer.class;
            case "Google Drive":
                return GoogleDriveServer.class;
            default:
                return null;
        }
    }
}
