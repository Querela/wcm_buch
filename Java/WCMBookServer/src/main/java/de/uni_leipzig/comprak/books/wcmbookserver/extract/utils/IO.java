package de.uni_leipzig.comprak.books.wcmbookserver.extract.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * Created by Erik on 28.11.2014.
 */
public class IO {
    private final static Logger log = LoggerFactory.getLogger(IO.class);
    private final static Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Write the given content <i>content</i> into the file <i>outputFile</i>. If the file <i>outputFile</i> already exists it will be overwritten.
     *
     * @param outputFile File to write into
     * @param content    String to write
     * @return true if successful, false if error occured (e. g. file is null or write error)
     */
    public static boolean writeContentToFile(File outputFile, String content) {
        if (outputFile == null) {
            log.warn("Write call with null File! Abort.");
            return false;
        } // if

        log.debug("Write content to file \"{}\"", outputFile);
        try {
            Files.write(outputFile.toPath(), content.getBytes(UTF8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            log.error("Write content to file failed!", e);
            return false;
        } // try-catch

        if (log.isDebugEnabled()) {
            log.debug("Wrote {} bytes to file \"{}\"", outputFile.length(), outputFile);
        } // if

        return true;
    }

    /**
     * Reads the whole content from the given file <i>inputFile</i> and returns it as a String. If an error occured then <i>null</i> will be returned.
     *
     * @param inputFile File to read from
     * @return String if successful else null
     */
    public static String readContentFromFile(File inputFile) {
        if (inputFile == null || !inputFile.exists() || !inputFile.isFile()) {
            log.warn("Read call with null or invalid File! Abort.");
            return null;
        } // if

        if (log.isDebugEnabled()) {
            log.debug("Read content ({} bytes) from file \"{}\"", inputFile.length(), inputFile);
        } // if

        try {
            return new String(Files.readAllBytes(inputFile.toPath()), UTF8);
        } catch (IOException e) {
            log.error("Write content to file failed!", e);
        } // try-catch

        return null;
    }
}
