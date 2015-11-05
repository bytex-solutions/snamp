package com.bytex.snamp.connectors.mda.impl.thrift;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.jmx.DefaultValues;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.collect.ImmutableSortedMap;
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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Represents serializer/deserializer of CompositeData.
 * This class cannot be inherited.
 * <p>
 *     CompositeData will be serialized into a set of fields according with 
 */
final class CompositeValueParser implements ThriftValueParser {
    private final CompositeData defaultValue;
    private final String[] sortedNames;
    private final ImmutableSortedMap<String, ? extends ThriftValueParser> sortedItems;
    private final TStruct struct;

    CompositeValueParser(final CompositeType type) throws OpenDataException {
        defaultValue = DefaultValues.get(type);
        this.struct = new TStruct(type.getTypeName());
        final SortedMap<String, SimpleValueParser> parsers = new TreeMap<>();
        for(final String itemName: type.keySet()){
            final WellKnownType itemType = WellKnownType.getType(type.getType(itemName));
            if(itemType == null || !itemType.isOpenType())
                throw new OpenDataException("Unsupported type of item ".concat(itemName));
            parsers.put(itemName, new SimpleValueParser(itemType));
        }
        sortedNames = ArrayUtils.toArray(parsers.keySet(), String.class);
        sortedItems = ImmutableSortedMap.copyOfSorted(parsers);
    }

    @Override
    public CompositeData getDefaultValue() {
        return defaultValue;
    }

    private void serialize(final CompositeData input, final TProtocol output) throws TException{
        short index = 1;
        output.writeStructBegin(struct);
        for(final Map.Entry<String, ? extends ThriftValueParser> itemDef: sortedItems.entrySet()){
            output.writeFieldBegin(new TField(itemDef.getKey(), itemDef.getValue().getType(), index++));
            itemDef.getValue().serialize(input.get(itemDef.getKey()), output);
            output.writeFieldEnd();
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
        final Map<String, Object> items = Maps.newHashMapWithExpectedSize(sortedItems.size());
        input.readStructBegin();
        while (true){
            final TField field = input.readFieldBegin();
            if(field.type == TType.STOP) break;
            final String itemName = sortedNames[field.id - 1];
            items.put(itemName, sortedItems.get(itemName).deserialize(input));
            input.readFieldEnd();
        }
        input.readStructEnd();

        try {
            return new CompositeDataSupport(defaultValue.getCompositeType(), items);
        } catch (final OpenDataException e) {
            throw new TException(e);
        }
    }

    @Override
    public byte getType() {
        return TType.STRUCT;
    }
}
