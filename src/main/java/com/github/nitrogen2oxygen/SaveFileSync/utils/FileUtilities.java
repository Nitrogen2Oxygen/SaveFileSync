package com.github.nitrogen2oxygen.SaveFileSync.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

    public static boolean ZipCompare(ZipFile zip1, ZipFile zip2) {
        Set<String> set1 = new LinkedHashSet<>();

        for (Enumeration<? extends ZipEntry> e = zip1.entries(); e.hasMoreElements();) {
            set1.add(e.nextElement().getName());
        }

        Set<String> set2 = new LinkedHashSet<>();
        for (Enumeration<? extends ZipEntry> e = zip2.entries(); e.hasMoreElements();)
            set2.add(e.nextElement().getName());

        for (String name : set1) {
            if (!set2.contains(name)) {
                return false;
            }
            try {
                set2.remove(name);
                if (!IOUtils.contentEquals(zip1.getInputStream(zip1.getEntry(name)),
                        zip2.getInputStream(zip2.getEntry(name)))) {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }
}
