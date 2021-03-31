package com.github.nitrogen2oxygen.SaveFileSync.data.server;

public class GoogleDriveServer extends Server {
    public GoogleDriveServer() {
        super();
    }

    @Override
    public String serverTypeName() {
        return "google_drive";
    }

    @Override
    public String serverDisplayName() {
        return "Google Drive";
    }
}
