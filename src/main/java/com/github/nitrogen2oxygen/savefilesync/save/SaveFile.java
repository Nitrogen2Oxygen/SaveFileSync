package com.github.nitrogen2oxygen.savefilesync.save;

import com.github.nitrogen2oxygen.savefilesync.util.FileUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SaveFile extends Save {
    public SaveFile(String name, File file) {
        super(name, file);
    }

    @Override
    public byte[] toZipData() throws Exception {
        File file = getFile();
        if (!file.exists())
            return new byte[0];

        /* Create a temporary zip file */
        File tmpFile = Files.createTempFile("SaveFileSync", ".zip").toFile();
        tmpFile.deleteOnExit();

        /* Write to the file */
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tmpFile));
        ZipEntry entry = new ZipEntry(file.getName());
        out.putNextEntry(entry);
        byte[] data = FileUtils.readFileToByteArray(file);
        out.write(data);
        out.closeEntry();

        /* Cleanup */
        out.close();
        byte[] saveData = Files.readAllBytes(tmpFile.toPath());
        tmpFile.delete();

        /* Return the result */
        return saveData;
    }

    @Override
    public void writeData(byte[] data, boolean makeBackup, boolean forceOverwrite) throws Exception {
        File file = getFile();

        /* Backup our current data if the user wants to */
        if (makeBackup) FileUtilities.makeBackup(this);

        /* Create temporary file to download the zip file from */
        File tmpFile = Files.createTempFile("SaveFileSync", ".zip").toFile();
        tmpFile.deleteOnExit();
        FileUtils.writeByteArrayToFile(tmpFile, data);

        /* Extract zip to original folder */
        ZipInputStream in = new ZipInputStream(new FileInputStream(tmpFile));
        ZipEntry entry = in.getNextEntry();

        /* Is just a file, just overwrite the data */
        String filePath = file.getPath();
        File saveFile = new File(filePath);
        if (!saveFile.toPath().normalize().startsWith(file.getPath())) // Test if the file name is valid due to bug
            throw new Exception("Bad Zip Entry! Aborting!");
        IOUtils.copy(in, new FileOutputStream(saveFile));

        /* Cleanup */
        in.close();
        tmpFile.delete();
    }

    @Override
    public String toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "file");
        json.put("name", getName());
        json.put("location", getFile().getPath());
        return json.toString();
    }
}
