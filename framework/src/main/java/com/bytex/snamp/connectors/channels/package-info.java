/**
 * Provides channel abstraction in the management information model.
 * Channel is a stream of data that can be received from managed resource
 * or pushed to managed resource.
 * For example, we have web camera and it is connected as managed resource to SNAMP.
 * The video stream can be captured using {@link java.nio.channels.AsynchronousByteChannel}.
 * @see java.nio.channels.AsynchronousByteChannel
 * @see com.bytex.snamp.connectors.channels.DataChannel
 */
package com.bytex.snamp.connectors.channels;