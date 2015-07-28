package com.bytex.snamp.management.impl;

import com.google.common.collect.ImmutableMap;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.OpenMBean;
import com.bytex.snamp.management.AbstractSnampManager;
import com.bytex.snamp.management.SnampComponentDescriptor;

import javax.management.MBeanOperationInfo;
import javax.management.openmbean.*;
import java.util.Locale;
import java.util.Objects;

/**
 * Abstract class for SNAMP component info
 *
 * @author Evgeniy Kirichenko
 */
abstract class AbstractComponentInfo extends OpenMBean.OpenOperation<CompositeData, CompositeType> implements CommonOpenTypesSupport<MBeanOperationInfo> {

    private static final CompositeType COMPONENT_CONFIG_SCHEMA;

    private static final CompositeTypeBuilder MAIN_TYPE;

    private static final String STATE_DESCRIPTION = "The state of the SNAMP component (" +
            "org.osgi.framework.Bundle#ACTIVE=32, " +
            "org.osgi.framework.Bundle#INSTALLED=2, " +
            "org.osgi.framework.Bundle#UNINSTALLED=1, " +
            "org.osgi.framework.Bundle#RESOLVED=4, " +
            "org.osgi.framework.Bundle#STARTING=8, " +
            "org.osgi.framework.Bundle#STOPPING=16" +
        ")";

    static {
        try {
            MAIN_TYPE = new CompositeTypeBuilder("com.bytex.management.ComponentConfig", "SNAMP Component Configuration Schema")
                    .addItem("Version", "Version of the SNAMP component", SimpleType.STRING)
                    .addItem("State", STATE_DESCRIPTION, SimpleType.INTEGER)
                    .addItem("DisplayName", "Snamp component name to be displayed", SimpleType.STRING)
                    .addItem("Description", "The short description of the SNAMP component", SimpleType.STRING);

            COMPONENT_CONFIG_SCHEMA = MAIN_TYPE.build();

        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * The Snamp manager.
     */
    protected final AbstractSnampManager snampManager;

    /**
     * Instantiates a new Abstract component info.
     *
     * @param snampManager the snamp manager
     * @param operationName the operation name
     * @param parameters the parameters
     */
    protected AbstractComponentInfo(final AbstractSnampManager snampManager,
                                    final String operationName,
                                    final OpenMBeanParameterInfo... parameters) {
        super(operationName, COMPONENT_CONFIG_SCHEMA, parameters);
        this.snampManager = Objects.requireNonNull(snampManager);
    }

    /**
     * Gets snamp component info.
     *
     * @param component the component
     * @param loc the loc
     * @return the snamp component info
     * @throws OpenDataException the open data exception
     */
    protected static CompositeData getSnampComponentInfo(final SnampComponentDescriptor component, final Locale loc) throws OpenDataException {
        return MAIN_TYPE.build(
            ImmutableMap.of(
                "Version", Objects.toString(component.getVersion(), "0.0"),
                "State", component.getState(),
                "DisplayName", component.getName(loc),
                "Description", component.getDescription(loc)
            )
        );
    }
}
