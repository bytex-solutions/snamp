package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.connectors.ResourceEventListener;
import com.bytex.snamp.connectors.mda.DataAcceptor;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;

import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ThriftDataAcceptor extends AbstractManagedResourceConnector implements DataAcceptor, TProcessor {
    @Override
    public boolean addAttribute(final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
        return false;
    }

    @Override
    public void removeAttributesExcept(final Set<String> attributes) {

    }

    @Override
    public boolean enableNotifications(final String listId, final String category, final CompositeData options) {
        return false;
    }

    @Override
    public void disableNotificationsExcept(final Set<String> notifications) {

    }

    @Override
    public void beginAccept(final Object... dependencies) throws IOException {

    }

    /**
     * Adds a new listener for the connector-related events.
     * <p/>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {

    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {

    }

    @Override
    public boolean process(final TProtocol in, final TProtocol out) throws TException {
        return false;
    }
}
