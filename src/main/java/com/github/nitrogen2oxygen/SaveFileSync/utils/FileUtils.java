package com.github.nitrogen2oxygen.SaveFileSync.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

public class FileUtils {
    public static List<String> generateFileList(File node) {
        List<String> fileList = new ArrayList<>();
        // add file only
        if (node.isFile()) {
            fileList.add(node.toString());
        }

        if (node.isDirectory()) {
            String[] subNode = node.list();
            assert subNode != null;
            for (String filename: subNode) {
                List<String> subList = generateFileList(new File(node, filename));
               fileList.addAll(subList);
            }
        }
        return fileList;
    }

    public static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[1024];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}
