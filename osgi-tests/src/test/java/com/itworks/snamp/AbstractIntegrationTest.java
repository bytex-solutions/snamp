package com.itworks.snamp;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.*;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.AbstractProvisionOption;

import static org.ops4j.pax.exam.CoreOptions.*;

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


    protected AbstractIntegrationTest(final AbstractProvisionOption<?>... deps){
        dependencies = Arrays.asList(deps);
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
