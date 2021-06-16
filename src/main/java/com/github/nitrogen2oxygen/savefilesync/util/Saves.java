package com.github.nitrogen2oxygen.savefilesync.util;

import com.github.nitrogen2oxygen.savefilesync.client.save.Save;
import com.github.nitrogen2oxygen.savefilesync.client.save.SaveDirectory;
import com.github.nitrogen2oxygen.savefilesync.client.save.SaveFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Saves {
    public static Save buildFromJSON(JSONObject data) throws Exception {
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
                // Check if its the correct type and throw an error if its not
                File directory = new File(location);
                if (!directory.isDirectory()) throw new Exception("File types do not match!");
                SaveDirectory saveDir = new SaveDirectory(name, new File(location));
                JSONArray exclusions = data.getJSONArray("exclusions");
                List<String> exclusionList = new ArrayList<>();
                for(int i = 0; i < exclusions.length(); i++){
                    exclusionList.add(exclusions.getString(i));
                }
                saveDir.setExclusions(exclusionList.toArray(new String[0]));
                save = saveDir;
                break;
            case "file":
                // Check if its the correct type and throw an error if its not
                File file = new File(location);
                if (!file.isFile()) throw new Exception("File types do not match!");
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
