package com.bytex.snamp.testing;

import com.google.common.base.StandardSystemProperty;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

//!!! DO NOT USE packages from SNAMP

/**
 * Represents a base class for all OSGi integration tests.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public abstract class AbstractIntegrationTest extends AbstractTest {
    /**
     * Represents test environment builder.
     */
    protected abstract static class EnvironmentBuilder {
        /**
         * Returns an array of system properties to be defined for the container' process.
         * @param testType The type of the integration test class.
         * @return An array of system properties.
         */
        public Map<String, String> getSystemProperties(final Class<? extends AbstractIntegrationTest> testType){
            final Map<String, String> result = new HashMap<>(10);
            for(final SystemProperties props : TestUtils.getAnnotations(testType, SystemProperties.class))
                for(final String definition: props.value()){
                    final String[] pair = definition.split("=");
                    if(pair.length == 2)
                        result.put(pair[0], pair[1]);
                }
            return result;
        }

        /**
         * Returns a set of features to be installed into test OSGi container.
         * @param testType The type of the integration test class.
         * @return A collection of features.
         */
        public abstract Collection<KarafFeaturesOption> getFeatures(final Class<? extends AbstractIntegrationTest> testType);

        public abstract Collection<MavenArtifactProvisionOption> getBundles(final Class<? extends  AbstractIntegrationTest> testType);

        /**
         * Returns an array of packages to be imported into test OSGi bundle.
         * @param testType The type of the integration test class.
         * @return An array of package names.
         */
        public String[] getPackages(final Class<? extends AbstractIntegrationTest> testType){
            final Collection<String> packages = new HashSet<>(15);
            for(final ImportPackages pkg: TestUtils.getAnnotations(testType, ImportPackages.class))
                Collections.addAll(packages, pkg.value());
            return packages.toArray(new String[packages.size()]);
        }

        /**
         * Returns an array of system properties that should be propagated into the test container.
         * @return An array of system properties that should be propagated into the test container.
         */
        public String[] getPropagatedProperties(final Class<? extends AbstractIntegrationTest> testType){
            final Collection<String> systemProperties = new HashSet<>(15);
            for (final PropagateSystemProperties prop : TestUtils.getAnnotations(testType, PropagateSystemProperties.class))
                Collections.addAll(systemProperties, prop.value());
            return systemProperties.toArray(new String[systemProperties.size()]);
        }

        public Map<String, String> getFrameworkProperties(final Class<? extends AbstractIntegrationTest> testType){
            final Map<String, String> frameworkProperties = new HashMap<>(20);
            for(final FrameworkProperties properties: TestUtils.getAnnotations(testType, FrameworkProperties.class))
                for(final FrameworkProperty prop: properties.value())
                    frameworkProperties.put(prop.name(), prop.value());
            return frameworkProperties;
        }
    }

    private static final String TEST_CONTAINER_INDICATOR = "com.bytex.snamp.testing.isInContainer";
    private static final String PROJECT_DIR = "com.bytex.snamp.testing.project.dir";

    private final EnvironmentBuilder builder;

    protected AbstractIntegrationTest(final EnvironmentBuilder builder){
        this.builder = Objects.requireNonNull(builder);
    }

    /**
     * Determines whether this test is executed in the separated test container.
     * @return {@literal true}, if this test is executed in the test container; otherwise, {@literal false}.
     */
    protected static boolean isInTestContainer(){
        return Boolean.getBoolean(TEST_CONTAINER_INDICATOR);
    }

    private Option getPropagatedProperties() {
        return propagateSystemProperties(builder.getPropagatedProperties(getClass()));
    }

    /**
     * Determines whether the forked test container should be suspended before
     * debugger will be attached.
     * @return {@literal true} to enable debugging; otherwise, {@literal false}.
     */
    protected boolean enableRemoteDebugging(){
        return false;
    }

    private Option[] configureTestingRuntimeImpl(){
        final MavenArtifactUrlReference karafUrl = maven()
                .groupId("org.apache.karaf")
                .artifactId("apache-karaf")
                .versionAsInProject()
                .type("tar.gz");
        final List<Option> result = new ArrayList<>(20);
        result.add(karafDistributionConfiguration().frameworkUrl(karafUrl)
                .name("Apache Karaf")
                .unpackDirectory(new File("exam")));
        result.add(logLevel(LogLevelOption.LogLevel.INFO));
        if(enableRemoteDebugging())
            result.add(debugConfiguration("32441", true));
        result.add(systemProperty(TEST_CONTAINER_INDICATOR).value("true"));
        result.addAll(builder.getSystemProperties(getClass()).entrySet().stream().map(sp -> systemProperty(sp.getKey()).value(sp.getValue())).collect(Collectors.toList()));
        result.addAll(builder.getFrameworkProperties(getClass()).entrySet().stream().map(fp -> frameworkProperty(fp.getKey()).value(fp.getValue())).collect(Collectors.toList()));
        result.add(systemProperty(PROJECT_DIR).value(System.getProperty(PROJECT_DIR)));
        result.add(getPropagatedProperties());
        // https://ops4j1.jira.com/wiki/display/PAXEXAM3/Configuration+Options
        result.add(keepRuntimeFolder());
        result.add(bootDelegationPackage("jdk.nashorn.*"));
        result.add(bootDelegationPackage("org.bouncycastle*"));
        result.addAll(builder.getFeatures(getClass()));
        result.addAll(builder.getBundles(getClass()));
        return result.toArray(new Option[result.size()]);
    }

    /**
     * Gets path of the directory in which the host Test Engine was executed.
     * @return The path of the directory.
     */
    protected static String getProjectRootDir(){
        return System.getProperty(PROJECT_DIR, StandardSystemProperty.USER_DIR.value());
    }

    protected static String getPathToFileInProjectRoot(final String fileName){
        return new File(getProjectRootDir(), fileName).getAbsolutePath();
    }

    /**
     * Returns configuration of Pax Exam testing runtime.
     * @return An array of Pax Exam configuration.
     */
    @Configuration
    public final Option[] configureTestingRuntime(){
        return isInTestContainer() ? new Option[0] : configureTestingRuntimeImpl();
    }

    /**
     * Returns context of the test bundle.
     * @return The context of the test bundle.
     */
    protected final BundleContext getTestBundleContext(){
        return FrameworkUtil.getBundle(getClass()).getBundleContext();
    }

    /**
     * Prepares test container in the host process.
     * @param builder The container builder to setup.
     * @return Prepared test container.
     */
    @ProbeBuilder
    public final TestProbeBuilder setupProbeConfiguration(final TestProbeBuilder builder) {
        final String[] packages = this.builder.getPackages(getClass());
        if (packages != null && packages.length > 0)
            builder.setHeader("Import-Package", TestUtils.join(packages, ','));
        return builder;
    }
}
