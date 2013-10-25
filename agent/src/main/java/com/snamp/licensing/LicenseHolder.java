package com.snamp.licensing;

import net.xeoh.plugins.base.Plugin;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.*;
import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.*;

/**
 * @author roman
 */
public final class LicenseHolder {
    private static final Logger log = Logger.getLogger("com.snamp.licensing");

    /**
     * Represents name of the system property that contains path to the license.
     */
    public static final String licenseFileProperty = "com.snamp.license";

    private static LicenseReader loadedLicense;

    private LicenseHolder(){

    }

    private static LicenseReader loadLicense(){
        //This method is specially not optimized for security purposes!!!
        final File licenseFile = new File(System.getProperty(licenseFileProperty, "snamp.lic"));
        try(final InputStream licenseStream = new FileInputStream(licenseFile)){
            final Key publicLicenseKey = new RSAPublicKeyImpl(null, null);
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document xmlLicense = builder.parse(licenseStream);
            final NodeList nl = xmlLicense.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            if(nl.getLength() == 0) throw new XMLSignatureException("License file has no digital signature.");
            //normal XML signature validation
            final DOMValidateContext valContext = new DOMValidateContext(publicLicenseKey, nl.item(0));
            final XMLSignatureFactory xmlsigfact = XMLSignatureFactory.getInstance("DOM");
            final XMLSignature signature = xmlsigfact.unmarshalXMLSignature(valContext);
            if(!signature.validate(valContext))
                throw new XMLSignatureException("Invalid license file signature.");
            return new XmlLicenseReader(xmlLicense);
        }
        catch (final IOException | ParserConfigurationException | SAXException | MarshalException | XMLSignatureException | InvalidKeyException e) {
            log.log(Level.SEVERE, "Unable to load license file.", e);
            return new EmptyLicenseReader();
        }
    }

    /**
     * Returns the currently loaded license for this process.
     * @return The license for this process.
     */
    public static LicenseReader getCurrentLinense(){
        return loadedLicense != null ? loadedLicense : (loadedLicense = loadLicense());
    }

    /**
     * Reloads the current license file.
     */
    public static void reloadCurrentLicense(){
        loadedLicense = loadLicense();
    }

    private static final class XmlLicenseReader implements LicenseReader {

        public XmlLicenseReader(final Document license){

        }

        /**
         * Returns a set of restrictions associated with the specified plugin.
         *
         * @param pluginImpl The plugin implementation.
         * @return A set of restrictions.
         */
        @Override
        public Restrictions getRestrictions(final Class<? extends Plugin> pluginImpl) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * Determines whether the specified plugin is allowed by this license.
         *
         * @param pluginImpl The class that represents a plugin.
         * @return {@literal true}, if the specified plug-in is allowed; otherwise, {@literal false}.
         */
        @Override
        public boolean isPluginAllowed(final Class<? extends Plugin> pluginImpl) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private static final class EmptyPermissionSet implements Restrictions{

        @Override
        public ValidationResult validate(final String optionName, final Validator validator) {
            return ValidationResult.UNKNOWN;
        }

        @Override
        public int size() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(final Object o) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Iterator<String> iterator() {
            return Arrays.<String>asList().iterator();
        }

        @Override
        public Object[] toArray() {
            return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public <T> T[] toArray(final T[] ts) {
            return ts;
        }

        @Override
        public boolean add(final String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(final Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(final Collection<?> objects) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean addAll(final Collection<? extends String> strings) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(final Collection<?> objects) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(final Collection<?> objects) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Represents empty license reader.
     */
    private static final class EmptyLicenseReader implements LicenseReader {

        /**
         * Returns a set of restrictions associated with the specified plugin.
         *
         * @param pluginImpl The plugin implementation.
         * @return A set of restrictions.
         */
        @Override
        public Restrictions getRestrictions(final Class<? extends Plugin> pluginImpl) {
            return new EmptyPermissionSet();
        }

        /**
         * Determines whether the specified plugin is allowed by this license.
         *
         * @param pluginImpl The class that represents a plugin.
         * @return {@literal true}, if the specified plug-in is allowed; otherwise, {@literal false}.
         */
        @Override
        public boolean isPluginAllowed(final Class<? extends Plugin> pluginImpl) {
            return false;
        }
    }
}
