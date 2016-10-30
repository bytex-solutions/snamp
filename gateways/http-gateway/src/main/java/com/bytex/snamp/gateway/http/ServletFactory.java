package com.bytex.snamp.gateway.http;

import javax.servlet.Servlet;
import java.util.function.Supplier;

/**
 * Represents {@link javax.servlet.Servlet} factory.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
interface ServletFactory<S extends Servlet> extends Supplier<S> {
}