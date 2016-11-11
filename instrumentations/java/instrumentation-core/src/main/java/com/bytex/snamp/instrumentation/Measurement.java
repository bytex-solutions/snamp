package com.bytex.snamp.instrumentation;

import org.codehaus.jackson.annotate.*;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents abstract POJO for all measurements.
 * @since 1.0
 * @version 1.0
 * @author Roman Sakno
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(IntegerMeasurement.class),
        @JsonSubTypes.Type(DoubleMeasurement.class),
        @JsonSubTypes.Type(StringMeasurement.class),
        @JsonSubTypes.Type(BooleanMeasurement.class),
        @JsonSubTypes.Type(TimeMeasurement.class),
        @JsonSubTypes.Type(Span.class)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Measurement implements Externalizable {
    private static final Pattern CLASS_NAME_SPLITTER = Pattern.compile("\\s+");
    private static final Pattern DOT_SPLITTER = Pattern.compile("\\.");

    static final String VALUE_JSON_PROPERTY = "v";
    private static final long serialVersionUID = -5122847206545823797L;

    private String instanceName;
    private String componentName;
    private String message;
    private long timestamp;
    private final LinkedHashMap<String, String> userData;

    Measurement(){
        instanceName = componentName = message = "";
        timestamp = System.currentTimeMillis();
        userData = new LinkedHashMap<String, String>();
    }

    @JsonProperty("userData")
    public final Map<String, String> getUserData(){
        return userData;
    }

    public final void setUserData(final Map<String, String> value){
        userData.clear();
        userData.putAll(value);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(instanceName);
        out.writeUTF(componentName);
        out.writeUTF(message);
        out.writeLong(timestamp);
        //save user data
        out.writeInt(userData.size());
        for(final Map.Entry<String, String> entry: userData.entrySet()){
            out.writeUTF(entry.getKey());
            out.writeUTF(entry.getValue());
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        instanceName = in.readUTF();
        componentName = in.readUTF();
        message = in.readUTF();
        timestamp = in.readLong();
        //load user data
        for (int size = in.readInt(); size > 0; size--)
            userData.put(in.readUTF(), in.readUTF());
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
            final String[] classParts = DOT_SPLITTER.split(CLASS_NAME_SPLITTER.split(cmdLine)[0]);
            cmdLine = classParts.length > 0 ? classParts[classParts.length - 1] : "";
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
        setInstanceName(getDefaultInstanceName());
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

    @JsonProperty("t")
    public final long getTimeStamp(){
        return timestamp;
    }

    public final void setTimeStamp(final long value){
        timestamp = value;
    }

    @JsonIgnore
    public final void setTimeStamp(final Date value){
        setTimeStamp(value.getTime());
    }
}
