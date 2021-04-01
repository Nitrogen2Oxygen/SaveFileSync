package com.github.nitrogen2oxygen.SaveFileSync.data.client;

import com.github.nitrogen2oxygen.SaveFileSync.data.server.Server;
import com.github.nitrogen2oxygen.SaveFileSync.utils.ServerManager;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ServerDeserializer implements JsonDeserializer<Server> {

    @Override
    public Server deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get("name");
        String name = prim.getAsString();
        Class<?> klass = ServerManager.getServerClass(name);
        return ctx.deserialize(jsonObject.get("data"), klass);
    }
}
