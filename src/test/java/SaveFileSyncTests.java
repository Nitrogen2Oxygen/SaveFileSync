import com.github.nitrogen2oxygen.savefilesync.client.ClientData;
import com.github.nitrogen2oxygen.savefilesync.client.Save;
import com.github.nitrogen2oxygen.savefilesync.server.DataServer;
import com.github.nitrogen2oxygen.savefilesync.server.ServerType;
import com.github.nitrogen2oxygen.savefilesync.util.DataManager;
import com.github.nitrogen2oxygen.savefilesync.util.DataServers;
import com.github.nitrogen2oxygen.savefilesync.util.FileLocations;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SaveFileSyncTests {
    public static String testDataDirectory() {
       return Paths.get(System.getProperty("user.home"), ".savefileSync-test").toString();
    }

    @Test
    public void dataManagement() throws Exception {
        // Initialize the data
        DataManager.init(testDataDirectory());
        ClientData data = DataManager.load(testDataDirectory());

        // Check if the data folder exists
        File dataDirectory = new File(testDataDirectory());
        assert dataDirectory.exists() && dataDirectory.isDirectory();

        // Make a test save
        File tmpFile = Files.createTempFile("SaveFileSyncTest", ".tmp").toFile();
        tmpFile.deleteOnExit();
        Save save = new Save("TestSave", tmpFile);
        data.addSave(save);
        assert data.getSave("TestSave") == save;
        data.removeSave("TestSave");
        assert data.getSave("TestSave") == null;

        // Does serialization work correct
        DataManager.save(data, testDataDirectory());
        File serverFile = new File(FileLocations.getServerFile(testDataDirectory()));
        assert serverFile.exists();
        File settingsFile = new File(FileLocations.getConfigFile(testDataDirectory()));
        assert settingsFile.exists();
        File savesDirectory = new File(FileLocations.getSaveDirectory(testDataDirectory()));
        assert savesDirectory.exists();
    }

    @Test
    public void saveFileManagement() throws Exception {
        // Create a few test save files
        File testFile1 = Files.createTempFile("SaveFileSyncTest1", ".tmp").toFile();
        File testFile2 = Files.createTempDirectory("SaveFileSyncTest2").toFile();
        File testFile3 = Files.createTempFile("SaveFileSyncTest3", ".tmp").toFile();
        testFile1.deleteOnExit();
        testFile2.deleteOnExit();
        testFile3.deleteOnExit();

        // Write data to files
        FileUtils.write(testFile1, "Testing testing 123!", StandardCharsets.UTF_8);
        FileUtils.write(testFile3, "Testing testing 456!!!", StandardCharsets.UTF_8);

        // Create save instances
        Save save1 = new Save("Test 1", testFile1);
        Save save2 = new Save("Test 2", testFile2);

        // Load files to data
        DataManager.init(testDataDirectory()); // Just in case
        ClientData data = DataManager.load(testDataDirectory());
        data.addSave(save1);
        data.addSave(save2);

        // Make sure data manager doesn't take bad saves
        Save badSave1 = new Save("Bad Test 1", testFile1);
        Save badSave2 = new Save("Test 1", testFile3);
        Save badSave3 = new Save("Bad Test 3", testFile1.getParentFile());
        try {
            data.addSave(badSave1);
            assert false;
        } catch (Exception e) {
            // The function should error out
        }
        try {
            data.addSave(badSave2);
            assert false;
        } catch (Exception e) {
            // The function should error out
        }
        try {
            data.addSave(badSave3);
            assert false;
        } catch (Exception e) {
            // The function should error out
        }

        // Test zip file creation
        File zipFile1 = Files.createTempFile("SaveFileSyncTest1", ".tmp.zip").toFile();
        File zipFile2 = Files.createTempFile("SaveFileSyncTest2", ".tmp.zip").toFile();
        zipFile1.deleteOnExit();
        zipFile2.deleteOnExit();
        FileUtils.writeByteArrayToFile(zipFile1, save1.toZipFile());
        FileUtils.writeByteArrayToFile(zipFile2, save2.toZipFile());
        ZipFile zip1 = new ZipFile(zipFile1);
        ZipFile zip2 = new ZipFile(zipFile1);

        // Test zip file extraction
        ZipEntry entry = zip1.entries().nextElement();
        assert entry.getName().equals(testFile1.getName());
    }

    @Test
    public void serverManagement() {
        /* We cannot test actual server integrations from unit tests. Those must be done manually */
        for (ServerType type : ServerType.values()) {
            DataServer testServer = DataServers.buildServer(type);
            if (type == ServerType.NONE) {
                assert testServer == null;
            } else {
                assert testServer != null;
            }
        }
    }
}
