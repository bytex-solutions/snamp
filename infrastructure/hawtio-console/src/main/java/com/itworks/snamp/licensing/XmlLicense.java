package com.itworks.snamp.licensing;

import com.google.common.collect.ImmutableSet;
import com.itworks.snamp.ArrayUtils;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents SNAMP license.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@XmlRootElement(name = "license", namespace = XmlLicense.NAMESPACE)
@XmlAccessorType(XmlAccessType.PROPERTY)
public final class XmlLicense {
    static final String NAMESPACE = "http://www.itworks.com/snamp/schemas/license";

    /**
     * Represents encoding of the license file.
     */
    public static final String LICENSE_CONTENT_ENCODING = "UTF-8";

    /**
     * Represents persistence identifier used to read and write license content.
     */
    private static final String LICENSE_PID = "com.itworks.snamp.license";

    /**
     * Represents name of the entry in the configuration dictionary which
     * contains raw license content in the form of byte array.
     */
    private static final String LICENSE_CONTENT_ENTRY = "license";

    private static final String ALL_ADAPTERS = "all";

    private final Set<String> adapters = new HashSet<>(10);
    private long numberOfManagedResources = 0L;

    /**
     * Gets maximum number of managed resources.
     * @return The maximum number of managed resources.
     */
    @XmlElement(name = "maximumNumberOfManagedResources", namespace = NAMESPACE)
    public long getNumberOfManagedResources(){
        return numberOfManagedResources;
    }

    /**
     * Sets maximum number of managed resources.
     * @param value The maximum number of managed resources.
     */
    public void setNumberOfManagedResources(final long value){
        numberOfManagedResources = value;
    }

    /**
     * Checks whether the actual number of managed resources is not greater that
     * the maximum number of managed resources declared in this license.
     * @param actual The actual number of managed resources.
     * @return {@literal true}, if actual number of managed resources is less or equal than the maximum number of managed resources
     *      declared in this license; otherwise, {@literal false}.
     */
    public boolean checkNumberOfManagedResources(final long actual){
        return actual <= numberOfManagedResources;
    }

    /**
     * Gets an array of allowed adapters.
     * @return An array of allowed adapters.
     */
    @XmlElement(name = "allowedAdapters", namespace = NAMESPACE)
    public String[] getAllowedAdapters(){
        return ArrayUtils.toArray(adapters, String.class);
    }

    /**
     * Allows any resource adapter.
     */
    public void allowAllAdapters(){
        setAllowedAdapters(ALL_ADAPTERS);
    }

    /**
     * Overwrites a set of allowed adapters.
     * @param values An array of allowed adapters.
     */
    public void setAllowedAdapters(final String... values){
        adapters.clear();
        addAllowedAdapters(values);
    }

    /**
     * Appends a new set of allowed adapters.
     * @param values An array of allowed adapters.
     */
    public void addAllowedAdapters(final String... values){
        Collections.addAll(adapters, values);
    }

    @XmlTransient
    public boolean isAdaptersAllowed(final Collection<String> adapterNames){
        return adapters.contains(ALL_ADAPTERS) || adapters.containsAll(adapterNames);
    }

    /**
     * Determines whether the specified resource adapter
     * @param adapterNames The system name of the resource adapter.
     * @return {@literal true}, if the specified resource adapter is allowed by this license; otherwise, {@literal false}.
     */
    @XmlTransient
    public boolean isAdaptersAllowed(final String... adapterNames){
        return isAdaptersAllowed(ImmutableSet.copyOf(adapterNames));
    }

    /**
     * Reads license content from OSGi persistent storage.
     * @param configAdmin OSGi configuration admin that provides access to the persistent storage. Cannot be {@literal null}.
     * @return Deserialized
     * @throws IOException
     */
    public static XmlLicense readLicense(final ConfigurationAdmin configAdmin) throws IOException{
        return null;
    }

    public static String toString(final XmlLicense license) throws IOException{
        return license.toString();
    }

    public static XmlLicense fromString(final String content) throws IOException{
        return null;
    }

    public static void writeLicense(final ConfigurationAdmin configAdmin,
                                    final XmlLicense license) {

    }
}
