package com.github.nitrogen2oxygen.savefilesync.server;

import com.github.nitrogen2oxygen.savefilesync.utils.Constants;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class GoogleDriveServer extends Server {
    private static final long serialVersionUID = 9137343934563456805L;
    private String verifier;
    private String apiKey;
    private String bearerKey;
    private String refreshKey;
    private Date expires;

    public static final String[] scopes = {
            "https://www.googleapis.com/auth/drive.file",
            "https://www.googleapis.com/auth/drive.metadata"
    };

    public GoogleDriveServer() {
        super();
    }

    @Override
    public String serverDisplayName() {
        return "Google Drive";
    }

    @Override
    public String getHostName() {
        return "drive.google.com";
    }

    @Override
    public void setData(HashMap<String, String> args) {
        if (apiKey == null || !apiKey.equals(args.get("apiKey"))) {
            apiKey = args.get("apiKey");
            verifier = args.get("verifier");
            bearerKey = null;
            refreshKey = null;
            expires = null;
        }
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
        return new ArrayList<>();
    }

    @Override
    public byte[] getSaveData(String name) {
        return new byte[0];
    }

    @Override
    public void uploadSaveData(String name, byte[] data) {

    }

    @Override
    public Boolean verifyServer() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://www.googleapis.com/drive/v2/files").openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + getBearerKey());

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
            HttpURLConnection connection = (HttpURLConnection) new URL("https://oauth2.googleapis.com/token").openConnection();
            connection.setRequestMethod("POST");
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");

            /* If there are no keys, create them. Otherwise, regenerate since the key has expired */
            StringBuilder postDataBuilder = new StringBuilder();
            postDataBuilder.append("client_id=").append(Constants.GOOGLE_DRIVE_APP_ID);
            postDataBuilder.append("&client_secret=").append(Constants.GOOGLE_DRIVE_SECRET);
            postDataBuilder.append("&redirect_uri=").append(Constants.GOOGLE_DRIVE_REDIRECT_URI);
            postDataBuilder.append("&access_type=offline");
            if (refreshing) {
                postDataBuilder.append("&grant_type=refresh_token");
                postDataBuilder.append("&refresh_token=").append(refreshKey);
            } else {
                postDataBuilder.append("&grant_type=authorization_code");
                postDataBuilder.append("&code=").append(apiKey);
                postDataBuilder.append("&code_verifier=").append(verifier);
            }
            connection.setRequestProperty("Content-Length", String.valueOf(postDataBuilder.toString().length()));
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
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static String generateVerifier() {
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[64];
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

    public static String getKey() throws IOException {
        ServerSocket server = new ServerSocket(1235);
        try (Socket client = server.accept()) {
            InputStreamReader isr = new InputStreamReader(client.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            String line = reader.readLine();
            String query = line.split(" ")[1].substring(2);
            String[] pairs = query.split("&");
            HashMap<String, String> queries = new HashMap<>();
            for (String pair : pairs) {
                queries.put(pair.split("=")[0], pair.split("=")[1]);
            }
            DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
            outputStream.writeUTF("You can close this window and return to the app!");
            outputStream.flush();
            client.close();
            return queries.get("code");
        }
    }
}