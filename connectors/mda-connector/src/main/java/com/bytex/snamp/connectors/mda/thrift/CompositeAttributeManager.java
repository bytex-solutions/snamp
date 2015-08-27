package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.jmx.DefaultValues;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Represents serializer/deserializer of CompositeData.
 * This class cannot be inherited.
 * <p>
 *     CompositeData will be serialized into a set of fields according with 
 */
final class CompositeAttributeManager extends ThriftAttributeManager {
    private final CompositeData defaultValue;
    private final SortedSet<String> sortedItems;

    CompositeAttributeManager(final CompositeType type, final String slotName) throws OpenDataException {
        super(type, slotName);
        defaultValue = DefaultValues.get(type);
        sortedItems = new TreeSet<>(type.keySet());
    }

    @Override
    CompositeData getDefaultValue() {
        return defaultValue;
    }

    private void serialize(final CompositeData input, final TProtocol output) throws TException{
        for(final String itemName: sortedItems){
            final WellKnownType itemType = WellKnownType.getItemType(input.getCompositeType(), itemName);
            SimpleAttributeManager.serialize(input.get(itemName), itemType, output);
        }
    }

    @Override
    protected void serialize(final Object input, final TProtocol output) throws TException {
        if(input instanceof CompositeData)
            serialize(((CompositeData)input), output);
    }

    @Override
    protected CompositeData deserialize(final TProtocol input) throws TException {
        final Map<String, Object> items = Maps.newHashMapWithExpectedSize(sortedItems.size());
        for(final String itemName: sortedItems){
            final WellKnownType itemType = WellKnownType.getItemType(defaultValue.getCompositeType(), itemName);
            items.put(itemName, SimpleAttributeManager.deserialize(input, itemType));
        }
        try {
            return new CompositeDataSupport(defaultValue.getCompositeType(), items);
        } catch (final OpenDataException e) {
            throw new TException(e);
        }
    }
}
