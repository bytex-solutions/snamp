package com.itworks.snamp.configuration;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class XmlConfigurationTest {
    @Inject
    private ConfigurationManager configMgr;

    @Configuration
    public final Option[] configOptions(){
        return options(
                mavenBundle("com.itworks.snamp", "corlib", "1.0.0"),
                mavenBundle("com.itworks.snamp", "config-bundle", "1.0.0"),
                junitBundles());
    }

    @Test
    public final void configManagerTest(){
        assertNotNull(configMgr);
    }
}
