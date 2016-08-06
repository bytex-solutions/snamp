package com.bytex.snamp.adapters.nagios;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.MBeanAttributeInfo;
import java.util.Objects;

import static com.bytex.snamp.adapters.nagios.NagiosAdapterConfigurationDescriptor.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents information about attribute in Nagios Plugins format.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class NagiosPluginOutput {
    //http://nagios.sourceforge.net/docs/3_0/pluginapi.html
    //https://nagios-plugins.org/doc/guidelines.html#PLUGOUTPUT

    //SERVICE STATUS: VALUE[UOM]
    private static final String SIMPLE_OUTPUT = "%s %s: %s%s";
    //'label'=value[UOM];[warn];[crit];[min];[max]
    private static final String EXTENDED_OUTPUT = SIMPLE_OUTPUT + " | %s=%s%s;%s;%s;%s;%s";

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
    private NagiosThreshold warnThreshold;
    private NagiosThreshold critThreshold;
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

    boolean checkWarnThreshold(final Number value){
        return warnThreshold == null || warnThreshold.check(value);
    }

    void setWarnThreshold(final String value) {
        this.warnThreshold = isNullOrEmpty(value) ? null : new NagiosThreshold(value);
    }

    boolean checkCritThreshold(final Number value) {
        return critThreshold == null || critThreshold.check(value);
    }

    void setCritThreshold(final String value) {
        this.critThreshold = isNullOrEmpty(value) ? null : new NagiosThreshold(value);
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

    void setMessage(final String value){
        this.value = value;
    }

    void setMetadata(final MBeanAttributeInfo metadata){
        setServiceName(getServiceName(metadata.getDescriptor(), AttributeDescriptor.getName(metadata)));
        setLabel(getLabel(metadata.getDescriptor(), metadata.getName()));
        setUnitOfMeasurement(getUnitOfMeasurement(metadata.getDescriptor()));
        setWarnThreshold(getWarnThreshold(metadata.getDescriptor()));
        setCritThreshold(getCritThreshold(metadata.getDescriptor()));
        setMaxValue(getMaxValue(metadata.getDescriptor()));
        setMinValue(getMinValue(metadata.getDescriptor()));
    }

    /**
     * Constructs a string in Nagios Plugins format.
     * @return A string in Nagios Plugins format.
     */
    public String toString() {
        if (warnThreshold == null &&
                critThreshold == null &&
                isNullOrEmpty(maxValue) &&
                isNullOrEmpty(minValue))
            return String.format(SIMPLE_OUTPUT, serviceName, status, value, unitOfMeasurement);
        else
            return String.format(EXTENDED_OUTPUT, serviceName, status, value, unitOfMeasurement,
                    labelName, value, unitOfMeasurement, toString(warnThreshold), toString(critThreshold), minValue, maxValue);
    }

    private static String toString(final NagiosThreshold threshold){
        return threshold != null ? threshold.getValue() : "";
    }
}
