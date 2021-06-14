package com.github.nitrogen2oxygen.savefilesync.save;

import com.github.nitrogen2oxygen.savefilesync.util.FileUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SaveDirectory extends Save {
    public SaveDirectory(String name, File file) {
        super(name, file);
    }

    @Override
    public byte[] toZipData() throws Exception {
        File file = getFile();
        String name = getName();
        if (!file.exists() || FileUtilities.generateFileList(file).isEmpty())
            return new byte[0];

        /* Create a temporary zip file */
        File tmpFile = Files.createTempFile("SaveFileSync", ".zip").toFile();
        tmpFile.deleteOnExit();

        /* Write to the file */
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tmpFile));
        List<String> fileList = FileUtilities.generateFileList(file);
        for (String saveFile : fileList) {
            String subDir = saveFile.substring(file.getPath().length() + 1);
            ZipEntry entry = new ZipEntry(name + File.separator + subDir);
            out.putNextEntry(entry);
            byte[] data = FileUtils.readFileToByteArray(new File(saveFile));
            out.write(data);
            out.closeEntry();
        }

        /* Cleanup */
        out.close();
        byte[] data = Files.readAllBytes(tmpFile.toPath());
        tmpFile.delete();

        /* Return the result */
        return data;
    }

    @Override
    public void writeData(byte[] data, boolean makeBackup, boolean forceOverwrite) throws Exception {
        File file = getFile();
        String name = getName();

        /* Backup our current data if the user wants to */
        if (makeBackup) FileUtilities.makeBackup(this);

        /* Create temporary file to download the zip file from */
        File tmpFile = Files.createTempFile("SaveFileSync", ".zip").toFile();
        tmpFile.deleteOnExit();
        FileUtils.writeByteArrayToFile(tmpFile, data);

        /* Delete data if force overwrite is enabled */
        if (forceOverwrite) FileUtils.cleanDirectory(file);

        /* Extract zip to original folder */
        ZipInputStream in = new ZipInputStream(new FileInputStream(tmpFile));
        ZipEntry entry = in.getNextEntry();

        /* Is a folder */
        while (entry != null) {
            String filePath = file.getPath() + File.separator + entry.getName().substring(name.length() + 1);
            File saveFile = new File(filePath);
            if (!saveFile.toPath().normalize().startsWith(file.getPath())) // Test if the file name is valid due to bug
                throw new Exception("Bad Zip Entry! Aborting!");
            if (!entry.isDirectory()) {
                saveFile.getParentFile().mkdirs();
                saveFile.createNewFile();
                IOUtils.copy(in, new FileOutputStream(saveFile));
            } else {
                File dir = new File(filePath);
                dir.mkdirs();
            }

            in.closeEntry();
            entry = in.getNextEntry();
        }

        /* Cleanup */
        in.close();
        tmpFile.delete();
    }

    @Override
    public String toJSON() {
        JSONObject json = new JSONObject();
        json.put("type", "directory");
        json.put("name", getName());
        json.put("location", getFile().getPath());
        return json.toString();
    }
}
