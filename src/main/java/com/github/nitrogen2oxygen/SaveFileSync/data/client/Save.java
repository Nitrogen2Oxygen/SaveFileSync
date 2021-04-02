package com.github.nitrogen2oxygen.SaveFileSync.data.client;

import com.github.nitrogen2oxygen.SaveFileSync.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
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

    public byte[] toZipFile() throws Exception { // TODO: Possible bug - won't delete temp dir if there's an error
        // Write to a temporary directory
        File tmpdir = Files.createTempDirectory("SaveFileSync").toFile();
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

    public void overwriteData(byte[] data) throws Exception { // TODO: Possible bug - won't delete temp dir if there's an error
        // Backup data first
        byte[] zipData = toZipFile();
        File backupFile = new File(file.getPath() + ".zip.bak");
        if (!backupFile.exists()) {
            boolean createFile = backupFile.createNewFile();
            if (!createFile) {
                throw new Exception("Cannot create backup file!");
            }
        }
        FileOutputStream out = new FileOutputStream(backupFile);
        out.write(zipData);
        out.close();

        // Download zip file from remote server
        File tmpDir = Files.createTempDirectory("SaveFileSync").toFile();
        File tmpFile = new File(tmpDir, name + ".zip");
        FileOutputStream fos = new FileOutputStream(tmpFile);
        fos.write(data);
        fos.close();

        // Extract zip to file or folder
        FileInputStream fin = new FileInputStream(tmpFile);
        ZipInputStream zin = new ZipInputStream(fin);
        ZipEntry entry = zin.getNextEntry();
        if (file.isDirectory()) {
            while (entry != null) {
                String filePath = file.getPath() + File.separator + entry.getName().substring(name.length() + 1);
                if (!entry.isDirectory()) {
                    FileUtils.extractFile(zin, filePath);
                } else {
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zin.closeEntry();
                entry = zin.getNextEntry();
            }
        } else {
            FileUtils.extractFile(zin, file.getPath());
            zin.closeEntry();
        }
        zin.close();

        // Cleanup
        org.apache.commons.io.FileUtils.deleteDirectory(tmpDir);
    }
}
