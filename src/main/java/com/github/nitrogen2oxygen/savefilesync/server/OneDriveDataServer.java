package com.github.nitrogen2oxygen.savefilesync.server;

import com.github.nitrogen2oxygen.savefilesync.io.CallbackServer;
import com.github.nitrogen2oxygen.savefilesync.util.Constants;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OneDriveDataServer extends DataServer {
    private static final long serialVersionUID = -8175793932377923413L;
    private static final String rootURL = "https://graph.microsoft.com/v1.0/me/";

    public static final Collection<String> SCOPES = Arrays.asList(
            "offline_access",
            "files.readwrite");

    private String apiKey;
    private String bearerKey;
    private String refreshKey;
    private Date expires;

    @Override
    public ServerType getServerType() {
        return ServerType.ONEDRIVE;
    }

    @Override
    public String getHostName() {
        return "microsoft.com";
    }

    @Override
    public void setData(HashMap<String, String> args) {
        if (apiKey == null || !apiKey.equals(args.get("apiKey"))) {
            apiKey = args.get("apiKey");
        }
    }

    @Override
    public HashMap<String, String> getData() {
        HashMap<String, String> data = new HashMap<>();
        data.put("apiKey", apiKey);
        return data;
    }

    @Override
    public List<String> getSaveNames() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(rootURL + "drive/special/approot/children").openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + getBearerKey());
            connection.setDoInput(true);
            connection.setRequestProperty("Accept", "application/json");

            /* Handle response code */
            InputStream inputStream = connection.getInputStream();
            JSONObject response = new JSONObject(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
            JSONArray files = response.getJSONArray("value");
            List<String> names = new ArrayList<>();
            for (int i = 0; i < files.length(); i++) {
                JSONObject file = files.getJSONObject(i);
                String fileName = file.getString("name");
                if (!fileName.endsWith(".zip")) continue;
                names.add(fileName.substring(0, fileName.length() - 4));
            }
            return names;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] getSaveData(String name) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(rootURL + "drive/special/approot:/" + name.replace(" ", "%20") + ".zip" + ":/content").openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + getBearerKey());
            connection.setDoInput(true);
            connection.setRequestProperty("Accept", "application/octet-stream");

            InputStream response = connection.getInputStream();
            return IOUtils.toByteArray(response);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void uploadSaveData(String name, byte[] data) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(rootURL + "drive/special/approot:/" + name.replace(" ", "%20") + ".zip" + ":/content").openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Authorization", "Bearer " + getBearerKey());
        connection.setDoInput(true);
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(data);
        outputStream.close();

        connection.getInputStream();
    }

    @Override
    public boolean verifyServer() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(rootURL + "drive/special/approot").openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + getBearerKey());
            connection.setDoInput(true);
            connection.setRequestProperty("Accept", "application/json");

            /* Handle response code */
            int code = connection.getResponseCode();
            return code < 300;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getSaveHash(String name) {
        return null;
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
            HttpURLConnection connection = (HttpURLConnection) new URL("https://login.microsoftonline.com/common/oauth2/v2.0/token").openConnection();
            connection.setRequestMethod("POST");
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");

            /* If there are no keys, create them. Otherwise, regenerate since the key has expired */
            StringBuilder postDataBuilder = new StringBuilder();
            postDataBuilder.append("client_id=").append(Constants.ONEDRIVE_APP_ID);
            if (refreshing) {
                postDataBuilder.append("&grant_type=refresh_token");
                postDataBuilder.append("&refresh_token=").append(refreshKey);
            } else {
                postDataBuilder.append("&grant_type=authorization_code");
                postDataBuilder.append("&code=").append(apiKey);
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
                expires = new Date(System.currentTimeMillis() + (object.getInt("expires_in") * 1000L));
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

    public static String getApiKey() {

        try (CallbackServer callbackServer = new CallbackServer(Constants.CALLBACK_PORT)) {
            String key = callbackServer.getQuery("code");
            if (key == null || key.length() == 0) {
                callbackServer.send("No code specified in query. Please close this window and try again...");
            } else {
                callbackServer.send("Code received! You can close this window and return to the app!");
            }
            return key;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
