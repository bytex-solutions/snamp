package com.itworks.snamp.connectors.attributes;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents default implementation of {@link com.itworks.snamp.connectors.attributes.AttributeSupportReporter} interface.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class AttributeSupportReporterImpl implements AttributeSupportReporter {
    private final Logger logger;

    public AttributeSupportReporterImpl(final Logger connectorLogger){
        this.logger = Objects.requireNonNull(connectorLogger, "connectorLogger is null.");
    }

    protected String getReportTemplateForGetAttributeFailure(){
        return "Unable to read attribute %s";
    }

    protected Level getLevelForGetAttributeFailure(){
        return Level.WARNING;
    }

    /**
     * Reports about attribute read failure.
     *
     * @param attributeID The identifier of the attribute.
     * @param reason      The reason of the failure.
     * @see com.itworks.snamp.connectors.attributes.AttributeSupport#getAttribute(String, com.itworks.snamp.TimeSpan, Object)
     */
    @Override
    public final void unableToGetAttribute(final String attributeID, final Exception reason) {
        logger.log(getLevelForGetAttributeFailure(), String.format(getReportTemplateForGetAttributeFailure(), attributeID), reason);
    }
}
