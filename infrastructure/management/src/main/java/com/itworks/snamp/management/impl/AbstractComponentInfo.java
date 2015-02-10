package com.itworks.snamp.management.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.licensing.LicensingDescriptionService;
import com.itworks.snamp.management.AbstractSnampManager;
import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.management.jmx.OpenMBean;

import javax.management.openmbean.*;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract class for SNAMP component info
 *
 * @author Evgeniy Kirichenko
 * @date 09.02.2015
 */
abstract class AbstractComponentInfo extends OpenMBean.OpenOperation<CompositeData, CompositeType>{

    protected static final OpenMBeanParameterInfo LOCALE_PARAM = new OpenMBeanParameterInfoSupport(
            "locale",
            "The expected localization of the configuration schema",
            SimpleType.STRING);

    private static final CompositeType COMPONENT_CONFIG_SCHEMA;
    private static final TabularType LIMITATION_SCHEMA;

    static{
        try {
            //LIMITATION_SCHEMA
            LIMITATION_SCHEMA = new TabularType("com.itworks.management.LimitationSchemeType",
                    "Snamp component limitation schema",
                    new CompositeType("com.itworks.management.LimitationTableScheme",
                            "Snamp component limitation schema description",
                            new String[]{"limitation", "description"},
                            new String[]{"Limitation name", "Limitation descriptor"},
                            new OpenType<?>[]{SimpleType.STRING, SimpleType.STRING}),
                    new String[]{"limitation"}
            );
            //COMPONENT_CONFIG_SCHEMA
            COMPONENT_CONFIG_SCHEMA = new CompositeType("com.itworks.management.ComponentConfig",
                    "SNAMP Component Configuration Schema",
                    new String[]{
                            "Version",
                            "State",
                            "DisplayName",
                            "Description",
                            "Licensing"},
                    new String[]{
                            "Version of the SNAMP component",
                            "The state of the SNAMP component (" +
                                "org.osgi.framework.Bundle#ACTIVE=32, org.osgi.framework.Bundle#INSTALLED=2" +
                                ", org.osgi.framework.Bundle#UNINSTALLED=1, org.osgi.framework.Bundle#RESOLVED=4" +
                                ", org.osgi.framework.Bundle#STARTING=8, org.osgi.framework.Bundle#STOPPING=16",
                            "Snamp component name to be displayed",
                            "The short description of the SNAMP component",
                            "Licensing limitation of the SNAMP component"},
                    new OpenType<?>[]{
                            SimpleType.STRING,
                            SimpleType.INTEGER,
                            SimpleType.STRING,
                            SimpleType.STRING,
                            LIMITATION_SCHEMA
                    }
            );
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    protected final AbstractSnampManager snampManager;

    protected AbstractComponentInfo(final AbstractSnampManager snampManager,
                                    final String operationName,
                                    final OpenMBeanParameterInfo... parameters) {
        super(operationName, COMPONENT_CONFIG_SCHEMA, parameters);
        this.snampManager = Objects.requireNonNull(snampManager);
    }

    protected static CompositeData getSnampComponentInfo(final SnampComponentDescriptor component, final Locale loc) throws OpenDataException {

        final TabularDataSupport tabularDataSupport = new TabularDataSupport(LIMITATION_SCHEMA);
        component.invokeSupportService(LicensingDescriptionService.class, new SafeConsumer<LicensingDescriptionService>() {
            @Override
            public void accept(final LicensingDescriptionService input) {
                for (final String limitation : input.getLimitations()) {
                    try {
                        tabularDataSupport.put(new CompositeDataSupport(tabularDataSupport.getTabularType().getRowType(),
                                ImmutableMap.<String, Object>of(
                                        "limitation", limitation,
                                        "description", input.getDescription(limitation, loc))));
                    } catch (OpenDataException e) {
                        // @TODO what do
                    }
                }
            }
        });

        final Map<String, Object> schema = Maps.newHashMapWithExpectedSize(COMPONENT_CONFIG_SCHEMA.keySet().size());
        schema.put("Version", Objects.toString(component.getVersion(), "0.0"));
        schema.put("State", component.getState());
        schema.put("DisplayName", component.getName(loc));
        schema.put("Description", component.getDescription(loc));
        schema.put("Licensing", tabularDataSupport);
        return new CompositeDataSupport(COMPONENT_CONFIG_SCHEMA, schema);
    }
}
