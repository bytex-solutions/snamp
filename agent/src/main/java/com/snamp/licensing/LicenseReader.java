package com.snamp.licensing;

import com.snamp.Activator;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import sun.security.provider.DSAPublicKeyImpl;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.*;
import java.security.*;
import java.util.*;
import java.util.logging.*;

/**
 * @author roman
 */
public final class LicenseReader {
    private static final Logger log = Logger.getLogger("com.snamp.licensing");

    /**
     * Represents name of the system property that contains path to the license.
     */
    public static final String LICENSE_FILE_PROPERTY = "com.snamp.license";

    private static final Map<Class<? extends LicenseLimitations>, LicenseLimitations> loadedLimitations;
    private static Document loadedLicense;

    static {
        loadedLimitations = new HashMap<>(5);
    }

    private LicenseReader(){

    }

    private static Document loadLicense(){
        //This method is specially not optimized for security purposes!!!
        final File licenseFile = new File(System.getProperty(LICENSE_FILE_PROPERTY, "snamp.lic"));
        try(final InputStream licenseStream = new FileInputStream(licenseFile)){
            final Key publicLicenseKey = new DSAPublicKeyImpl(new byte[]{
                    48, -126, 1, -72, 48, -126, 1, 44, 6, 7, 42, -122, 72, -50, 56, 4, 1, 48, -126, 1, 31, 2, -127, -127, 0, -3, 127, 83, -127, 29, 117, 18, 41, 82, -33, 74, -100, 46, -20, -28, -25, -10, 17, -73, 82, 60, -17, 68, 0, -61, 30, 63, -128, -74, 81, 38, 105, 69, 93, 64, 34, 81, -5, 89, 61, -115, 88, -6, -65, -59, -11, -70, 48, -10, -53, -101, 85, 108, -41, -127, 59, -128, 29, 52, 111, -14, 102, 96, -73, 107, -103, 80, -91, -92, -97, -97, -24, 4, 123, 16, 34, -62, 79, -69, -87, -41, -2, -73, -58, 27, -8, 59, 87, -25, -58, -88, -90, 21, 15, 4, -5, -125, -10, -45, -59, 30, -61, 2, 53, 84, 19, 90, 22, -111, 50, -10, 117, -13, -82, 43, 97, -41, 42, -17, -14, 34, 3, 25, -99, -47, 72, 1, -57, 2, 21, 0, -105, 96, 80, -113, 21, 35, 11, -52, -78, -110, -71, -126, -94, -21, -124, 11, -16, 88, 28, -11, 2, -127, -127, 0, -9, -31, -96, -123, -42, -101, 61, -34, -53, -68, -85, 92, 54, -72, 87, -71, 121, -108, -81, -69, -6, 58, -22, -126, -7, 87, 76, 11, 61, 7, -126, 103, 81, 89, 87, -114, -70, -44, 89, 79, -26, 113, 7, 16, -127, -128, -76, 73, 22, 113, 35, -24, 76, 40, 22, 19, -73, -49, 9, 50, -116, -56, -90, -31, 60, 22, 122, -117, 84, 124, -115, 40, -32, -93, -82, 30, 43, -77, -90, 117, -111, 110, -93, 127, 11, -6, 33, 53, 98, -15, -5, 98, 122, 1, 36, 59, -52, -92, -15, -66, -88, 81, -112, -119, -88, -125, -33, -31, 90, -27, -97, 6, -110, -117, 102, 94, -128, 123, 85, 37, 100, 1, 76, 59, -2, -49, 73, 42, 3, -127, -123, 0, 2, -127, -127, 0, -73, 123, -109, 117, -39, -21, 70, 99, 62, 73, -20, -2, -78, -23, 31, 88, -75, 86, 49, -64, -22, 64, -80, 13, 11, 104, -2, 71, -119, -66, 15, -59, -31, 92, -45, 104, -57, 54, 18, 75, -84, -59, 93, 78, 88, -55, -101, 119, -110, 70, 118, 4, -109, 104, 43, -101, -15, 4, -119, 2, -60, 16, 123, 66, -94, 34, 101, -65, 59, -116, 99, -90, 38, 100, 40, -42, 59, -41, -104, 25, -18, 112, 68, -105, -95, -25, 125, -112, -49, -79, -101, -42, -23, 101, 42, -18, -56, 98, 57, -52, -40, -49, 10, -9, -22, 41, 18, 34, -119, 97, -6, -64, -58, 39, -91, 25, -100, -74, -115, 43, -97, 117, -73, -51, 35, 56, 32, 111
            });
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
            return xmlLicense;
        }
        catch (final IOException | ParserConfigurationException | SAXException | MarshalException | XMLSignatureException | InvalidKeyException e) {
            log.log(Level.SEVERE, "Unable to load license file.", e);
            return null;
        }
    }

    /**
     * Reloads the current license file.
     */
    public static void reloadCurrentLicense(){
        loadedLicense = loadLicense();
        loadedLimitations.clear();
    }

    private static <T extends LicenseLimitations> T getLimitations(final String licensedObject, final Unmarshaller deserializer) throws JAXBException {
        final NodeList target = loadedLicense.getElementsByTagName(licensedObject);
        return target.getLength() > 0 ? (T)deserializer.unmarshal(target.item(0)) : null;
    }

    public static <T extends LicenseLimitations> T getLimitations(final Class<T> limitationsDescriptor, final Activator<T> fallback){
        T result = null;
        if(loadedLicense == null || limitationsDescriptor == null) return fallback.newInstance();
        else if(loadedLimitations.containsKey(limitationsDescriptor)) result = (T)loadedLimitations.get(limitationsDescriptor);
        else try {
            final JAXBContext context = JAXBContext.newInstance(limitationsDescriptor);
            if(limitationsDescriptor.isAnnotationPresent(XmlRootElement.class)){
                final XmlRootElement rootElement = limitationsDescriptor.getAnnotation(XmlRootElement.class);
                result = LicenseReader.<T>getLimitations(rootElement.name(), context.createUnmarshaller());
            }
            else result = null;


        }
        catch (final JAXBException e) {
            log.warning(e.getLocalizedMessage());
            result = fallback.newInstance();
        }
        finally {
            loadedLimitations.put(limitationsDescriptor, result);
        }
        return result;
    }
}
