package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.itworks.snamp.licensing.LicensedFrameworkService;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpResourceAdapter extends AbstractResourceAdapter implements LicensedFrameworkService<SnmpAdapterLimitations> {
    private final SnmpAgent agent;

    /**
     * Initializes a new resource adapter.
     *
     * @param resources A collection of managed resources to be exposed in protocol-specific manner
     *                  to the outside world.
     */
    protected SnmpResourceAdapter(final int port,
                                  final String hostName,
                                  final SecurityConfiguration securityOptions,
                                  final int socketTimeout,
                                  final Collection<ManagedResourceConfiguration> resources) throws IOException {
        super(resources);
        agent = new SnmpAgent(port, hostName, securityOptions, socketTimeout);
    }

    /**
     * Gets logger associated with this adapter.
     *
     * @return The logger associated with this adapter.
     */
    @Override
    public Logger getLogger() {
        return SnmpHelpers.getLogger();
    }

    /**
     * Starts the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     *
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    @Override
    protected boolean start() {
        try {
            return agent.start();
        }
        catch (final IOException e) {
            getLogger().log(Level.SEVERE, "Unable to start SNMP Agent", e);
            return false;
        }
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    @Override
    protected void stop() {
        agent.stop();
    }

    /**
     * Returns license limitations associated with this plugin.
     *
     * @return The license limitations applied to this plugin.
     */
    @Override
    public SnmpAdapterLimitations getLimitations() {
        return SnmpAdapterLimitations.current();
    }
}
