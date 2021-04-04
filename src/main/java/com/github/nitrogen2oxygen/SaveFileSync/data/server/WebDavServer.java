package com.github.nitrogen2oxygen.SaveFileSync.data.server;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public ArrayList<String> getSaveNames() {
        try {
            List<DavResource> resources = sardine().list(uri);
            ArrayList<String> names = new ArrayList<>();
            for (DavResource res : resources) {
                names.add(res.getName());
            }
            return names;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] getSaveData(String name) {
        try {
            URL baseURL = new URL(uri);
            String url = new URL(baseURL, baseURL.getPath() + "/" + name + ".zip").toString();
            InputStream stream = sardine().get(url);
            return IOUtils.toByteArray(stream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void uploadSaveData(String name, byte[] data) throws Exception {
        URL baseURL = new URL(uri);
        String url = new URL(baseURL, baseURL.getPath() + "/" + name + ".zip").toString();
        sardine().put(url, data);
    }

    @Override
    public Boolean verifyServer() {
        Sardine sardine = sardine();
        try {
            return sardine.exists(uri);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
