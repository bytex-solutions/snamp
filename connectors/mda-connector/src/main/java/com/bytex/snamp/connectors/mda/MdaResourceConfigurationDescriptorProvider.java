package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import javax.management.Descriptor;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.List;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MdaResourceConfigurationDescriptorProvider extends ConfigurationEntityDescriptionProviderImpl {
    private static final String TYPE_PARAM = "expectedType";
    private static final String ITEM_NAMES_PARAM = "dictionaryItemNames";
    private static final String ITEM_TYPES_PARAM = "dictionaryItemTypes";
    private static final String TYPE_NAME_PARAM = "dictionaryName";
    private static final String EXPIRE_TIME_PARAM = "expireTime";
    private static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";

    private static final Splitter ITEMS_SPLITTER = Splitter.on(',').trimResults();

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

    public static OpenType<?> parseType(final Descriptor descriptor) throws OpenDataException {
        final String displayName = DescriptorUtils.getField(descriptor, TYPE_PARAM, String.class);
        final WellKnownType result = WellKnownType.parse(displayName);
        if(result == null)
            return null;
        else if(result == WellKnownType.DICTIONARY){
            final String itemNames = DescriptorUtils.getField(descriptor, ITEM_NAMES_PARAM, String.class);
            final String itemTypes = DescriptorUtils.getField(descriptor, ITEM_TYPES_PARAM, String.class);
            final String typeName = DescriptorUtils.getField(descriptor, TYPE_NAME_PARAM, String.class);
            if(Strings.isNullOrEmpty(itemNames) || Strings.isNullOrEmpty(itemTypes) || Strings.isNullOrEmpty(typeName))
                return null;
            else return parseCompositeType(typeName, ITEMS_SPLITTER.splitToList(itemNames), ITEMS_SPLITTER.splitToList(itemTypes));
        }
        else if(result.isPrimitive() || result.isSimpleArray())
            return result.getOpenType();
        else return null;
    }

    public static long parseExpireTime(final Map<String, String> parameters){
        if(parameters.containsKey(EXPIRE_TIME_PARAM))
            return Integer.parseInt(parameters.get(EXPIRE_TIME_PARAM));
        else return Long.MAX_VALUE;
    }

    public static int parseSocketTimeout(final Map<String, String> parameters){
        if(parameters.containsKey(SOCKET_TIMEOUT_PARAM))
            return Integer.parseInt(parameters.get(SOCKET_TIMEOUT_PARAM));
        else return 4000;
    }
}
