package com.bytex.snamp.instrumentation.reporters.http;

import com.bytex.snamp.instrumentation.reporters.LazyReporter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Represents Provider for HTTP-based reporter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class HttpReporterSpi extends LazyReporter<HttpReporter> {
    public static final String SNAMP_URL_PROPERTY = "com.bytex.snamp.url";

    public HttpReporterSpi(){

    }

    private static boolean isNullOrEmpty(final String value){
        return value == null || value.isEmpty();
    }

    private static Map<String, ?> fromSystemProperties(){
        final Properties systemProps = System.getProperties();
        final Map<String, String> result = new HashMap<String, String>();
        for(final String name: systemProps.stringPropertyNames())
            result.put(name, systemProps.getProperty(name));
        return result;
    }

    @Override
    protected HttpReporter createReporter() throws IOException {
        final String url = System.getProperty(SNAMP_URL_PROPERTY);
        if (isNullOrEmpty(url))
            return null;
        try {
            return new HttpReporter(url, fromSystemProperties());
        } catch (final URISyntaxException e) {
            throw new IOException(e);
        }
    }
}
