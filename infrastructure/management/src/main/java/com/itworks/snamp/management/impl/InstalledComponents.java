package com.itworks.snamp.management.impl;

import com.google.common.collect.Maps;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.licensing.LicensingDescriptionService;
import com.itworks.snamp.management.Maintainable;
import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.management.SnampManager;
import com.itworks.snamp.management.jmx.OpenMBean;

import javax.management.openmbean.*;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

/**
* @author Roman Sakno
* @version 1.0
* @since 1.0
*/
final class InstalledComponents extends OpenMBean.OpenAttribute<TabularData, TabularType> {
    private static final String NAME_COLUMN = "Name";
    private static final String DESCRIPTION_COLUMN = "Description";
    private static final String VERSION_COLUMN = "Version";
    private static final String BUNDLE_STATE_COLUMN = "State";
    private static final String IS_LICENSED_COLUMN = "IsCommerciallyLicensed";
    private static final String IS_MANAGEABLE_COLUMN = "IsManageable";
    private static final String IS_CONFIG_DESCR_AVAIL_COLUMN = "IsConfigurationDescriptionAvailable";
    private static final String[] COLUMNS = new String[]{
        NAME_COLUMN,
        DESCRIPTION_COLUMN,
        VERSION_COLUMN,
        BUNDLE_STATE_COLUMN,
        IS_LICENSED_COLUMN,
        IS_MANAGEABLE_COLUMN,
        IS_CONFIG_DESCR_AVAIL_COLUMN,
    };
    private static final String[] DESCRIPTIONS = new String[]{
        "Display name of SNAMP component",
        "Description of SNAMP component",
        "SNAMP component version",
        "State of the component inside of OSGI environment.",
        "SNAMP component is commercially licensed",
        "SNAMP component supports command-line interaction",
        "SNAMP component provides description of its configuration schema"
    };
    private static final OpenType<?>[] COLUMN_TYPES = new OpenType<?>[]{
        SimpleType.STRING,
        SimpleType.STRING,
        SimpleType.STRING,
        SimpleType.INTEGER,
        SimpleType.BOOLEAN,
        SimpleType.BOOLEAN,
        SimpleType.BOOLEAN
    };

    private static TabularType createTabularType() throws OpenDataException {
        final CompositeType rowType = new CompositeType("com.itworks.snamp.management.SnampComponent",
                "SNAMP component descriptor",
                COLUMNS,
                DESCRIPTIONS,
                COLUMN_TYPES);
        return new TabularType("com.itworks.snamp.management.SnampComponents",
                "A set of SNAMP components", rowType, new String[]{NAME_COLUMN});
    }

    private final SnampManager manager;

    InstalledComponents(final SnampManager manager) throws OpenDataException{
        super("InstalledComponents", createTabularType());
        this.manager = Objects.requireNonNull(manager);
    }

    private CompositeData createRow(final SnampComponentDescriptor component) throws OpenDataException{
        final Map<String, Object> row = Maps.newHashMapWithExpectedSize(COLUMNS.length);
        row.put(NAME_COLUMN, component.getName(null));
        row.put(DESCRIPTION_COLUMN, component.getDescription(null));
        row.put(VERSION_COLUMN, Objects.toString(component.getVersion(), "0.0"));
        row.put(BUNDLE_STATE_COLUMN, component.getState());
        row.put(IS_MANAGEABLE_COLUMN, false);
        row.put(IS_LICENSED_COLUMN, false);
        row.put(IS_CONFIG_DESCR_AVAIL_COLUMN, false);
        try {
            component.invokeSupportService(Maintainable.class, new SafeConsumer<Maintainable>() {
                @Override
                public void accept(final Maintainable input) {
                    row.put(IS_MANAGEABLE_COLUMN, input != null);
                }
            });
            component.invokeSupportService(ConfigurationEntityDescriptionProvider.class, new SafeConsumer<ConfigurationEntityDescriptionProvider>() {
                @Override
                public void accept(final ConfigurationEntityDescriptionProvider input) {
                    row.put(IS_CONFIG_DESCR_AVAIL_COLUMN, input != null);
                }
            });
            component.invokeSupportService(LicensingDescriptionService.class, new SafeConsumer<LicensingDescriptionService>() {
                @Override
                public void accept(final LicensingDescriptionService input) {
                    row.put(IS_LICENSED_COLUMN, input != null);
                }
            });
        }
        catch (final Exception e){
            MonitoringUtils.getLogger().log(Level.WARNING, e.getLocalizedMessage(), e);
        }
        return new CompositeDataSupport(openType.getRowType(), row);
    }

    @Override
    public TabularData getValue() throws OpenDataException{
        final TabularData result = new TabularDataSupport(openType);
        for(final SnampComponentDescriptor component: manager.getInstalledResourceAdapters())
            result.put(createRow(component));
        for(final SnampComponentDescriptor component: manager.getInstalledResourceConnectors())
            result.put(createRow(component));
        for(final SnampComponentDescriptor component: manager.getInstalledComponents())
            result.put(createRow(component));
        return result;
    }
}
