package com.bytex.snamp.cluster;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.core.KeyValueStorage;
import com.bytex.snamp.core.SharedObjectRepository;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ImmutableMap;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.config.OServerConfigurationManager;
import com.orientechnologies.orient.server.config.OServerParameterConfiguration;
import com.orientechnologies.orient.server.plugin.OServerPlugin;
import com.orientechnologies.orient.server.plugin.OServerPluginInfo;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.JMException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents database node in non-distributed environment.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
class DatabaseNode extends OServer implements SharedObjectRepository<KeyValueStorage> {
    private static final String SNAMP_DATABASE = "snamp_storage";

    static {
        //fix performance issues with default hash security provider
        OGlobalConfiguration.SECURITY_USER_PASSWORD_SALT_ITERATIONS.setValue(1024);
        //setup database home directory
        final String KARAF_DATA_DIR = "karaf.data";
        final File databaseHome;
        if (System.getProperties().containsKey(Orient.ORIENTDB_HOME))
            databaseHome = new File(System.getProperty(Orient.ORIENTDB_HOME));
        else if (System.getProperties().containsKey(KARAF_DATA_DIR))
            databaseHome = Paths.get(System.getProperty(KARAF_DATA_DIR), "snamp").toFile();
        else
            databaseHome = Utils.callAndWrapException(() -> Files.createTempDirectory("orientdb").toFile(), ExceptionInInitializerError::new);
        if (!databaseHome.exists()) {
            final boolean created = databaseHome.mkdir();
            assert created;
        }
        System.setProperty(Orient.ORIENTDB_HOME, databaseHome.getAbsolutePath());
    }

    private final File databaseConfigFile;
    private SnampDatabase snampDatabase;

    DatabaseNode() throws ClassNotFoundException, JMException, IOException {
        super(true);
        databaseConfigFile = DatabaseConfigurationFile.EMBEDDED_CONFIG.toFile(true);
        serverCfg = new OServerConfigurationManager(databaseConfigFile);
        distributedManager = null;
    }

    @Nonnull
    @Override
    public final OrientKeyValueStorage getSharedObject(@Nonnull final String collectionName) {
        return snampDatabase.getKeyValueStorage(collectionName);
    }

    @Override
    public final void releaseSharedObject(@Nonnull final String collectionName) {
        snampDatabase.dropKeyValueStorage(collectionName);
    }

    ODatabaseDocumentTx getSnampDatabase(){
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
        if (credentials == null)
            throw new SecurityException(String.format("Credentials for special SNAMP user not found. Database %s cannot be opened", databaseConfigFile));
        //initialize SNAMP database
        snampDatabase = new SnampDatabase(getStoragePath(SNAMP_DATABASE));
        if (!snampDatabase.exists()) {
            snampDatabase.create();
            credentials.createUser(snampDatabase);
            snampDatabase.init();
            snampDatabase.close();
        }
        credentials.login(snampDatabase);
        return this;
    }

    @Override
    public final boolean shutdown() {
        if (snampDatabase != null)
            snampDatabase.close();
        snampDatabase = null;
        try {
            return super.shutdown();
        } finally {
            distributedManager = null;
        }
    }

    @Override
    public String getStoragePath(final String iName) {
        final String dbURL = getConfiguration().getStoragePath(iName);
        return isNullOrEmpty(dbURL) ?
                "plocal:" + Paths.get(getDatabaseDirectory(), iName).toFile().getAbsolutePath() :
                super.getStoragePath(iName);
    }

    void dropDatabases(){
        snampDatabase.drop(); 
    }
}
