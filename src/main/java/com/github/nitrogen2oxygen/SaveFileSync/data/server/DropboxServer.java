package com.github.nitrogen2oxygen.SaveFileSync.data.server;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

public class DropboxServer extends Server {
    private static final long serialVersionUID = 8175793937439456805L;
    private String bearerKey;
    private String refreshKey;
    private Date expires;
    private String verifier;
    private String apiKey;

    @Override
    public String serverDisplayName() {
        return "Dropbox";
    }

    @Override
    public String getHostName() {
        return "dropbox.com";
    }

    @Override
    public void setData(HashMap<String, String> args) {
        apiKey = args.get("apiKey");
        bearerKey = null;
        refreshKey = null;
        expires = null;
        verifier = args.get("verifier");
    }

    @Override
    public HashMap<String, String> getData() {
        HashMap<String, String> data = new HashMap<>();
        data.put("apiKey", apiKey);
        data.put("verifier", verifier);
        return data;
    }

    @Override
    public ArrayList<String> getSaveNames() {
        return null;
    }

    @Override
    public byte[] getSaveData(String name) {
        String path = "/" + name + ".zip";
        try {
            JSONObject json = new JSONObject();
            json.put("path", path);
            HttpURLConnection connection = (HttpURLConnection) new URL("https://content.dropboxapi.com/2/files/download").openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + getBearerKey());
            connection.setRequestProperty("Dropbox-API-Arg", json.toString());

            InputStream response = connection.getInputStream();
            return IOUtils.toByteArray(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void uploadSaveData(String name, byte[] data) throws Exception {

    }

    @Override
    public Boolean verifyServer() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.dropboxapi.com/2/check/user").openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + getBearerKey());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write("{\"query\": \"verify\"}".getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            /* Handle response code */
            int code = connection.getResponseCode();
            return code < 300;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getBearerKey() {
        try {
            if (bearerKey == null || refreshKey == null) {
                HttpURLConnection connection = (HttpURLConnection) new URL("https://api.dropboxapi.com/oauth2/token").openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setInstanceFollowRedirects(false);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");
                byte[] postData = ("code=" + apiKey + "&grant_type=authorization_code&client_id=i136jjbqxg4aaci&code_verifier=" + verifier).getBytes(StandardCharsets.UTF_8);
                connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
                connection.setUseCaches(false);
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(postData);
                outputStream.close();

                InputStream stream = connection.getInputStream();
                String json = IOUtils.toString(stream, StandardCharsets.UTF_8);
                JSONObject object = new JSONObject(json);
                String token = object.getString("access_token");
                String refresh = object.getString("refresh_token");
                String expiresTime = object.getString("expires_in");
                expires = new Date(System.currentTimeMillis() + Integer.parseInt(expiresTime));
                refreshKey = refresh;
                bearerKey = token;
                return token;
            } else if (expires.before(new Date())) {
                /* Regenerate the bearer token when it expires */
                HttpURLConnection connection = (HttpURLConnection) new URL("https://api.dropboxapi.com/oauth2/token").openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setInstanceFollowRedirects(false);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");
                byte[] postData = ("grant_type=refresh_token&client_id=i136jjbqxg4aaci&refresh_token=" + refreshKey).getBytes(StandardCharsets.UTF_8);
                connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
                connection.setUseCaches(false);
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(postData);
                outputStream.close();

                InputStream stream = connection.getInputStream();
                String json = IOUtils.toString(stream, StandardCharsets.UTF_8);
                JSONObject object = new JSONObject(json);
                String token = object.getString("access_token");
                String expiresTime = object.getString("expires_in");
                expires = new Date(System.currentTimeMillis() + Integer.parseInt(expiresTime));
                bearerKey = token;
                return token;
            } else {
                /* Return the current bearer token if its not expired */
                return bearerKey;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getVerifier() {
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[32];
        sr.nextBytes(code);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(code);
    }

    public static String getChallenge(String verifier) throws NoSuchAlgorithmException {
        byte[] bytes = verifier.getBytes(StandardCharsets.US_ASCII);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(bytes, 0, bytes.length);
        byte[] digest = md.digest();
        return org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(digest);
    }
}
