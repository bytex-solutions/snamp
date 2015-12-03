package com.bytex.snamp.adapters.decanter;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.adapters.modeling.ModelOfAttributes;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import org.osgi.service.event.EventAdmin;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents polled collector used to harvest attributes.
 */
final class PolledCollector extends ModelOfAttributes<DecanterAttributeAccessor> implements Runnable, DecanterCollector {
    private final EventAdmin eventAdmin;
    private final Logger logger;

    PolledCollector(final EventAdmin admin,
                    final Logger logger){
        this.eventAdmin = Objects.requireNonNull(admin);
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    protected DecanterAttributeAccessor createAccessor(final MBeanAttributeInfo metadata) {
        return new DecanterAttributeAccessor(metadata);
    }

    @Override
    public void run() {
        //do not send events in the passive node
        if (DistributedServices.isActiveNode(Utils.getBundleContextOfObject(this)))
            try {
                forEachAttribute(new EntryReader<String, DecanterAttributeAccessor, JMException>() {
                    @Override
                    public boolean read(final String resourceName, final DecanterAttributeAccessor accessor) throws JMException {
                        accessor.collectData(eventAdmin, TOPIC_PREFIX + resourceName + '/');
                        return true;
                    }
                });
            } catch (final JMException e) {
                logger.log(Level.SEVERE, "Unable to collect attributes", e);
            }
    }
}
