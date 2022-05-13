package org.chaiware.recursive_archive_extractor;

import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Goes over the source folder recursively and copies all files to the target preserving the original folder structure <br/>
 * In the case of an archive, it extracts its contents to the target<br/>
 * Archives within archives will also be extracted
 * */
public class RecursiveArchiveExtractor {
    private static final Logger log = LoggerFactory.getLogger(RecursiveArchiveExtractor.class);
    static String archiveSources = "";
    static String extractedTarget = "";

    static Map<String, Integer> extensionCount = new HashMap<>();

    public static void main(String[] args) throws Exception {
        usage(args);
        File sourceFolder = initializeSourceFolder();
        handleFolder(sourceFolder);
    }

    private static void usage(String[] args) {
        if (args.length < 2) {
            log.error("Please send the Source folder of the archives as the first argument and the target destination of the extracted files as the second argument");
            log.info("Usage: java -jar recursive-archive-extractor.jar $SOURCE_FOLDER $TARGET_FOLDER");
            System.exit(0);
        } else {
            archiveSources = args[0];
            extractedTarget = args[1];
        }
    }

    /**
     * Handles a folder <br/>
     * Creates the folder in the target if needed <br/>
     * Iterates over the folder and handles all internal files
     */
    private static void handleFolder(File file) throws IOException {
        log.info("In folder: {}, Files: {}", file.getPath(), file.listFiles().length);
        String destination = extractedTarget + file.getPath().replace(archiveSources, "");
        initializeFolder(destination);

        File[] filesList = file.listFiles();
        for (File currentFile : filesList) {
            if (currentFile.isFile()) {
                handleFile(currentFile, destination);
            } else { // Folder
                reportAndInitExtensionCount();
                handleFolder(currentFile);
            }
        }
    }

    /**
     * Handles a single file <br/>
     * If it is a regular file then copy it to the target <br/>
     * If it is an archived file then extract it to the target
     */
    private static void handleFile(File file, String destinationPath) throws IOException {
        String fileExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        extensionCount.put(fileExtension, extensionCount.getOrDefault(fileExtension, 0) + 1);

        try {
            Archiver archiver = ArchiverFactory.createArchiver(file); // Throws an exception when not an archive
            String targetFolder = destinationPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf("."));
            initializeFolder(targetFolder);
            File destinationFolder = new File(targetFolder);
            archiver.extract(file, destinationFolder);
            log.info("Extracted: {} files to {}", destinationFolder.listFiles().length, destinationFolder.getPath());

            // Going over extracted files in order to find archives in them
            File[] filesList = destinationFolder.listFiles();
            for (File currentFile : filesList) {
                if (currentFile.isFile()) {
                    try {
                        ArchiverFactory.createArchiver(currentFile); // Throws an exception when not an archive
                        handleFile(currentFile, destinationFolder.getPath());
                        currentFile.delete();
                    } catch (Exception ex) { // This is ok, just continue

                    }
                }
            }
        } catch (Exception ex) { // Not a compressed file
            Files.copy(file.toPath(), new File(destinationPath + "\\" + file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static File initializeFolder(String folderName) {
        folderName = folderName.replace("\\\\", "\\");
        File targetFolder = new File(folderName);
        if (!targetFolder.exists()) {
            if (targetFolder.mkdirs()) {
                log.info("Folder {} Created", targetFolder);
            }
        }

        return targetFolder;
    }

    /**
     * Logs a report of the current folder extension count then initializes the extension count
     */
    private static void reportAndInitExtensionCount() {
        log.info("Extension Count Report:");
        for (Map.Entry<String, Integer> entry: extensionCount.entrySet()) {
            log.info("{}: {}", entry.getKey(), entry.getValue() );
        }
        log.info("\n");

        extensionCount = new HashMap<>();
    }

    private static File initializeSourceFolder() throws Exception {
        File sourceFolder = new File(archiveSources);
        if (!sourceFolder.exists()) {
            throw new Exception("Source folder: " + archiveSources + "doesn't exist... exiting");
        }

        return sourceFolder;
    }
}
