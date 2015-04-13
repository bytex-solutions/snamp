package com.itworks.snamp.adapters.nagios;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.MBeanAttributeInfo;

import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;
import static com.itworks.snamp.adapters.nagios.NagiosAdapterConfigurationDescriptor.*;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents information about attribute in Nagios Plugins format.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NagiosPluginOutput {
    //http://nagios.sourceforge.net/docs/3_0/pluginapi.html
    //https://nagios-plugins.org/doc/guidelines.html#PLUGOUTPUT

    //SERVICE STATUS: VALUE
    private static final String SIMPLE_OUTPUT = "%s %s: %s";
    //'label'=value[UOM];[warn];[crit];[min];[max]
    private static final String EXTENDED_OUTPUT = SIMPLE_OUTPUT + "| %s=%s%s;%s;%s;%s;%s";

    /**
     * Represents service status.
     */
    enum Status{
        OK,
        WARNING,
        CRITICAL,
        UNKNOWN
    }

    private Status status;
    private String serviceName;
    private String labelName;
    private String unitOfMeasurement;
    private NagiosThreshold warnLevel;
    private NagiosThreshold critLevel;
    private String maxValue;
    private String minValue;
    private String value;

    NagiosPluginOutput() {
        status = Status.UNKNOWN;
        serviceName =
                labelName =
                        maxValue =
                                minValue =
                                        value =
                                                unitOfMeasurement = "";
        maxValue = minValue = null;
    }

    void setStatus(final Status value){
        this.status = value;
    }

    void setServiceName(final String value) {
        this.serviceName = nullToEmpty(value);
    }

    void setLabel(final String value) {
        this.labelName = nullToEmpty(value);
    }

    void setUnitOfMeasurement(final String value) {
        this.unitOfMeasurement = nullToEmpty(value);
    }

    boolean checkWarnLevel(final Number value){
        return warnLevel == null || warnLevel.check(value);
    }

    void setWarnLevel(final String value) {
        this.warnLevel = isNullOrEmpty(value) ? null : new NagiosThreshold(value);
    }

    boolean checkCritLevel(final Number value) {
        return critLevel == null || critLevel.check(value);
    }

    void setCritLevel(final String value) {
        this.critLevel = isNullOrEmpty(value) ? null : new NagiosThreshold(value);
    }

    void setMaxValue(String value) {
        this.maxValue = nullToEmpty(value);
    }

    void setMinValue(final String value) {
        this.minValue = nullToEmpty(value);
    }

    void setValue(final Object value) {
        this.value = Objects.toString(value, "null");
    }

    void setMetadata(final MBeanAttributeInfo metadata){
        setServiceName(getServiceName(metadata.getDescriptor(),
                AttributeDescriptor.getAttributeName(metadata)));
        setLabel(getLabel(metadata.getDescriptor(), metadata.getName()));
        setUnitOfMeasurement(getUnitOfMeasurement(metadata.getDescriptor()));
        setWarnLevel(getWarnLevel(metadata.getDescriptor()));
        setCritLevel(getCritLevel(metadata.getDescriptor()));
        setMaxValue(getMaxValue(metadata.getDescriptor()));
        setMinValue(getMinValue(metadata.getDescriptor()));
    }

    /**
     * Constructs a string in Nagios Plugins format.
     * @return A string in Nagios Plugins format.
     */
    public String toString() {
        if (isNullOrEmpty(unitOfMeasurement) &&
                warnLevel == null &&
                critLevel == null &&
                isNullOrEmpty(maxValue) &&
                isNullOrEmpty(minValue))
            return String.format(SIMPLE_OUTPUT, serviceName, status, value);
        else
            return String.format(EXTENDED_OUTPUT, serviceName, status, value,
                    labelName, value, unitOfMeasurement, toString(warnLevel), toString(critLevel), minValue, maxValue);
    }

    private static String toString(final NagiosThreshold threshold){
        return threshold != null ? threshold.getValue() : "";
    }
}
