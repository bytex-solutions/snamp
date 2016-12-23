package com.bytex.snamp.cluster;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ImmutableMap;
import com.hazelcast.core.HazelcastInstance;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.config.OServerConfigurationManager;
import com.orientechnologies.orient.server.config.OServerParameterConfiguration;
import com.orientechnologies.orient.server.plugin.OServerPlugin;
import com.orientechnologies.orient.server.plugin.OServerPluginInfo;
import org.osgi.framework.BundleContext;

import javax.management.JMException;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.bytex.snamp.internal.Utils.interfaceStaticInitialize;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
class DatabaseNode extends OServer {
    private static final String SNAMP_DATABASE = "snamp_storage";

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
    private SnampDatabase snampDatabase;

    DatabaseNode(final HazelcastInstance hazelcast) throws ReflectiveOperationException, JMException, JAXBException, IOException {
        super(true);
        databaseConfigFile = DatabaseConfigurationFile.EMBEDDED_CONFIG.toFile(true);
        updateConfig();
        distributedManager = new OrientHazelcastBridge(hazelcast);
    }

    final ODatabaseDocumentTx getSnampDatabase(){
        return snampDatabase;
    }

    private OServerPluginInfo createPluginInfo(final OServerPlugin plugin) {
        final BundleContext context = Utils.getBundleContextOfObject(this);
        final String name = "DistributedOrientDB";
        final String description = "OrientDB distributed environment based on Hazelcast";
        final String version = context == null ? "Not Defined" : context.getBundle().getVersion().toString();
        return new OServerPluginInfo(name, version, description, null, plugin, ImmutableMap.of(), System.currentTimeMillis(), null);
    }

    @Override
    public DatabaseNode activate() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super.activate();
        //activate Hazelcast-based distributor
        if (distributedManager instanceof OServerPlugin) {
            final OServerPlugin plugin = (OServerPlugin) distributedManager;
            plugin.config(this, ArrayUtils.emptyArray(OServerParameterConfiguration[].class));
            plugin.startup();
            pluginManager.registerPlugin(createPluginInfo(plugin));
        }
        final DatabaseCredentials credentials = DatabaseCredentials.resolveSnampUser(getBundleContextOfObject(this), getConfiguration());
        if(credentials == null)
            throw new SecurityException(String.format("Credentials for special SNAMP user not found. Database %s cannot be opened", databaseConfigFile));
        //initialize SNAMP database
        snampDatabase = new SnampDatabase(getStoragePath(SNAMP_DATABASE));
        if (snampDatabase.exists())
            credentials.login(snampDatabase);
         else {
            snampDatabase.create();
        }
        return this;
    }

    @Override
    public boolean shutdown() {
        if (snampDatabase != null)
            snampDatabase.close();
        snampDatabase = null;
        final boolean success = super.shutdown();
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
