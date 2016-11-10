package com.bytex.snamp.tracer;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Represents abstract POJO for all measurements.
 * @since 1.0
 * @version 1.0
 * @author Roman Sakno
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({@JsonSubTypes.Type(IntegerMeasurement.class)})
public abstract class Measurement {
    private String instanceName;
    private String componentName;
    private String message;

    Measurement(){
        instanceName = componentName = message = "";
    }

    /**
     * Gets default instance name based on IP address.
     * @return Instance name based on IP address.
     */
    public static String getDefaultInstanceName() {
        final String LOCALHOST = "127.0.0.1";
        final Enumeration<NetworkInterface> ifaces;
        try {
            ifaces = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException e) {
            return LOCALHOST;
        }
        final TreeSet<String> siteLocalCandidates = new TreeSet<String>();
        final TreeSet<String> candidates = new TreeSet<String>();
        for (NetworkInterface iface; ifaces.hasMoreElements(); ) {
            iface = ifaces.nextElement();
            for (final Enumeration<InetAddress> addrs = iface.getInetAddresses(); addrs.hasMoreElements(); ) {
                final InetAddress addr = addrs.nextElement();
                if (!addr.isLoopbackAddress())
                    (addr.isSiteLocalAddress() ? siteLocalCandidates : candidates).add(addr.getHostAddress());
            }
        }
        if (!siteLocalCandidates.isEmpty())
            return siteLocalCandidates.first();
        else if (!candidates.isEmpty())
            return candidates.first();
        else
            return LOCALHOST;
    }

    public static String getDefaultComponentName(){
        String cmdLine = System.getProperty("sun.java.command");
        if(cmdLine != null && !cmdLine.isEmpty()) {
            final String fullClassName = cmdLine.split("\\s+")[0];
            final String[] classParts = fullClassName.split("\\.");
            cmdLine = classParts[classParts.length - 1];
        }
        return (cmdLine == null || cmdLine.isEmpty()) ?
                ManagementFactory.getRuntimeMXBean().getName() :
                cmdLine;
    }

    /**
     * Gets name of the component acting as a source for this measurement.
     * @return Name of the component.
     */
    @JsonProperty("c")
    public final String getComponentName(){
        return componentName;
    }

    public final void setDefaultComponentName(){
        setComponentName(getDefaultComponentName());
    }

    public final void setDefaultInstanceName(){
        setComponentName(getDefaultInstanceName());
    }

    /**
     * Sets name of the component acting as a source for this measurement.
     * @param value Name of the component.
     */
    public final void setComponentName(final String value){
        componentName = value;
    }

    @JsonProperty("i")
    public final String getInstanceName(){
        return instanceName;
    }

    public final void setInstanceName(final String value){
        instanceName = value;
    }

    @JsonProperty("m")
    public final String getMessage(){
        return message;
    }

    public final void setMessage(final String value){
        message = value;
    }
}
