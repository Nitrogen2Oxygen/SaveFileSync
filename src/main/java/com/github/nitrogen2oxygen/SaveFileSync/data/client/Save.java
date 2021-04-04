package com.github.nitrogen2oxygen.SaveFileSync.data.client;

import com.github.nitrogen2oxygen.SaveFileSync.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/* TODO: Handle the many errors in this file!! */
public class Save {
    public File file;
    public Boolean isDir;
    public String name;

    public Save(String name, File file) {
        this.name = name;
        this.file = file;
        this.isDir = file.isDirectory();
    }

    public byte[] toRawFile() throws Exception {
        if (isDir == null) this.isDir = file.isDirectory();
        if (!isDir) {
            if (!file.exists()) return new byte[0];
           return org.apache.commons.io.FileUtils.readFileToByteArray(file);
        }
        // Write to a temporary directory
        File tmpdir = Files.createTempDirectory("SaveFileSync").toFile();
        tmpdir.deleteOnExit();
        File tmpFile = new File(tmpdir, name + ".zip");
        byte[] buffer = new byte[1024];
        FileOutputStream fos = new FileOutputStream(tmpFile);
        ZipOutputStream zos = new ZipOutputStream(fos);

        // Get file list
        List<String> fileList = FileUtils.generateFileList(file);
        for (String file : fileList) {
            String subDir = file.substring(this.file.getPath().length() + 1);
            ZipEntry ze = new ZipEntry(name + File.separator + subDir);
            zos.putNextEntry(ze);
            FileInputStream in = new FileInputStream(file);
            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            in.close();
            zos.closeEntry();
        }
        zos.close();

        // Delete the temp directory and return the result
        byte[] res = Files.readAllBytes(tmpFile.toPath());
        org.apache.commons.io.FileUtils.deleteDirectory(tmpdir);
        return res;
    }

    private void overwriteFile(byte[] data) throws Exception {
        /* Backup data we have just in case */
        byte[] backupData = toRawFile();
        if (!Arrays.equals(backupData, new byte[0])) {
            File backupFile = new File(file.getPath() + ".bak");
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

        /* Overwrite file! No need to do any weird zip stuff or anything */
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(data);
        stream.close();
    }

    private void overwriteDirectory(byte[] data) throws Exception { // TODO: Possible bug - won't delete temp dir if there's an error
        /* Backup data we have just in case */
        byte[] backupData = toRawFile();
        if (!Arrays.equals(backupData, new byte[0])) {
            File backupFile = new File(file.getPath() + ".zip.bak");
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

        /* Create temporary directory to store the zip file */
        File tmpDir = Files.createTempDirectory("SaveFileSync").toFile();
        tmpDir.deleteOnExit(); // This never works but hey, its here
        File tmpFile = new File(tmpDir, name + ".zip");
        FileOutputStream fos = new FileOutputStream(tmpFile);
        fos.write(data); // Having a byte[] as our raw data makes writing to files significantly easier
        fos.close();

        /* Extract zip to original folder */
        FileInputStream fin = new FileInputStream(tmpFile); // We don't need to touch this due to zin managing it for us
        ZipInputStream zin = new ZipInputStream(fin);
        ZipEntry entry = zin.getNextEntry();
        while (entry != null) {
            String filePath = file.getPath() + File.separator + entry.getName().substring(name.length() + 1);
            File testFile = new File(filePath);
            if (!testFile.toPath().normalize().startsWith(file.getPath())) // Test if the file name is valid due to bug
                throw new Exception("Bad Zip Entry! Aborting!");
            if (!entry.isDirectory()) {
                FileUtils.extractFile(zin, filePath);
            } else {
                File dir = new File(filePath);
                dir.mkdir();
            }
            zin.closeEntry();
            entry = zin.getNextEntry();
        }
        zin.close();

        // Cleanup
        org.apache.commons.io.FileUtils.deleteDirectory(tmpDir);
    }


    public void overwriteData(byte[] data) throws Exception {
        if (isDir == null) this.isDir = file.isDirectory();
        if (isDir) {
            overwriteDirectory(data);
        } else {
            overwriteFile(data);
        }
    }
}
