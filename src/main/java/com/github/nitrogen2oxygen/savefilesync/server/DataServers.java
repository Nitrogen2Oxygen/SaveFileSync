package com.github.nitrogen2oxygen.savefilesync.server;

public class DataServers {
    /* Create a server object */
    public static DataServer ServerFactory(ServerType type) {
        switch (type) {
            case WEBDAV:
                return new WebDavDataServer();
            case DROPBOX:
                return new DropboxDataServer();
            case ONEDRIVE:
                return new OneDriveDataServer();
            default:
                return null;
        }
    }

    /* Get server display names */
    public static String getDisplayName(ServerType type) {
        switch (type) {
            case WEBDAV:
                return "WebDAV";
            case DROPBOX:
                return "Dropbox";
            case ONEDRIVE:
                return "OneDrive";
            case NONE:
                return "None";
            default:
                return null;
        }
    }
}
