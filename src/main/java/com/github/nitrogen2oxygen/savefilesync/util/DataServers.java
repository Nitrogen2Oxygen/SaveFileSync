package com.github.nitrogen2oxygen.savefilesync.util;

import com.github.nitrogen2oxygen.savefilesync.server.*;
import org.jetbrains.annotations.NotNull;

public class DataServers {
    /* Create a server object */
    public static DataServer buildServer(@NotNull ServerType type) {
        switch (type) {
            case WEBDAV:
                return new WebDavDataServer();
            case DROPBOX:
                return new DropboxDataServer();
            case ONEDRIVE:
                return new OneDriveDataServer();
        }
        return null;
    }

    /* Get server display names */
    public static String getDisplayName(@NotNull ServerType type) {
        switch (type) {
            case WEBDAV:
                return "WebDAV";
            case DROPBOX:
                return "Dropbox";
            case ONEDRIVE:
                return "OneDrive";
            case NONE:
                return "None";
        }
        return null;
    }
}
