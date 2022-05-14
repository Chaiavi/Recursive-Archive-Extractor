package org.chaiware.recursive_archive_extractor;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Common Compress archive extractor
 */
public class CCExtractor {
    static Logger log = LoggerFactory.getLogger(CCExtractor.class);

    public static boolean isArchivePREV(String fileNamePath) {
        String lowerCasedFileName = fileNamePath.toLowerCase(Locale.ROOT);

        return lowerCasedFileName.endsWith(ArchiveStreamFactory.ZIP);
    }

    /** Determines if the requested file is an archive file or not */
    public static boolean isArchive(Path fileNamePath) throws IOException {
        try {
            InputStream inputStream = new BufferedInputStream(Files.newInputStream(fileNamePath));
            String archiveType = ArchiveStreamFactory.detect(inputStream); // Will throw an exception if not an archive
            return true;
        } catch (ArchiveException ex) { // Not an archive
            return false;
        }
    }

    /**
     * Extracts the given archive to the destination<br/>
     * Returns false when extraction failed
     */
    public static boolean extract(Path archiveFile, File extractDirectoryPath) {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(Files.newInputStream(archiveFile));
            ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();
            ArchiveInputStream archiveInputStream = archiveStreamFactory.createArchiveInputStream(inputStream);

            ArchiveEntry archiveEntry = null;
            while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {
                Path destinationFilePath = Paths.get(extractDirectoryPath.getPath(), archiveEntry.getName().replace("?", "-")); // Fixes a bug with a filename containing ANSI characters
                File file = destinationFilePath.toFile();
                if (archiveEntry.isDirectory()) {
                    if (!file.isDirectory()) {
                        file.mkdirs();
                    }
                } else {
                    File parent = file.getParentFile();
                    if (parent.isDirectory()) {
                        parent.mkdirs();
                    }

                    try (OutputStream outputStream = Files.newOutputStream(destinationFilePath)) {
                        IOUtils.copy(archiveInputStream, outputStream);
                    }
                }
            }
        } catch (IOException e) {
            log.error("IO Failure", e);
            return false;
        } catch (ArchiveException e) {
            log.error("Failed Extracting the Archive", e);
            return false;
        }

        return true;
    }
}
