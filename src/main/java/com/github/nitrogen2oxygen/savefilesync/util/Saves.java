package com.github.nitrogen2oxygen.savefilesync.util;

import com.github.nitrogen2oxygen.savefilesync.save.Save;
import com.github.nitrogen2oxygen.savefilesync.save.SaveDirectory;
import com.github.nitrogen2oxygen.savefilesync.save.SaveFile;
import org.json.JSONObject;

import java.io.File;

public class Saves {
    public static Save buildFromJSON(JSONObject data) {
        Save save;
        String name = data.getString("name");
        String location = data.getString("location");
        switch (data.getString("type")) {
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
}
