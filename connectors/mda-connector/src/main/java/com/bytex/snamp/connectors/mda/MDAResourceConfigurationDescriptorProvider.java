package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.core.ServiceSpinWait;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.hazelcast.core.HazelcastInstance;
import org.osgi.framework.BundleContext;

import javax.management.Descriptor;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MDAResourceConfigurationDescriptorProvider extends ConfigurationEntityDescriptionProviderImpl {
    private static final Splitter ITEMS_SPLITTER = Splitter.on(',').trimResults();
    private static final String TYPE_PARAM = "expectedType";
    private static final String ITEM_NAMES_PARAM = "dictionaryItemNames";
    private static final String ITEM_TYPES_PARAM = "dictionaryItemTypes";
    private static final String TYPE_NAME_PARAM = "dictionaryName";
    private static final String EXPIRE_TIME_PARAM = "expirationTime";
    private static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";
    private static final String WAIT_FOR_HZ_PARAM = "waitForHazelcast";

    private static final class AttributeConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "MdaAttributeConfig";

        private AttributeConfigurationDescriptor(){
            super(RESOURCE_NAME, AttributeConfiguration.class, TYPE_PARAM, ITEM_NAMES_PARAM, ITEM_TYPES_PARAM, TYPE_NAME_PARAM);
        }
    }

    private static final class EventConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "MdaEventConfig";

        private EventConfigurationDescriptor(){
            super(RESOURCE_NAME, EventConfiguration.class, TYPE_PARAM, ITEM_NAMES_PARAM, ITEM_TYPES_PARAM, TYPE_NAME_PARAM);
        }
    }

    private static final class ConnectorConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private static final String RESOURCE_NAME = "MdaConnectorConfig";

        private ConnectorConfigurationDescriptor(){
            super(RESOURCE_NAME, ManagedResourceConfiguration.class, WAIT_FOR_HZ_PARAM, SOCKET_TIMEOUT_PARAM, EXPIRE_TIME_PARAM);
        }
    }

    MDAResourceConfigurationDescriptorProvider(){
        super(new AttributeConfigurationDescriptor(), new EventConfigurationDescriptor(), new ConnectorConfigurationDescriptor());
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

    static boolean waitForHazelcast(final Map<String, String> parameters, final BundleContext context) throws TimeoutException, InterruptedException {
        if(parameters.containsKey(WAIT_FOR_HZ_PARAM)){
            final long timeout = Long.parseLong(parameters.get(WAIT_FOR_HZ_PARAM));
            final ServiceSpinWait<HazelcastInstance> hazelcastWait = new ServiceSpinWait<>(context, HazelcastInstance.class);
            try {
                return hazelcastWait.get(timeout, TimeUnit.MILLISECONDS) != null;
            } catch (final ExecutionException ignored) {
                return false;
            }
        }
        else return false;
    }
}
