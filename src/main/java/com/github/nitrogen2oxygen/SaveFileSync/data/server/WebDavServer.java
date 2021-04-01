package com.github.nitrogen2oxygen.SaveFileSync.data.server;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class WebDavServer extends Server {

    private String username;
    private String password;
    private String uri;

    public WebDavServer() {
        super();
    }

    /* Private functions for the class to use */
    private Sardine sardine() {
        return (username != null) ? SardineFactory.begin(username, password) : SardineFactory.begin();
    }

    /* Abstract function overrides */
    @Override
    public String serverDisplayName() {
        return "WebDav";
    }

    @Override
    public void setData(HashMap<String, String> args) {
        uri = args.get("uri");
        username = args.get("username");
        password = args.get("password");
    }

    @Override
    public HashMap<String, String> getData() {
        HashMap<String, String> data = new HashMap<>();
        data.put("uri", uri);
        data.put("username", username);
        data.put("password", password);
        return data;
    }

    @Override
    public String[] getSaveNames() {
        return new String[0];
    }

    @Override
    public byte[] getSaveData(String name) {
        return new byte[0];
    }

    @Override
    public void uploadSaveData(byte[] data) {

    }

    @Override
    public String getServerData() {
        return null;
    }

    @Override
    public void setServerData() {

    }

    @Override
    public Boolean verifyServer() {
        return null;
    }
}
