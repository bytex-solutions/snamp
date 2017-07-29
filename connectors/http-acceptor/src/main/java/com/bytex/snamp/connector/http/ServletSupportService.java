package com.bytex.snamp.connector.http;

import com.bytex.snamp.core.SupportService;

import javax.servlet.Servlet;

/**
 * Represents bridge between {@link Servlet} and {@link SupportService} interfaces.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
interface ServletSupportService extends SupportService, Servlet {
}
