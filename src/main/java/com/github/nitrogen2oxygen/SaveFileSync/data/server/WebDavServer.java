package com.github.nitrogen2oxygen.SaveFileSync.data.server;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
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
            URI uri = getSaveURI(name + ".zip");
            InputStream stream = sardine().get(uri.toString());
            return IOUtils.toByteArray(stream);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void uploadSaveData(String name, byte[] data) throws Exception {
        URI uri = getSaveURI(name + ".zip");
        sardine().put(uri.toString(), data);
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

    private URI getSaveURI(String fileName) throws URISyntaxException {
        URI base = new URI(this.uri);
        String path = base.getPath() + "/" + URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        return base.resolve(path);
    }
}
