package com.github.nitrogen2oxygen.SaveFileSync.data.client;

import com.github.nitrogen2oxygen.SaveFileSync.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
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
}
