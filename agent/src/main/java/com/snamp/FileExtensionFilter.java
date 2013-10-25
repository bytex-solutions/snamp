package com.snamp;

import java.io.*;

/**
 * Represents a extension-based filter for files.
 * @author roman
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

    public static String getExtension(final String fileName){
        final int i = fileName.lastIndexOf('.');
        final int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        return i > p ? fileName.substring(i + 1) : "";
    }

    @Override
    public final boolean accept(final File file, final String fileName) {
        return extension.equals(getExtension(fileName));
    }
}
