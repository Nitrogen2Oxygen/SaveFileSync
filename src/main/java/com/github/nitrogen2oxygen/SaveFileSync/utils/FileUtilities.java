package com.github.nitrogen2oxygen.SaveFileSync.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtilities {
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
}
