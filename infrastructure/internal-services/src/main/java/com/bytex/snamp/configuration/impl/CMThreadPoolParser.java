package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Represents parser of {@link SerializableThreadPoolConfiguration}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class CMThreadPoolParser extends AbstractConfigurationParser<SerializableThreadPoolConfiguration> {
    public static final String PID = "com.bytex.snamp.concurrency.threadPools";

    private static final class ThreadPoolConfigurationException extends PersistentConfigurationException{
        private static final long serialVersionUID = 2668941491644815016L;

        private ThreadPoolConfigurationException(final Throwable e) {
            super(PID, SerializableThreadPoolConfiguration.class, e);
        }
    }

    CMThreadPoolParser(){
    }

    public static SerializableThreadPoolConfiguration deserialize(final String poolName,
                                                                  final Dictionary<String, ?> properties,
                                                                  final ClassLoader caller) throws IOException {
        final byte[] serializedConfig = Utils.getProperty(properties, poolName, byte[].class, (Supplier<byte[]>) () -> new byte[0]);
        return IOUtils.deserialize(serializedConfig, SerializableThreadPoolConfiguration.class, caller);
    }

    private static Configuration getConfig(final ConfigurationAdmin admin) throws IOException {
        return admin.getConfiguration(PID, null);
    }

    private void readThreadPools(final Configuration input, final Map<String, SerializableThreadPoolConfiguration> output) throws IOException{
        final Dictionary<String, ?> threadPools = input.getProperties();
        final Enumeration<String> names = threadPools.keys();
        while (names.hasMoreElements()) {
            final String poolName = names.nextElement();
            output.put(poolName, deserialize(poolName, threadPools, getClass().getClassLoader()));
        }
    }

    @Override
    void fill(final ConfigurationAdmin input, final Map<String, SerializableThreadPoolConfiguration> output) throws IOException {
        readThreadPools(getConfig(input), output);
    }

    @Override
    void removeAll(final ConfigurationAdmin input) throws IOException {
        getConfig(input).delete();
    }

    private static byte[] serialize(final SerializableThreadPoolConfiguration input) throws ThreadPoolConfigurationException{
        try {
            return IOUtils.serialize(input);
        } catch (final IOException e) {
            throw new ThreadPoolConfigurationException(e);
        }
    }

    @Override
    void saveChanges(final SerializableAgentConfiguration source, final ConfigurationAdmin dest) throws IOException {
        final Configuration config = getConfig(dest);
        source.getEntities(SerializableThreadPoolConfiguration.class).modifiedEntries((poolName, poolConfig) -> {
            config.getProperties().put(poolName, serialize(poolConfig));
            return true;
        });
    }
}
