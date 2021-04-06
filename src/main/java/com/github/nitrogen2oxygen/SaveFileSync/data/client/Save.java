package com.github.nitrogen2oxygen.SaveFileSync.data.client;

import com.github.nitrogen2oxygen.SaveFileSync.utils.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Save {
    public File file;
    public String name;

    public Save(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public byte[] toZipFile() throws Exception {
        /* Test if the file exists */
        if ((file.isDirectory() && Files.list(file.toPath()).findFirst().isEmpty()) || !file.exists())
            return new byte[0];

        /* Create a temporary zip file */
        File tmpFile = Files.createTempFile("SaveFileSync", ".zip").toFile();
        org.apache.commons.io.FileUtils.forceDeleteOnExit(tmpFile); // It never likes deleting itself so we force it to

        /* Write to the file */
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tmpFile));
        if (!file.isDirectory()) {
            ZipEntry entry = new ZipEntry(file.getName());
            out.putNextEntry(entry);
            byte[] data = org.apache.commons.io.FileUtils.readFileToByteArray(file);
            out.write(data);
            out.closeEntry();
        } else {
            List<String> fileList = FileUtils.generateFileList(this.file);
            for (String saveFile : fileList) {
                String subDir = saveFile.substring(file.getPath().length() + 1);
                ZipEntry entry = new ZipEntry(name + File.separator + subDir);
                out.putNextEntry(entry);
                byte[] data = org.apache.commons.io.FileUtils.readFileToByteArray(new File(saveFile));
                out.write(data);
                out.closeEntry();
            }
        }
        out.close();

        /* Return the result */
        return Files.readAllBytes(tmpFile.toPath());
    }

    public void overwriteData(byte[] data) throws Exception {
        /* Backup our current data */
        byte[] backupData = toZipFile();
        if (!Arrays.equals(backupData, new byte[0])) {
            File backupFile = new File(file.getPath() + ".bak.zip");
            if (!backupFile.exists()) {
                boolean createFile = backupFile.createNewFile();
                if (!createFile) {
                    throw new Exception("Cannot create backup file!");
                }
            }
            FileOutputStream out = new FileOutputStream(backupFile);
            out.write(backupData);
            out.close();
        }

        /* Create temporary file to download the zip file from */
        File tmpFile = Files.createTempFile("SaveFileSync", ".zip").toFile();
        org.apache.commons.io.FileUtils.forceDeleteOnExit(tmpFile); // It never likes deleting itself so we force it to
        org.apache.commons.io.FileUtils.writeByteArrayToFile(tmpFile, data);

        /* Extract zip to original folder */
        ZipInputStream in = new ZipInputStream(new FileInputStream(tmpFile));
        ZipEntry entry = in.getNextEntry();
        while (entry != null) {
            String filePath = file.getPath() + File.separator + entry.getName().substring(name.length() + 1);
            File testFile = new File(filePath);
            if (!testFile.toPath().normalize().startsWith(file.getPath())) // Test if the file name is valid due to bug
                throw new Exception("Bad Zip Entry! Aborting!");
            if (entry.isDirectory()) {
                FileUtils.extractFile(in, filePath);
            } else {
                File dir = new File(filePath);
                dir.mkdir();
            }

            in.closeEntry();
            entry = in.getNextEntry();
        }

        in.close();
    }
}
