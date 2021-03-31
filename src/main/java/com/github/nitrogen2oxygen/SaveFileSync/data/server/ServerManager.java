package com.github.nitrogen2oxygen.SaveFileSync.data.server;

public class ServerManager {

    public static Server ServerFactory(String type) {
        switch (type) {
            case "WebDav":
                return new WebDavServer();
            case "Google Drive":
                return new GoogleDriveServer();
            default:
                return null;
        }
    }
}
