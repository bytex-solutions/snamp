package com.bytex.snamp.instrumentation.reporters.http;

import com.bytex.snamp.instrumentation.reporters.LazyReporter;

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

    @Override
    protected HttpReporter createReporter() {
        return null;
    }
}
