package com.itworks.snamp.adapters;

import java.util.EventListener;

/**
 * Represents listener for events related to resource adapters.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see com.itworks.snamp.concurrent.AsyncEventListener
 * @see ResourceAdapterClient#addEventListener(String, ResourceAdapterEventListener)
 * @see ResourceAdapterClient#removeEventListener(String, ResourceAdapterEventListener)
 */
public interface ResourceAdapterEventListener extends EventListener {
    /**
     * Invokes after resource adapter started.
     * @param e An event object that describes the started resource adapter.
     */
    void handle(final ResourceAdapterEvent e);
}
