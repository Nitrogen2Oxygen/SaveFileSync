package com.github.nitrogen2oxygen.SaveFileSync.server;

import com.github.nitrogen2oxygen.SaveFileSync.utils.Constants;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
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
        try {
            JSONObject json = new JSONObject();
            json.put("path", "");
            json.put("include_non_downloadable_files", false);
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.dropboxapi.com/2/files/list_folder").openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + getBearerKey());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(json.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            InputStream stream = connection.getInputStream();
            JSONObject response = new JSONObject(IOUtils.toString(stream, StandardCharsets.UTF_8));
            JSONArray entries = response.getJSONArray("entries");
            ArrayList<String> names = new ArrayList<>();
            for (int i = 0; i < entries.length(); i++) {
                JSONObject entry = entries.getJSONObject(i);
                String name = entry.getString("name");
                if (!name.endsWith(".zip")) continue;
                names.add(name.substring(0, name.length() - 4));
            }
            return names;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

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
        String path = "/" + name + ".zip";
        JSONObject json = new JSONObject();
        json.put("path", path);
        json.put("mode", "overwrite");
        json.put("autorename", false);
        json.put("mute", false);
        json.put("strict_conflict", false);

        HttpURLConnection connection = (HttpURLConnection) new URL("https://content.dropboxapi.com/2/files/upload").openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + getBearerKey());
        connection.setRequestProperty("Dropbox-API-Arg", json.toString());
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(data);
        outputStream.close();

        connection.getInputStream();
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
            boolean hasKeys = bearerKey != null && refreshKey != null;
            boolean keyExpired = expires == null || expires.before(new Date());
            boolean refreshing = hasKeys && keyExpired;
            if (hasKeys && !keyExpired) {
                return bearerKey;
            }

            /* If the keys don't exist OR the key has expired, request new credentials */
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.dropboxapi.com/oauth2/token").openConnection();
            connection.setRequestMethod("POST");
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");

            /* If there are no keys, create them. Otherwise, regenerate since the key has expired */
            StringBuilder postDataBuilder = new StringBuilder();
            postDataBuilder.append("client_id=").append(Constants.DROPBOX_APP_ID);
            if (refreshing) {
                postDataBuilder.append("&grant_type=refresh_token");
                postDataBuilder.append("&refresh_token=").append(refreshKey);
            } else {
                postDataBuilder.append("&grant_type=authorization_code");
                postDataBuilder.append("&code=").append(apiKey);
                postDataBuilder.append("&code_verifier=").append(verifier);
            }
            byte[] postData = postDataBuilder.toString().getBytes(StandardCharsets.UTF_8);

            /* Write post data to output stream */
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(postData);
            outputStream.close();

            /* Get response and handle it */
            InputStream stream = connection.getInputStream();
            String json = IOUtils.toString(stream, StandardCharsets.UTF_8);
            try {
                JSONObject object = new JSONObject(json);
                expires = new Date(System.currentTimeMillis() + Integer.parseInt(object.getString("expires_in")));
                if (!refreshing) refreshKey = object.getString("refresh_token");
                bearerKey = object.getString("access_token");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            return bearerKey;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
