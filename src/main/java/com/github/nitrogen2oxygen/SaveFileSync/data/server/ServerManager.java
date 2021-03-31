package com.github.nitrogen2oxygen.SaveFileSync.data.server;

public class ServerManager {

    public static Server ServerFactory(String type) {
        switch (type) {
            case "webdav":
                return new WebDavServer();
            case "google_drive":
                return new GoogleDriveServer();
            default:
                return null;
        }
    }
}
