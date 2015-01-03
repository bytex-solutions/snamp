package com.itworks.snamp.adapters;

import java.util.EventListener;

/**
 * Represents listener for events related to resource adapters.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ResourceAdapterEventListener extends EventListener {
    /**
     * Invokes after resource adapter started.
     * @param e An event object that describes the started resource adapter.
     */
    void adapterStarted(final ResourceAdapterEvent e);

    /**
     * Invokes after resource adapter stopped.
     * @param e An event object that describes resource adapter is stopped.
     */
    void adapterStopped(final ResourceAdapterEvent e);
}
