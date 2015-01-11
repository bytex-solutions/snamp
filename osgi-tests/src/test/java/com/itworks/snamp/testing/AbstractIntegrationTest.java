package com.itworks.snamp.testing;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.AbstractProvisionOption;
import org.ops4j.pax.exam.options.FrameworkPropertyOption;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import java.util.*;

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

    protected AbstractIntegrationTest(final List<AbstractProvisionOption<?>> deps){
        this.dependencies = new ArrayList<>(deps);
    }

    protected AbstractIntegrationTest(final AbstractProvisionOption<?>... deps){
        this(Arrays.asList(deps));
    }

    protected Collection<FrameworkPropertyOption> getFrameworkProperties(){
        return Collections.emptyList();
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
        result.addAll(getFrameworkProperties());
        return result.toArray(new Option[result.size()]);
    }

    /**
     * Returns context of the test bundle.
     * @return The context of the test bundle.
     */
    protected final BundleContext getTestBundleContext(){
        return FrameworkUtil.getBundle(getClass()).getBundleContext();
    }
}
