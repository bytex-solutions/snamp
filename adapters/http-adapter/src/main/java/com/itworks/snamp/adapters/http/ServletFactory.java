package com.itworks.snamp.adapters.http;

import com.google.common.base.Supplier;

import javax.servlet.Servlet;

/**
 * Represents {@link javax.servlet.Servlet} factory.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface ServletFactory<S extends Servlet> extends Supplier<S> {
}
