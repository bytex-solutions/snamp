package com.itworks.snamp;

import static com.itworks.snamp.configuration.ConfigurationManager.CONFIGURATION_FILE_PROPERTY;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationManager;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.*;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.AbstractProvisionOption;

import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.*;

import java.io.*;
import java.util.*;

/**
 * Represents a base class for all OSGi integration tests.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@RunWith(PaxExam.class)
public abstract class AbstractIntegrationTest extends AbstractTest {
    private final List<AbstractProvisionOption<?>> dependencies;
    private static final File tempConfigurationFile;
    @Inject
    private ConfigurationManager configManager;

    static {
        File configFile = null;
        try {
            configFile = File.createTempFile("snamp-config", ".xml");
            configFile.delete();
        } catch (final IOException e) {
            fail(e.getMessage());
        }
        finally {
            tempConfigurationFile = configFile;
            System.setProperty(CONFIGURATION_FILE_PROPERTY, configFile.getAbsolutePath());
        }

    }

    protected AbstractIntegrationTest(final AbstractProvisionOption<?>... deps){
        dependencies = Arrays.asList(deps);
    }

    /**
     * Creates a new configuration for running this test.
     * @return A new SNAMP configuration used for executing SNAMP bundles.
     */
    protected abstract void setupTestConfiguration(final AgentConfiguration config);

    /**
     * Reads SNAMP configuration from temporary storage.
     * @return Deserialized SNAMP configuration.
     * @throws IOException
     */
    protected final AgentConfiguration readSnampConfiguration() throws IOException{
        return configManager.getCurrentConfiguration();
    }

    /**
     * Saves SNAMP configuration into the output stream.
     * @throws IOException
     */
    @Before
    public final void makeSnampConfiguration() throws IOException{
        setupTestConfiguration(configManager.getCurrentConfiguration());
        configManager.sync();
    }

    /**
     * Returns configuration of Pax Exam testing runtime.
     * @return
     */
    @Configuration
    public final Option[] configureTestingRuntime(){
        final List<Option> result = new ArrayList<>(dependencies.size() + 1);
        result.addAll(dependencies);
        result.add(junitBundles());
        return result.toArray(new Option[0]);
    }
}
