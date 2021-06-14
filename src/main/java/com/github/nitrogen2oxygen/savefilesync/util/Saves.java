package com.github.nitrogen2oxygen.savefilesync.util;

import com.github.nitrogen2oxygen.savefilesync.client.save.Save;
import com.github.nitrogen2oxygen.savefilesync.client.save.SaveDirectory;
import com.github.nitrogen2oxygen.savefilesync.client.save.SaveFile;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class Saves {
    public static Save buildFromJSON(JSONObject data) {
        Save save;
        String name = data.getString("name");
        String location = data.getString("location");
        String type;
        try {
            type = data.getString("type");
        } catch (JSONException e) {
            // Attempt to figure out the save type manually
            File saveFile = new File(location);
            type = saveFile.isDirectory() ? "directory" : "file";
        }
        switch (type) {
            case "directory":
                save = new SaveDirectory(name, new File(location));
                break;
            case "file":
                save = new SaveFile(name, new File(location));
                break;
            default:
                save = null;
        }
        
        return save;
    }

    public static Save build(boolean isDirectory, String name, File location) throws Exception {
        if (isDirectory) {
            if (!location.isDirectory()) throw new Exception("Save type does not match file type.");
            return new SaveDirectory(name, location);
        } else {
            if (location.isDirectory()) throw new Exception("Save type does not match file type.");
            return new SaveFile(name, location);
        }
    }

    public static boolean isDirectory(Save save) {
        return save.getClass().equals(SaveDirectory.class);
    }
}
