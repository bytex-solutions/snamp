package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.connectors.ManagedResourceDescriptionProvider;
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

import com.bytex.snamp.configuration.EntityConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents basic configuration schema for all MDA connectors.
 * @author Roman Sakno
 * @version 1.2
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
        final String displayName = DescriptorUtils.getField(descriptor, TYPE_PARAM, String.class);
        final WellKnownType result = WellKnownType.parse(displayName);
        if(result == null)
            return null;
        else if(result == WellKnownType.DICTIONARY){
            final String itemNames = DescriptorUtils.getField(descriptor, ITEM_NAMES_PARAM, String.class);
            final String itemTypes = DescriptorUtils.getField(descriptor, ITEM_TYPES_PARAM, String.class);
            final String typeName = DescriptorUtils.getField(descriptor, TYPE_NAME_PARAM, String.class);
            if(isNullOrEmpty(itemNames) || isNullOrEmpty(itemTypes) || isNullOrEmpty(typeName))
                return null;
            else return parseCompositeType(typeName, ITEMS_SPLITTER.splitToList(itemNames), ITEMS_SPLITTER.splitToList(itemTypes));
        }
        else if(result.isPrimitive() || result.isSimpleArray())
            return result.getOpenType();
        else return null;
    }

    static long parseExpireTime(final Map<String, String> parameters){
        final long MAX_EXPIRE_TIME = Duration.ofNanos(Long.MAX_VALUE).toMillis();
        if(parameters.containsKey(EXPIRE_TIME_PARAM))
            return Math.min(MAX_EXPIRE_TIME, Long.parseLong(parameters.get(EXPIRE_TIME_PARAM)));
        else return MAX_EXPIRE_TIME;
    }
}
