package com.itworks.snamp.licensing;

import com.google.common.base.MoreObjects;
import com.google.common.io.CharStreams;
import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.internal.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.management.openmbean.SimpleType;
import javax.xml.bind.JAXBException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.Key;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;

import static com.itworks.snamp.jmx.OpenMBean.OpenAttribute;

/**
 * Exposes access to the SNAMP license content.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
final class LicenseAttribute extends OpenAttribute<String, SimpleType<String>> implements LicenseLoader {
    /**
     * Represents name of the entry in the configuration dictionary which
     * contains raw license content in the form of byte array.
     */
    private static final String LICENSE_CONTENT_ENTRY = "License";

    private static final String LICENSE_CONTENT_CHARSET = "UTF-8";

    private static final String NAME = "license";

    private static final class XmlLicenseHolder{
        private final XmlLicense license;
        private final Document source;

        private XmlLicenseHolder(final Document licenseContent) throws JAXBException {
            this.source = Objects.requireNonNull(licenseContent);
            this.license = XmlLicense.load(licenseContent);
        }

        private XmlLicenseHolder(final String licenseContent) throws MarshalException, ParserConfigurationException, SAXException, XMLSignatureException, IOException, JAXBException {
            this(fromString(licenseContent));
        }

        private XmlLicenseHolder(final InputStream licenseContent) throws IOException, MarshalException, ParserConfigurationException, SAXException, XMLSignatureException, JAXBException {
            this(fromStream(licenseContent));
        }

        public String toString() throws IllegalStateException{
            try {
                try(final StringWriter writer = new StringWriter()){
                    final DOMSource domSource = new DOMSource(source);
                    final StreamResult result = new StreamResult(writer);
                    final TransformerFactory tf = TransformerFactory.newInstance();
                    final Transformer transformer = tf.newTransformer();
                    transformer.transform(domSource, result);
                    return writer.toString();
                }
            } catch (final IOException | TransformerException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private volatile XmlLicenseHolder license;

    LicenseAttribute() throws JAXBException {
        super(NAME, SimpleType.STRING);
        license = null;

    }

    private static Document toXmlDocument(final InputSource licenseSource) throws ParserConfigurationException, IOException, SAXException, XMLSignatureException, MarshalException{
        final Key publicLicenseKey = new LicensePublicKey();
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document xmlLicense = builder.parse(licenseSource);
        final NodeList nl = xmlLicense.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) throw new XMLSignatureException("License file has no digital signature.");
        //normal XML signature validation
        final DOMValidateContext valContext = new DOMValidateContext(publicLicenseKey, nl.item(0));
        final XMLSignatureFactory xmlsigfact = XMLSignatureFactory.getInstance("DOM");
        final XMLSignature signature = xmlsigfact.unmarshalXMLSignature(valContext);
        if (signature.validate(valContext))
            return xmlLicense;
        else throw new XMLSignatureException("Invalid license file signature.");
    }

    private static Document fromString(final String licenseContent) throws ParserConfigurationException, IOException, SAXException, XMLSignatureException, MarshalException {
        if(licenseContent == null || licenseContent.isEmpty()) return null;
        final InputSource source = new InputSource();
        try(final StringReader reader = new StringReader(licenseContent)){
            source.setCharacterStream(reader);
            return toXmlDocument(source);
        }
    }

    static Document fromStream(final InputStream licenseContent) throws IOException, MarshalException, ParserConfigurationException, SAXException, XMLSignatureException {
        try(final InputStreamReader reader = new InputStreamReader(licenseContent, LICENSE_CONTENT_CHARSET)){
            final InputSource source = new InputSource();
            source.setCharacterStream(reader);
            return toXmlDocument(source);
        }
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextByObject(this);
    }

    @Override
    public String getValue() throws IOException, TransformerException {
        return Objects.toString(license, "");
    }

    private static void saveLicense(final String licenseContent,
                                    final ConfigurationAdmin configAdmin) throws IOException {
        final Configuration storage = configAdmin.getConfiguration(LICENSE_PID);
        final Dictionary<String, String> props = new Hashtable<>(3);
        props.put(LICENSE_CONTENT_ENTRY, MoreObjects.firstNonNull(licenseContent, ""));
        storage.update(props);
    }

    @Override
    public void setValue(final String value) throws Exception {
        final ServiceReferenceHolder<ConfigurationAdmin> configAdminRef = new ServiceReferenceHolder<>(getBundleContext(), ConfigurationAdmin.class);
        try{
            saveLicense(value, configAdminRef.get());
        }
        finally {
            configAdminRef.release(getBundleContext());
        }
    }

    @Override
    public void updated(final Dictionary<String, ?> properties) throws ConfigurationException {
        if (properties == null) license = null;
        else {
            final String licenseContent = Utils.getProperty(properties, LICENSE_CONTENT_ENTRY, String.class, "");
            try {
                this.license = new XmlLicenseHolder(licenseContent);
            } catch (final JAXBException | ParserConfigurationException | SAXException | IOException | XMLSignatureException | MarshalException e) {
                LicenseLogger.error("Unable to update SNAMP license", e);
                throw new ConfigurationException(LICENSE_CONTENT_ENTRY, "Invalid XML content of the license", e);
            }
        }
    }

    @Override
    public void loadLicense(final InputStream licenseContent) throws IOException {
        final ServiceReferenceHolder<ConfigurationAdmin> configAdminRef = new ServiceReferenceHolder<>(getBundleContext(), ConfigurationAdmin.class);
        try(final InputStreamReader reader = new InputStreamReader(licenseContent, LICENSE_CONTENT_CHARSET)){
            saveLicense(CharStreams.toString(reader), configAdminRef.get());
        }
        finally {
            configAdminRef.release(getBundleContext());
        }
    }

    XmlLicense getLicense() {
        final XmlLicenseHolder holder = license;
        return holder != null ? holder.license : new XmlLicense();
    }
}
