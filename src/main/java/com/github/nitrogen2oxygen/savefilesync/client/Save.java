package com.github.nitrogen2oxygen.savefilesync.client;

import com.github.nitrogen2oxygen.savefilesync.util.FileUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Save implements java.io.Serializable {
    private static final long serialVersionUID = -3053800939549922372L;
    private final File file;
    private final String name;

    public Save(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public byte[] toZipFile() throws Exception {
        /* Test if the file exists */
        if ((file.isDirectory() && !Files.list(file.toPath()).findFirst().isPresent()) || !file.exists())
            return new byte[0];

        /* Create a temporary zip file */
        File tmpFile = Files.createTempFile("SaveFileSync", ".zip").toFile();
        tmpFile.deleteOnExit();

        /* Write to the file */
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tmpFile));
        if (!file.isDirectory()) {
            ZipEntry entry = new ZipEntry(file.getName());
            out.putNextEntry(entry);
            byte[] data = FileUtils.readFileToByteArray(file);
            out.write(data);
            out.closeEntry();
        } else {
            List<String> fileList = FileUtilities.generateFileList(this.file);
            for (String saveFile : fileList) {
                String subDir = saveFile.substring(file.getPath().length() + 1);
                ZipEntry entry = new ZipEntry(name + File.separator + subDir);
                out.putNextEntry(entry);
                byte[] data = FileUtils.readFileToByteArray(new File(saveFile));
                out.write(data);
                out.closeEntry();
            }
        }

        /* Cleanup */
        out.close();
        byte[] data = Files.readAllBytes(tmpFile.toPath());
        tmpFile.delete();

        /* Return the result */
        return data;
    }

    public void overwriteData(byte[] data, boolean makeBackup, boolean forceOverwrite) throws Exception {
        /* Backup our current data if the user wants to */
        if (makeBackup) FileUtilities.makeBackup(this);

        /* Create temporary file to download the zip file from */
        File tmpFile = Files.createTempFile("SaveFileSync", ".zip").toFile();
        tmpFile.deleteOnExit();
        FileUtils.writeByteArrayToFile(tmpFile, data);

        /* Delete data if force overwrite is enabled */
        if (forceOverwrite && file.isDirectory()) FileUtils.cleanDirectory(file);

        /* Extract zip to original folder */
        ZipInputStream in = new ZipInputStream(new FileInputStream(tmpFile));
        ZipEntry entry = in.getNextEntry();
        if (file.isFile()) {
            /* Is just a file, just overwrite the data */
            String filePath = file.getPath();
            File saveFile = new File(filePath);
            if (!saveFile.toPath().normalize().startsWith(file.getPath())) // Test if the file name is valid due to bug
                throw new Exception("Bad Zip Entry! Aborting!");
            IOUtils.copy(in, new FileOutputStream(saveFile));
        } else {
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
        }

        /* Cleanup */
        in.close();
        tmpFile.delete();
    }
}
