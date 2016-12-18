package com.bytex.snamp.instrumentation.reporters.http.osgi;

import com.bytex.snamp.instrumentation.reporters.Reporter;
import com.bytex.snamp.instrumentation.reporters.http.HttpReporter;
import com.bytex.snamp.instrumentation.reporters.http.HttpReporterSpi;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Represents OSGi activator for {@link HttpReporter}.
 */
public final class HttpReporterActivator implements BundleActivator {
    private ServiceRegistration<Reporter> reporterRegistration;

    @Override
    public void start(final BundleContext context) throws Exception {
        final String snampUrl = System.getProperty(HttpReporterSpi.SNAMP_URL_PROPERTY, context.getProperty(HttpReporterSpi.SNAMP_URL_PROPERTY));
        if(snampUrl == null || snampUrl.isEmpty())
            throw new BundleException("Unable to instantiate HttpReporter for OSGi environment");

        final HttpReporter reporter = new HttpReporter(snampUrl, null);
        final Dictionary<String, String> regProperties = new Hashtable<>();
        regProperties.put(Reporter.TYPE_PROPERTY, HttpReporter.TYPE);
        regProperties.put(HttpReporterSpi.SNAMP_URL_PROPERTY, snampUrl);
        reporterRegistration = context.registerService(Reporter.class, reporter, regProperties);
    }

    @Override
    public void stop(final BundleContext context) {
        if(reporterRegistration != null)
            reporterRegistration.unregister();
    }
}
