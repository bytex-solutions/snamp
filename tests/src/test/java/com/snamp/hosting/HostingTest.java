package com.snamp.hosting;

import com.snamp.SnampTestSet;
import com.snamp.hosting.*;
import org.junit.*;

import java.io.*;
import java.util.*;

/**
 * Represents an abstract class for building SNAMP adapter tests.
 * @author roman
 */
public abstract class HostingTest extends SnampTestSet implements AgentConfiguration {
    private final String adapterName;
    private final Map<String, String> parameters;
    private Agent agent;

    protected HostingTest(final String adapterName, final Map<String, String> adapterParams){
        this.adapterName = adapterName;
        this.parameters = adapterParams;
    }

    protected void beforeAgentStart(final Agent agent) throws Exception{

    }

    protected void afterAgentStart(final Agent agent) throws Exception{

    }

    @Before
    public final void initializeTestSet() throws Exception{
        agent = new Agent(getAgentHostingConfig());
        beforeAgentStart(agent);
        agent.start(getTargets());
        afterAgentStart(agent);
    }

    protected void beforeAgentStop(final Agent agent) throws Exception{

    }

    protected void afterAgentStop(final Agent agent) throws Exception{

    }

    @After
    public final void cleanupTestSet() throws Exception{
        beforeAgentStop(agent);
        agent.stop();
        afterAgentStop(agent);
        agent.close();
        agent = null;
    }

    /**
     * Returns the agent hosting configuration.
     *
     * @return The agent hosting configuration.
     */
    @Override
    public final HostingConfiguration getAgentHostingConfig() {
        return new HostingConfiguration() {
            @Override
            public final String getAdapterName() {
                return adapterName;
            }

            @Override
            public final void setAdapterName(final String adapterName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public final Map<String, String> getHostingParams() {
                return parameters;
            }
        };
    }

    /**
     * Creates clone of the current configuration.
     *
     * @return
     */
    @Override
    public final AgentConfiguration clone() {
        throw new UnsupportedOperationException();
    }

    /**
     * Empty implementation of ManagementTargetConfiguration interface
     *
     * @return implementation of ManagementTargetConfiguration interface
     */
    @Override
    public final ManagementTargetConfiguration newManagementTargetConfiguration() {
        throw new UnsupportedOperationException();
    }

    /**
     * Serializes this object into the specified stream.
     *
     * @param output
     * @throws UnsupportedOperationException Serialization is not supported.
     * @throws java.io.IOException           Some I/O error occurs.
     */
    @Override
    public final void save(final OutputStream output) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Reads the file and fills the current instance.
     *
     * @param input
     * @throws UnsupportedOperationException Deserialization is not supported.
     * @throws java.io.IOException           Cannot read from the specified stream.
     */
    @Override
    public final void load(final InputStream input) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Imports the state of specified object into this object.
     *
     * @param input
     */
    @Override
    public final void load(final AgentConfiguration input) {
        throw new UnsupportedOperationException();
    }
}
