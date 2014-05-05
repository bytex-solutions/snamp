package com.itworks.snamp.testing;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.AbstractProvisionOption;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;

/**
 * Represents a base class for all OSGi integration tests.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@RunWith(PaxExam.class)
public abstract class AbstractIntegrationTest extends AbstractTest {
    private final List<AbstractProvisionOption<?>> dependencies;
    @Inject
    private BundleContext context;

    protected AbstractIntegrationTest(final List<AbstractProvisionOption<?>> deps){
        this.dependencies = new ArrayList<>(deps);
        this.context = null;
    }

    protected AbstractIntegrationTest(final AbstractProvisionOption<?>... deps){
        this(Arrays.asList(deps));
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

    /**
     * Returns context of the test bundle.
     * @return The context of the test bundle.
     */
    protected final BundleContext getTestBundleContext(){
        return context;
    }
}
