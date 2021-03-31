package com.github.nitrogen2oxygen.SaveFileSync.data.server;

public class WebDavServer extends Server {
    public WebDavServer() {
        super();
    }

    @Override
    public String serverTypeName() {
        return "webdav";
    }

    @Override
    public String serverDisplayName() {
        return "Web Dav";
    }
}
