package com.github.nitrogen2oxygen.savefilesync.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class CallbackServer implements AutoCloseable {
    private final ServerSocket socket;
    private final Socket client;
    private final HashMap<String, String> queries = new HashMap<>();

    public CallbackServer(int port) throws IOException {
        socket = new ServerSocket(port);
        client = socket.accept();
        InputStreamReader isr = new InputStreamReader(client.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        String line = reader.readLine();
        try {
            String query = line.split(" ")[1].substring(2);
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                queries.put(pair.split("=")[0], pair.split("=")[1]);
            }
        } catch (StringIndexOutOfBoundsException ignored) {}
    }

    public void send(String message) throws IOException {
        OutputStream outputStream = client.getOutputStream();
        outputStream.write(message.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }

    public String getQuery(String key) {
        return queries.get(key);
    }

    @Override
    public void close() {
        try {
            if (client != null) {
                client.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
