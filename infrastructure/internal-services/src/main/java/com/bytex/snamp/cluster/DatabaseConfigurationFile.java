package com.bytex.snamp.cluster;

import com.bytex.snamp.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents database configuration file.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
enum DatabaseConfigurationFile {
    DISTRIBUTED_CONFIG("orientdb-distributed.json"),
    EMBEDDED_CONFIG("orientdb.xml");

    private final String fileName;

    DatabaseConfigurationFile(final String fileName){
        this.fileName = fileName;
    }

    final File toFile(final boolean createDefaultIfNotExists) throws IOException {
        //read OrientDB distributed configuration
        final String karafConfigFolder = System.getProperty("karaf.etc");
        final File result = isNullOrEmpty(karafConfigFolder) ?
                Files.createTempFile("orientdb", fileName).toFile() :
                Paths.get(karafConfigFolder, fileName).toFile();
        if ((!result.exists() || result.length() == 0) && createDefaultIfNotExists)
            writeDefaultConfig(result);
        return result;
    }

    private void writeDefaultConfig(final File output) throws IOException {
        try (final InputStream configStream = getClass().getResourceAsStream(fileName);
             final Reader configReader = new InputStreamReader(configStream, IOUtils.DEFAULT_CHARSET);
             final Writer writer = new OutputStreamWriter(new FileOutputStream(output, false), IOUtils.DEFAULT_CHARSET)) {
            IOUtils.copy(configReader, writer);
        }
    }
}
