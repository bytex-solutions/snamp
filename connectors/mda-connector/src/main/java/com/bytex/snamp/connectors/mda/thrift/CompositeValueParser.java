package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.jmx.DefaultValues;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.collect.Maps;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.protocol.TType;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import java.util.Arrays;
import java.util.Map;

/**
 * Represents serializer/deserializer of CompositeData.
 * This class cannot be inherited.
 * <p>
 *     CompositeData will be serialized into a set of fields according with 
 */
final class CompositeValueParser implements ThriftValueParser {
    private final CompositeData defaultValue;
    private final String[] sortedItems;
    private final TStruct struct;

    CompositeValueParser(final CompositeType type) throws OpenDataException {
        defaultValue = DefaultValues.get(type);
        sortedItems = ArrayUtils.toArray(type.keySet(), String.class);
        Arrays.sort(sortedItems);
        this.struct = new TStruct(type.getTypeName());
    }

    @Override
    public CompositeData getDefaultValue() {
        return defaultValue;
    }

    private void serialize(final CompositeData input, final TProtocol output) throws TException{
        short index = 0;
        output.writeStructBegin(struct);
        for(final String itemName: sortedItems){
            final WellKnownType itemType = WellKnownType.getItemType(input.getCompositeType(), itemName);
            SimpleValueParser.serialize(input.get(itemName), itemType, output, index++, itemName);
        }
        output.writeFieldStop();
        output.writeStructEnd();
    }

    @Override
    public void serialize(final Object input, final TProtocol output) throws TException {
        if(input instanceof CompositeData)
            serialize(((CompositeData)input), output);
    }

    @Override
    public CompositeData deserialize(final TProtocol input) throws TException {
        final Map<String, Object> items = Maps.newHashMapWithExpectedSize(sortedItems.length);
        input.readStructBegin();
        while (true){
            final TField field = input.readFieldBegin();
            if(field.type == TType.STOP) break;
            final String itemName = sortedItems[field.id];
            final WellKnownType itemType = WellKnownType.getItemType(defaultValue.getCompositeType(), itemName);
            items.put(itemName, SimpleValueParser.deserializeNaked(input, itemType));
            input.readFieldEnd();
        }
        input.readStructEnd();

        try {
            return new CompositeDataSupport(defaultValue.getCompositeType(), items);
        } catch (final OpenDataException e) {
            throw new TException(e);
        }
    }
}
