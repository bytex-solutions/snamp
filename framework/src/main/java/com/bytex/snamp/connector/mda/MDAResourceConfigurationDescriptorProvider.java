package com.bytex.snamp.connector.mda;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.base.Splitter;
import com.google.common.collect.ObjectArrays;

import javax.management.Descriptor;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.bytex.snamp.MapUtils.getValueAsLong;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents basic configuration schema for all MDA connector.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class MDAResourceConfigurationDescriptorProvider extends ConfigurationEntityDescriptionProviderImpl implements ManagedResourceDescriptionProvider {
    private static final Splitter ITEMS_SPLITTER = Splitter.on(',').trimResults();
    /**
     * Represents configuration parameter which describes the type of the attribute.
     */
    protected static final String TYPE_PARAM = "expectedType";
    protected static final String ITEM_NAMES_PARAM = "dictionaryItemNames";
    protected static final String ITEM_TYPES_PARAM = "dictionaryItemTypes";
    protected static final String TYPE_NAME_PARAM = "dictionaryName";
    /**
     * Represents configuration parameter which describes interval of trust for attribute values.
     */
    protected static final String EXPIRE_TIME_PARAM = "expirationTime";

    protected static abstract class AttributeConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        protected AttributeConfigurationDescriptor(final String resourceName, final String... parameters){
            super(resourceName, AttributeConfiguration.class,
                    ObjectArrays.concat(new String[]{TYPE_PARAM, ITEM_NAMES_PARAM, ITEM_TYPES_PARAM, TYPE_NAME_PARAM}, parameters, String.class));
        }
    }

    protected static abstract class EventConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        protected EventConfigurationDescriptor(final String resourceName, final String... parameters){
            super(resourceName, EventConfiguration.class, ObjectArrays.concat(new String[]{TYPE_PARAM, ITEM_NAMES_PARAM, ITEM_TYPES_PARAM, TYPE_NAME_PARAM}, parameters, String.class));
        }
    }

    protected static abstract class ConnectorConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        protected ConnectorConfigurationDescriptor(final String resourceName, final String... parameters){
            super(resourceName, ManagedResourceConfiguration.class, ObjectArrays.concat(EXPIRE_TIME_PARAM, parameters));
        }
    }

    private static CompositeType parseCompositeType(final String typeName,
                                                    final List<String> itemNames,
                                                    final List<String> itemTypes) throws OpenDataException {
        final CompositeTypeBuilder builder = new CompositeTypeBuilder(typeName, typeName);
        for(int i = 0; i < itemNames.size(); i++){
            final WellKnownType itemType = WellKnownType.parse(itemTypes.get(i));
            if(itemType != null && (itemType.isPrimitive() || itemType.isSimpleArray())){
                final String itemName = itemNames.get(i);
                builder.addItem(itemName, itemName, itemType.getOpenType());
            }
        }
        return builder.build();
    }

    @SafeVarargs
    protected MDAResourceConfigurationDescriptorProvider(final ConfigurationEntityDescription<? extends EntityConfiguration>... descriptions) {
        super(descriptions);
    }

    static OpenType<?> parseType(final Descriptor descriptor) throws OpenDataException {
        final String displayName = DescriptorUtils.getField(descriptor, TYPE_PARAM, Objects::toString, () -> "");
        final WellKnownType result = WellKnownType.parse(displayName);
        if(result == null)
            return null;
        else if(result == WellKnownType.DICTIONARY){
            final String itemNames = DescriptorUtils.getField(descriptor, ITEM_NAMES_PARAM, Objects::toString, () -> "");
            final String itemTypes = DescriptorUtils.getField(descriptor, ITEM_TYPES_PARAM, Objects::toString, () -> "");
            final String typeName = DescriptorUtils.getField(descriptor, TYPE_NAME_PARAM, Objects::toString, () -> "");
            if(isNullOrEmpty(itemNames) || isNullOrEmpty(itemTypes) || isNullOrEmpty(typeName))
                return null;
            else return parseCompositeType(typeName, ITEMS_SPLITTER.splitToList(itemNames), ITEMS_SPLITTER.splitToList(itemTypes));
        }
        else if(result.isPrimitive() || result.isSimpleArray())
            return result.getOpenType();
        else return null;
    }

    static long parseExpireTime(final Map<String, String> parameters) {
        return getValueAsLong(parameters, EXPIRE_TIME_PARAM, Long::parseLong, Duration.ofNanos(Long.MAX_VALUE)::toMillis);
    }
}
