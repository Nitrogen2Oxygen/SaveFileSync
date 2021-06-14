package com.github.nitrogen2oxygen.savefilesync.util;

import com.github.nitrogen2oxygen.savefilesync.client.Settings;
import com.github.nitrogen2oxygen.savefilesync.save.Save;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
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

    public static boolean ZipCompare(@NotNull ZipFile zip1, @NotNull ZipFile zip2) {
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
            set2.remove(name);
            if (zip1.getEntry(name).getCrc() != zip2.getEntry(name).getCrc()) {
                return false;
            }
        }

        return true;
    }

    public static void makeBackup(Save save) throws Exception {
        byte[] backupData = save.toZipData();
        if (backupData == null || Arrays.equals(backupData, new byte[0])) return; // Check to see if data is empty empty

        File backupDirectory = new File(FileLocations.getBackupDirectory(FileLocations.getDataDirectory()));
        if (!backupDirectory.exists()) {
            boolean createDir = backupDirectory.mkdirs();
            if (!createDir) {
                throw new Exception("Cannot create backup directory!");
            }
        }
        File backupFile = new File(backupDirectory, save.getName() + ".zip");
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

    public static void restoreBackup(Save save, Settings settings) throws Exception {
        if (!hasBackup(save)) throw new Exception("Does not have backup. This function should not be called unless a backup has been verified");
        File backupDirectory = new File(FileLocations.getBackupDirectory(FileLocations.getDataDirectory()));
        File backupFile = new File(backupDirectory, save.getName() + ".zip");
        byte[] data = FileUtils.readFileToByteArray(backupFile);
        save.writeData(data, true, settings.shouldForceOverwrite()); // Data will be force backed up to allows restoration
    }

    public static boolean hasBackup(Save save) {
        File backupDirectory = new File(FileLocations.getBackupDirectory(FileLocations.getDataDirectory()));
        if (!backupDirectory.exists()) return false;
        File backupFile = new File(backupDirectory, save.getName() + ".zip");
        return backupFile.exists();
    }

    public static File getBackup(Save save) {
        File backupDirectory = new File(FileLocations.getBackupDirectory(FileLocations.getDataDirectory()));
        if (!backupDirectory.exists()) return null;
        File backupFile = new File(backupDirectory, save.getName() + ".zip");
        if (!backupFile.exists()) return null;
        return backupFile;
    }

    public static String toHash(File file) throws Exception {
        if (file.isDirectory()) throw new Exception("Cannot convert directory to hash");
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(FileUtils.readFileToByteArray(file));
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase(Locale.ROOT);
    }
}
