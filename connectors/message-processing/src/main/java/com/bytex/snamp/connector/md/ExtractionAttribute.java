package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import javax.management.openmbean.OpenType;
import java.io.Serializable;

/**
 * Represents attribute that can extract portion of metric provided by {@link MetricHolderAttribute}.
 */
abstract class ExtractionAttribute<T extends Serializable> extends MessageDrivenAttribute<T> {
    private static final long serialVersionUID = 3652422235984102814L;

    protected ExtractionAttribute(final String name,
                                  final OpenType<T> type,
                                  final String description,
                                  final AttributeDescriptor descriptor) {
        super(name, type, description, AttributeSpecifier.READ_ONLY, descriptor);
    }

    final T getValue(final AttributeSupport support){
        return null;
    }

    @Override
    protected Serializable takeSnapshot() {
        return null;
    }

    @Override
    protected void loadFromSnapshot(Serializable snapshot) {

    }

    @Override
    protected boolean accept(MeasurementNotification notification) {
        return false;
    }

    @Override
    public void close() throws Exception {

    }
}
