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

    @JsonProperty("group")
    public final String getGroupName() {
        return groupName;
    }

    public final void setGroupName(final String value) {
        groupName = Objects.requireNonNull(value);
    }

    @JsonProperty("resources")
    public final String[] getResources() {
        return instances.toArray(ArrayUtils.emptyArray(String[].class));
    }

    public final void setResources(final String... value) {
        instances.clear();
        Collections.addAll(instances, value);
    }

    public final void addResource(final String resourceName) {
        instances.add(resourceName);
    }

    @JsonIgnore
    final boolean hasResource(final String resourceName){
        return instances.isEmpty() || instances.contains(resourceName);
    }

    abstract void fillChartData(final String resourceName, final AttributeList attributes, final Consumer<? super AttributeChartData> acceptor);

    @Override
    public final Iterable<? extends AttributeChartData> collectChartData(final BundleContext context) throws Exception {
        final Set<String> resources = isNullOrEmpty(groupName) ?
                instances :
                ManagedResourceConnectorClient.filterBuilder().setGroupName(groupName).getResources(context);
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
