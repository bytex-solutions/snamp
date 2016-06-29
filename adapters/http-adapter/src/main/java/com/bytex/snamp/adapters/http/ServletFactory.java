package com.bytex.snamp.adapters.http;

import javax.servlet.Servlet;
import java.util.function.Supplier;

/**
 * Represents {@link javax.servlet.Servlet} factory.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
interface ServletFactory<S extends Servlet> extends Supplier<S> {
}
