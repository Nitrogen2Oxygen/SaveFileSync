package com.github.nitrogen2oxygen.SaveFileSync.data.server;

import com.github.nitrogen2oxygen.SaveFileSync.data.server.Server;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ServerSerializer implements JsonSerializer<Server> {
    public JsonElement serialize(Server src, Type type, JsonSerializationContext ctx) {
        if (src == null) return null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", src.serverDisplayName());
        jsonObject.add("data", ctx.serialize(src));
        return jsonObject;
    }
}
