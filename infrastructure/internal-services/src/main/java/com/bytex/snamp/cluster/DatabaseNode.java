package com.bytex.snamp.cluster;

import com.bytex.snamp.ArrayUtils;
import com.hazelcast.core.HazelcastInstance;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.exception.OConfigurationException;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.config.*;
import com.orientechnologies.orient.server.network.OServerNetworkListener;
import com.orientechnologies.orient.server.network.OServerSocketFactory;
import com.orientechnologies.orient.server.network.protocol.ONetworkProtocol;
import com.orientechnologies.orient.server.plugin.OServerPlugin;

import javax.management.JMException;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.bytex.snamp.internal.Utils.interfaceStaticInitialize;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
class DatabaseNode extends OServer {
    public static final File ORIENTDB_HOME = interfaceStaticInitialize(() -> {
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

    static {
        OGlobalConfiguration.SECURITY_USER_PASSWORD_SALT_ITERATIONS.setValue(1024); //fix performance issues with default hash security provider
    }

    private final File databaseConfigFile;

    DatabaseNode(final HazelcastInstance hazelcast) throws ReflectiveOperationException, JMException, JAXBException, IOException {
        super(true);
        databaseConfigFile = DatabaseConfigurationFile.EMBEDDED_CONFIG.toFile(true);
        updateConfig();
        distributedManager = new OrientHazelcastBridge(hazelcast);
    }

    @Override
    public DatabaseNode activate() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super.activate();
        if (distributedManager instanceof OServerPlugin) {
            final OServerPlugin plugin = (OServerPlugin) distributedManager;
            plugin.config(this, ArrayUtils.emptyArray(OServerParameterConfiguration[].class));
            plugin.startup();
        }
        return this;
    }

    @Override
    public boolean shutdown() {
        final boolean success = super.shutdown();
        if (success && distributedManager instanceof OServerPlugin)
            ((OServerPlugin) distributedManager).shutdown();
        distributedManager = null;
        return success;
    }

    @Override
    public String getStoragePath(final String iName) {
        final String dbURL = getConfiguration().getStoragePath(iName);
        return isNullOrEmpty(dbURL) ?
                "plocal:" + Paths.get(getDatabaseDirectory(), iName).toFile().getAbsolutePath() :
                super.getStoragePath(iName);
    }

    private void updateConfig() throws IOException {
        serverCfg = new OServerConfigurationManager(databaseConfigFile);
    }
}
