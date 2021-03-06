package com.bytex.snamp.management.jmx;

import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.core.SnampComponentDescriptor;
import com.bytex.snamp.core.SnampManager;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.OpenMBean;
import com.google.common.collect.Maps;

import javax.management.openmbean.*;
import java.util.Map;
import java.util.Objects;

import static com.bytex.snamp.internal.Utils.staticInit;

/**
 * The type Installed components.
 * @author Evgeniy Kirichenko
 * @version 1.1
 * @since 1.0
 */
final class InstalledComponents extends OpenMBean.OpenAttribute<TabularData, TabularType> {
    private static final String NAME_COLUMN = "Name";
    private static final String DESCRIPTION_COLUMN = "Description";
    private static final String VERSION_COLUMN = "Version";
    private static final String BUNDLE_STATE_COLUMN = "State";
    private static final String IS_CONFIG_DESCR_AVAIL_COLUMN = "IsConfigurationDescriptionAvailable";

    private static final CompositeTypeBuilder INSTALLED_COMPONENT_BUILDER = new CompositeTypeBuilder("com.bytex.snamp.management.SnampComponent","SNAMP component descriptor")
            .addItem(NAME_COLUMN, "Display name of SNAMP component", SimpleType.STRING)
            .addItem(DESCRIPTION_COLUMN, "Description of SNAMP component", SimpleType.STRING)
            .addItem(VERSION_COLUMN, "SNAMP component version", SimpleType.STRING)
            .addItem(BUNDLE_STATE_COLUMN, "State of the component inside of OSGI environment", SimpleType.INTEGER)
            .addItem(IS_CONFIG_DESCR_AVAIL_COLUMN, "SNAMP component provides description of its configuration schema", SimpleType.BOOLEAN);

    private static final CompositeType INSTALLED_COMPONENT = staticInit(INSTALLED_COMPONENT_BUILDER::build);
    private static final TabularType INSTALLED_COMPONENTS_MAP = staticInit(() -> new TabularType("com.bytex.snamp.management.SnampComponents",
            "A set of SNAMP components", INSTALLED_COMPONENT, new String[]{NAME_COLUMN}));

    private final SnampManager manager;

    /**
     * Instantiates a new Installed components.
     *
     * @param manager the manager
     */
    InstalledComponents(final SnampManager manager) {
        super("InstalledComponents", INSTALLED_COMPONENTS_MAP);
        this.manager = Objects.requireNonNull(manager);
    }

    private CompositeData createRow(final SnampComponentDescriptor component) throws OpenDataException {
        final Map<String, Object> row = Maps.newHashMapWithExpectedSize(INSTALLED_COMPONENT.keySet().size());
        row.put(NAME_COLUMN, component.getName(null));
        row.put(DESCRIPTION_COLUMN, component.toString(null));
        row.put(VERSION_COLUMN, Objects.toString(component.getVersion(), "0.0"));
        row.put(BUNDLE_STATE_COLUMN, component.getState());
        row.put(IS_CONFIG_DESCR_AVAIL_COLUMN, false);
        component.invokeSupportService(ConfigurationEntityDescriptionProvider.class, input -> row.put(IS_CONFIG_DESCR_AVAIL_COLUMN, input != null));
        return INSTALLED_COMPONENT_BUILDER.build(row);
    }

    @Override
    public TabularData getValue() throws OpenDataException{
        final TabularData result = new TabularDataSupport(openType);
        for(final SnampComponentDescriptor component: manager.getInstalledGateways())
            result.put(createRow(component));
        for(final SnampComponentDescriptor component: manager.getInstalledResourceConnectors())
            result.put(createRow(component));
        for(final SnampComponentDescriptor component: manager.getInstalledComponents())
            result.put(createRow(component));
        return result;
    }
}
