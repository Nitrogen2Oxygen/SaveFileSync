package com.github.nitrogen2oxygen.SaveFileSync.data.server;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

/* TODO: Prevent boot time from crippling due to invalid connection */
public class WebDavServer extends Server {

    private static final long serialVersionUID = -2218215115068183298L;
    private String username;
    private String password;
    private String uri;

    public WebDavServer() {
        super();
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
    public ArrayList<String> getSaveNames()  {
        return new ArrayList<>();
    }

    @Override
    public byte[] getSaveData(String name) {
        try {
            HttpURLConnection connection = (HttpURLConnection) getSaveURL(name + ".zip").openConnection();
            connection.setRequestMethod("GET");
            /* Get authentication */
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeaderValue = "Basic " + new String(encodedAuth);
            connection.setRequestProperty("Authorization", authHeaderValue);
            InputStream stream = connection.getInputStream();
            return IOUtils.toByteArray(stream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void uploadSaveData(String name, byte[] data) throws Exception {
            HttpURLConnection connection = (HttpURLConnection) getSaveURL(name + ".zip").openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            /* Get authentication */
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeaderValue = "Basic " + new String(encodedAuth);
            connection.setRequestProperty("Authorization", authHeaderValue);
            /* Handle request */
            OutputStream stream = connection.getOutputStream();
            stream.write(data);
            stream.close();
            connection.getInputStream();
    }

    @Override
    public Boolean verifyServer() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(uri).openConnection();
            connection.setRequestMethod("HEAD");
            /* Get authentication */
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeaderValue = "Basic " + new String(encodedAuth);
            connection.setRequestProperty("Authorization", authHeaderValue);
            int code = connection.getResponseCode();
            return code < 300;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private URL getSaveURL(String fileName) throws IOException, URISyntaxException {
        URI base = new URL(this.uri).toURI();
        String path = base.getPath() + "/" + URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        return base.resolve(path).toURL();
    }
}
