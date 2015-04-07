package com.itworks.snamp.licensing;

import com.google.common.collect.ImmutableSet;
import com.itworks.snamp.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.annotation.*;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
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

    public static XmlLicense fromString(final String content) throws IOException{
        if(content == null || content.isEmpty()) return new XmlLicense();
        return null;
    }

    public static XmlLicense fromStream(final InputStream licenseContent) throws ParserConfigurationException, IOException, SAXException, MarshalException, XMLSignatureException {
        final Key publicLicenseKey = new LicensePublicKey();
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document xmlLicense = builder.parse(licenseContent);
        final NodeList nl = xmlLicense.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) throw new XMLSignatureException("License file has no digital signature.");
        //normal XML signature validation
        final DOMValidateContext valContext = new DOMValidateContext(publicLicenseKey, nl.item(0));
        final XMLSignatureFactory xmlsigfact = XMLSignatureFactory.getInstance("DOM");
        final XMLSignature signature = xmlsigfact.unmarshalXMLSignature(valContext);
        if (!signature.validate(valContext))
            throw new XMLSignatureException("Invalid license file signature.");
        return null;
    }


}
