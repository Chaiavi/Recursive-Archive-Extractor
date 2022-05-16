package org.chaiware.recursive_archive_extractor;

import org.apache.commons.compress.utils.FileNameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Goes over the source folder recursively and copies all files to the target preserving the original folder structure <br/>
 * In the case of an archive, it extracts its contents to the target<br/>
 * Archives within archives will also be extracted
 */
public class RecursiveArchiveExtractor {
    private static final Logger log = LoggerFactory.getLogger(RecursiveArchiveExtractor.class);
    static String archiveSources = "";
    static String extractedTarget = "";

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
        File destination = new File(extractedTarget + file.getPath().replace(archiveSources, ""));
        initializeFolder(destination);

        File[] filesList = file.listFiles();
        for (File currentFile : filesList) {
            if (currentFile.isFile()) {
                handleFile(currentFile, destination);
            } else { // Folder
                handleFolder(currentFile);
            }
        }
    }

    /**
     * Handles a single file <br/>
     * If it is a regular file then copy it to the target <br/>
     * If it is an archived file then extract it to the target
     */
    private static void handleFile(File file, File destinationPath) throws IOException {
        if (CCExtractor.isArchive(file.toPath())) {
            String baseFilename = FileNameUtils.getBaseName(file.getName());
            // Solves a bug when an archive has no suffix, then the folder can't be created as there is a file with the same name
            baseFilename = !(baseFilename.equals(file.getName())) ? baseFilename : baseFilename + ".zip";
            File destinationArchiveFolder = Paths.get(destinationPath.getPath(), baseFilename).toFile();
            initializeFolder(destinationArchiveFolder);
            CCExtractor.extract(file.toPath(), destinationArchiveFolder);
            log.info("Extracted: {} files to {}", destinationArchiveFolder.listFiles().length, destinationArchiveFolder.getPath());

            // Going over extracted files in order to find archives in them
            for (File currentFile : destinationArchiveFolder.listFiles()) {
                if (currentFile.isFile() && CCExtractor.isArchive(currentFile.toPath())) {
                    handleFile(currentFile, destinationArchiveFolder);
                    currentFile.delete();
                }
            }
        } else { // is a regular file
            Files.copy(file.toPath(), new File(destinationPath + "\\" + file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static File initializeFolder(File targetFolder) {
        if (targetFolder.mkdirs()) {
            log.info("Folder {} Created", targetFolder.getPath());
        }

        return targetFolder;
    }

    private static File initializeSourceFolder() throws Exception {
        File sourceFolder = new File(archiveSources);
        if (!sourceFolder.exists()) {
            throw new Exception("Source folder: " + archiveSources + "doesn't exist... exiting");
        }

        return sourceFolder;
    }
}
