package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.osgi.framework.BundleContext;

import javax.management.AttributeList;
import java.util.*;
import java.util.function.Consumer;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ChartOfAttributeValues extends AbstractChart {
    private final Set<String> instances; //empty for all instances
    private String groupName;

    public ChartOfAttributeValues() {
        instances = new HashSet<>();
        groupName = "";
    }

    @JsonProperty("component")
    public final String getGroupName() {
        return groupName;
    }

    public final void setGroupName(final String value) {
        groupName = Objects.requireNonNull(value);
    }

    @JsonProperty("instances")
    public final String[] getInstances() {
        return instances.toArray(ArrayUtils.emptyArray(String[].class));
    }

    public final void setInstances(final String... value) {
        instances.clear();
        Collections.addAll(instances, value);
    }

    public final void addInstance(final String instance) {
        instances.add(instance);
    }

    @JsonIgnore
    final boolean hasInstance(final String instanceName){
        return instances.isEmpty() || instances.contains(instanceName);
    }

    abstract void fillChartData(final String resourceName, final AttributeList attributes, final Consumer<? super AttributeChartData> acceptor);

    @Override
    public final Iterable<? extends AttributeChartData> collectChartData(final BundleContext context) throws Exception {
        final Set<String> resources = isNullOrEmpty(groupName) ? instances : ManagedResourceConnectorClient.filterBuilder().getResources(context);
        final List<AttributeChartData> result = new LinkedList<>();
        for (final String resourceName : resources) {
            final Optional<ManagedResourceConnectorClient> clientRef = ManagedResourceConnectorClient.tryCreate(context, resourceName);
            if (clientRef.isPresent())
                try (final ManagedResourceConnectorClient client = clientRef.get()) {
                    fillChartData(resourceName, client.getAttributes(), result::add);
                }
        }
        return result;
    }
}
