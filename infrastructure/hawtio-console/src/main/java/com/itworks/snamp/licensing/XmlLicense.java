package com.itworks.snamp.licensing;

import com.google.common.collect.ImmutableSet;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.internal.Utils;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
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

    private static final String ALL_ADAPTERS = "all";
    private static final Unmarshaller unmarshaller;
    private static final Marshaller marshaller;

    static {
        try {
            final JAXBContext context = JAXBContext.newInstance(XmlLicense.class);
            unmarshaller = context.createUnmarshaller();
            marshaller = context.createMarshaller();
        } catch (final JAXBException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Set<String> adapters;
    private long numberOfManagedResources;
    private String issuer = "IT Works Inc.";
    private String customer = "Anonymous";

    public XmlLicense(final Set<String> allowedAdapters,
                      final long maxNumberOfManagedResources){
        this.adapters = allowedAdapters.isEmpty() ?
                new HashSet<String>(10):
                new HashSet<>(allowedAdapters);
        this.numberOfManagedResources = maxNumberOfManagedResources;
    }

    public XmlLicense(){
        this(Collections.<String>emptySet(), 0L);
    }

    /**
     * Gets maximum number of managed resources.
     * @return The maximum number of managed resources.
     */
    @XmlElement(name = "maximumNumberOfManagedResources", namespace = NAMESPACE)
    public long getNumberOfManagedResources(){
        return numberOfManagedResources;
    }

    @XmlAttribute(name = "issuer", namespace = NAMESPACE)
    public String getIssuer(){
        return issuer;
    }

    public void setIssuer(final String value){
        this.issuer = value;
    }

    @XmlAttribute(name = "customer", namespace = NAMESPACE)
    public String getCustomer(){
        return customer;
    }

    public void setCustomer(final String value){
        this.customer = value;
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
    @XmlElement(name = "allowedAdapter", namespace = NAMESPACE)
    public String[] getAllowedAdapters(){
        return ArrayUtils.toArray(adapters, String.class);
    }

    /**
     * Allows any resource adapter.
     */
    public void allowAllAdapters(){
        setAllowedAdapters(ALL_ADAPTERS);
    }

    public void disallowAllAdapters(){
        adapters.clear();
    }

    /**
     * Overwrites a set of allowed adapters.
     * @param values An array of allowed adapters.
     */
    public void setAllowedAdapters(final String... values){
        disallowAllAdapters();
        addAllowedAdapters(values);
    }

    /**
     * Appends a new set of allowed adapters.
     * @param values An array of allowed adapters.
     */
    public void addAllowedAdapters(final String... values){
        Collections.addAll(adapters, values);
    }

    public boolean isAdaptersAllowed(final Collection<String> adapterNames){
        return anyAdapterAllowed() || adapters.containsAll(adapterNames);
    }

    private boolean anyAdapterAllowed(){
        return adapters.contains(ALL_ADAPTERS);
    }

    /**
     * Determines whether the specified resource adapter
     * @param adapterNames The system name of the resource adapter.
     * @return {@literal true}, if the specified resource adapter is allowed by this license; otherwise, {@literal false}.
     */
    public boolean isAdaptersAllowed(final String... adapterNames){
        return isAdaptersAllowed(ImmutableSet.copyOf(adapterNames));
    }

    @Override
    public String toString() {
        final String FORMAT = "Allowed adapters: %s; maxNumberOfManagedResources: %s";
        return String.format(FORMAT,
                anyAdapterAllowed() ? ALL_ADAPTERS : adapters.toString(),
                numberOfManagedResources);
    }

    public void save(final Writer output) throws JAXBException {
        marshaller.marshal(this, output);
    }

    public void save(final OutputStream output) throws JAXBException {
        marshaller.marshal(this, output);
    }

    public void save(final File output) throws JAXBException {
        marshaller.marshal(this, output);
    }

    public static XmlLicense load(final Node input) throws JAXBException {
        return Utils.safeCast(unmarshaller.unmarshal(input), XmlLicense.class);
    }

    public static XmlLicense load(final Reader input) throws JAXBException {
        return Utils.safeCast(unmarshaller.unmarshal(input), XmlLicense.class);
    }
}
