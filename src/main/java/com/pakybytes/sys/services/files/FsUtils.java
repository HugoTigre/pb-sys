package com.pakybytes.sys.services.files;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * @author Hugo Tigre
 * @since 1.0.0
 */
public class FsUtils {

    /**
     * Reads an entire text file to assure that the file can be
     * read by a particular charset.
     * <p>
     * Warning: This could take a while, depending on the available charsets and
     * file size. The charset returned is not necessarily the right one, but a
     * possible one that can read the entire file without error.
     *
     * @param file The path to the file
     * @return The string of the charset that read the entire file.
     */
    public String detectCharset(Path file) {
        String charset = null;

        for (Charset cs : Charset.availableCharsets().values()) {
            charset = cs.toString().replace("_", "-");

            try (BufferedReader reader = Files.newBufferedReader(file, Charset.forName(charset))) {
                while ((reader.readLine()) != null) {
                }
                break;
            } catch (MalformedInputException miex) {
                continue; // Try another charset
            } catch (IOException ioex) {
                throw new SysFsException(ioex);
            }
        }

        return charset;
    }


    private static class SysFsException extends RuntimeException {
        SysFsException(Throwable ex) {
            super(ex);
        }
    }
}
