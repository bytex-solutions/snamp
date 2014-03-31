package com.snamp.internal;

import java.io.*;
import java.nio.file.FileSystems;

/**
 * Represents file selection filter based on the file extension. This class cannot be inherited.<br/>
 * <p>
 *     <b>Example:</b><br/>
 *     <pre>{@code
 *     final File dir = new File("/usr/bin");
 *     final File[] soFiles = dir.listFiles(new FileExtensionFilter(".so"));
 *     }
 *     </pre>
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class FileExtensionFilter implements FilenameFilter {
    /**
     * Represents an extension of the file to accept.
     */
    public final String extension;

    /**
     * Initializes a new extension-based filter for files.
     * @param expectedExtension A file extension to accept.
     * @throws IllegalArgumentException expectedExtension is {@literal null}.
     */
    public FileExtensionFilter(final String expectedExtension) throws IllegalArgumentException{
        if(expectedExtension == null) throw new IllegalArgumentException("expectedExtension is null.");
        extension = expectedExtension;
    }

    private static String getExtension(final String fileName){
        final int i = fileName.lastIndexOf('.');
        final int p = fileName.lastIndexOf(FileSystems.getDefault().getSeparator());
        return i > p ? fileName.substring(i + 1) : "";
    }

    /**
     * Determines whether the specified file has expected file extension.
     * @param fileName The file name to check. Cannot be {@literal null}.
     * @return {@literal true}, if the specified file has expected file extension; otherwise, {@literal false}.
     */
    public final boolean accept(final String fileName){
        return extension.equals(getExtension(fileName));
    }

    /**
     * Determines whether the specified file has expected file extension.
     * @param file The file to check.
     * @param fileName The file name to check. Cannot be {@literal null}.
     * @return {@literal true}, if the specified file has expected file extension; otherwise, {@literal false}.
     */
    @Override
    public final boolean accept(final File file, final String fileName) {
        return accept(fileName);
    }
}
