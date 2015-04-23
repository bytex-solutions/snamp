package com.itworks.snamp.licensing;

import com.itworks.snamp.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class XmlLicensingTest extends Assert {
    @Test
    public void licenseCheckTest(){
        final XmlLicense license = new XmlLicense();
        license.allowAllAdapters();
        assertTrue(license.isAdaptersAllowed("a", "b"));
    }

    @Test
    public void serializationDeserializationTest() throws JAXBException, IOException {
        final JAXBContext context = JAXBContext.newInstance(XmlLicense.class);
        XmlLicense license = new XmlLicense();
        license.allowAllAdapters();
        license.setNumberOfManagedResources(Long.MAX_VALUE);
        final Marshaller serializer = context.createMarshaller();
        final String licenseContent;
        try(final StringWriter writer = new StringWriter(1024)){
            serializer.marshal(license, writer);
            licenseContent = writer.toString();
        }
        assertFalse(licenseContent.isEmpty());
        try(final InputStream input = new FileInputStream(getLicenseFile());
            final InputStreamReader reader = new InputStreamReader(input, IOUtils.DEFAULT_CHARSET)){
            license = XmlLicense.load(reader);
        }
        assertNotNull(license);
        assertTrue(license.isAdaptersAllowed("jmx"));
        assertTrue(license.checkNumberOfManagedResources(Long.MAX_VALUE));
    }

    private static String getLicenseFile(){
        return System.getProperty("com.itworks.snamp.licensing.file", "unlimited.lic");
    }

    @Test
    public void signatureVerificationTest() throws IOException, MarshalException, ParserConfigurationException, SAXException, XMLSignatureException {
        try(final InputStream input = new FileInputStream(getLicenseFile())){
            assertNotNull(LicenseAttribute.fromStream(input));
        }
    }
}
