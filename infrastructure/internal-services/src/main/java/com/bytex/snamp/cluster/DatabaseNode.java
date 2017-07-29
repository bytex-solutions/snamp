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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class DatabaseNode extends OServer {
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
            try {
                databaseHome = Files.createTempDirectory("orientdb").toFile();
            } catch (final IOException e) {
                throw new ExceptionInInitializerError(e);
            }
        if (!databaseHome.exists()) {
            final boolean created = databaseHome.mkdir();
            assert created;
        }
        System.setProperty(Orient.ORIENTDB_HOME, databaseHome.getAbsolutePath());
    }

    private final File databaseConfigFile;
    private SnampDatabase snampDatabase;

    DatabaseNode(final HazelcastInstance hazelcast) throws ClassNotFoundException, JMException, IOException {
        super(true);
        databaseConfigFile = DatabaseConfigurationFile.EMBEDDED_CONFIG.toFile(true);
        updateConfig();
        distributedManager = new OrientDistributedEnvironment(hazelcast);
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
            snampDatabase.close();

        }
        credentials.login(snampDatabase);
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
