package com.bytex.snamp.database;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.io.IOUtils;
import com.hazelcast.core.HazelcastInstance;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.config.OServerConfigurationManager;
import com.orientechnologies.orient.server.config.OServerParameterConfiguration;
import com.orientechnologies.orient.server.distributed.impl.ODistributedAbstractPlugin;

import javax.management.JMException;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.bytex.snamp.internal.Utils.interfaceStaticInitialize;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OrientDatabaseService extends OServer implements DatabaseService {

    static final File ORIENTDB_HOME = interfaceStaticInitialize(() -> {
        String karafDataFolder = System.getProperty("karaf.data");
        final String ORIENTDB_PREFIX = "orientdb";
        final File home;
        if(isNullOrEmpty(karafDataFolder))
             home = Files.createTempDirectory(ORIENTDB_PREFIX).toFile();
        else {
            home = Paths.get(karafDataFolder, ORIENTDB_PREFIX).toFile();
            if (!home.exists()) {
                final boolean created = home.mkdir();
                assert created;
            }
        }

        System.setProperty(Orient.ORIENTDB_HOME, home.getAbsolutePath());
        return home;
    });

    private final File databaseConfigFile;

    public OrientDatabaseService(final HazelcastInstance hazelcast) throws ReflectiveOperationException, JMException, JAXBException, IOException {
        super(false);
        databaseConfigFile = DatabaseConfigurationFile.DISTRIBUTED_CONFIG.toFile(true);
        final ODistributedAbstractPlugin hazelcastPlugin = new OrientHazelcastBridge(hazelcast);
        hazelcastPlugin.config(this, ArrayUtils.emptyArray(OServerParameterConfiguration[].class));
        distributedManager = hazelcastPlugin;
        updateConfig();
    }

    private void updateConfig() throws IOException {
        serverCfg = new OServerConfigurationManager(databaseConfigFile);
    }

    @Override
    public void restart() throws IOException {
        try {
            super.restart();
        } catch (final ReflectiveOperationException e) {
            throw new IOException(e);
        }
    }

    /**
     * Sets a new configuration.
     *
     * @param input Reader of configuration content.
     * @throws IOException Unable to parse new configuration.
     */
    @Override
    public void setupConfiguration(final Reader input) throws IOException {
        try (final Writer configWriter = new OutputStreamWriter(new FileOutputStream(databaseConfigFile, false), IOUtils.DEFAULT_CHARSET)) {
            IOUtils.copy(input, configWriter);
        }
        updateConfig();
    }

    @Override
    public void readConfiguration(final Writer output) throws IOException {
        try (final Reader configReader = new InputStreamReader(new FileInputStream(databaseConfigFile), IOUtils.DEFAULT_CHARSET)) {
            IOUtils.copy(configReader, output);
        }
    }
}
