package com.itworks.snamp.licensing.impl;

import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.itworks.snamp.ConcurrentResourceAccess;
import com.itworks.snamp.core.AbstractFrameworkService;
import com.itworks.snamp.licensing.LicenseLimitations;
import com.itworks.snamp.licensing.LicenseReader;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.AbstractConcurrentResourceAccess.Action;
import static com.itworks.snamp.AbstractConcurrentResourceAccess.ConsistentAction;

/**
 * Represents license reader for SNAMP license consumers. This class cannot be inherited
 * or instantiated.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
final class XmlLicenseReader extends AbstractFrameworkService implements LicenseReader {

    /**
     * Represents licensing context.
     */
    private static final class LicensingContext {
        public final Cache<Class<? extends LicenseLimitations>, LicenseLimitations> loadedLimitations;
        public Document loadedLicense;

        public LicensingContext() {
            loadedLimitations = CacheBuilder.newBuilder().softValues().initialCapacity(3).build();
            loadedLicense = null;
        }
    }

    private final ConcurrentResourceAccess<LicensingContext> licensingContext;

    /**
     * Initializes a new instance of the license reader service.
     *
     * @param loggerInstance A logger associated with this service.
     */
    public XmlLicenseReader(final Logger loggerInstance) {
        super(loggerInstance);
        licensingContext = new ConcurrentResourceAccess<>(new LicensingContext());
    }

    /**
     * Represents SNAMP license public key.
     * This class cannot be inherited.
     */
    private static final class LicensePublicKey implements DSAPublicKey {
        private final BigInteger y = new BigInteger("128845946868065518139105100346134088561451695532489534564502511284114388615352895117640704632157997300528432493194918376860877045101144240405823813650889167435931568780129892308016828375003253778562505916526570781073798310854595087678390054241923795575971323518799459014755412400842177938026989864621895589999");
        private final String format = "X.509";
        private final String algorithm = "DSA";
        private final byte[] encoded = new byte[]{
                48, -126, 1, -72, 48, -126, 1, 44, 6, 7, 42, -122, 72, -50, 56, 4, 1, 48, -126, 1, 31, 2, -127, -127, 0, -3, 127, 83, -127, 29, 117, 18, 41, 82, -33, 74, -100, 46, -20, -28, -25, -10, 17, -73, 82, 60, -17, 68, 0, -61, 30, 63, -128, -74, 81, 38, 105, 69, 93, 64, 34, 81, -5, 89, 61, -115, 88, -6, -65, -59, -11, -70, 48, -10, -53, -101, 85, 108, -41, -127, 59, -128, 29, 52, 111, -14, 102, 96, -73, 107, -103, 80, -91, -92, -97, -97, -24, 4, 123, 16, 34, -62, 79, -69, -87, -41, -2, -73, -58, 27, -8, 59, 87, -25, -58, -88, -90, 21, 15, 4, -5, -125, -10, -45, -59, 30, -61, 2, 53, 84, 19, 90, 22, -111, 50, -10, 117, -13, -82, 43, 97, -41, 42, -17, -14, 34, 3, 25, -99, -47, 72, 1, -57, 2, 21, 0, -105, 96, 80, -113, 21, 35, 11, -52, -78, -110, -71, -126, -94, -21, -124, 11, -16, 88, 28, -11, 2, -127, -127, 0, -9, -31, -96, -123, -42, -101, 61, -34, -53, -68, -85, 92, 54, -72, 87, -71, 121, -108, -81, -69, -6, 58, -22, -126, -7, 87, 76, 11, 61, 7, -126, 103, 81, 89, 87, -114, -70, -44, 89, 79, -26, 113, 7, 16, -127, -128, -76, 73, 22, 113, 35, -24, 76, 40, 22, 19, -73, -49, 9, 50, -116, -56, -90, -31, 60, 22, 122, -117, 84, 124, -115, 40, -32, -93, -82, 30, 43, -77, -90, 117, -111, 110, -93, 127, 11, -6, 33, 53, 98, -15, -5, 98, 122, 1, 36, 59, -52, -92, -15, -66, -88, 81, -112, -119, -88, -125, -33, -31, 90, -27, -97, 6, -110, -117, 102, 94, -128, 123, 85, 37, 100, 1, 76, 59, -2, -49, 73, 42, 3, -127, -123, 0, 2, -127, -127, 0, -73, 123, -109, 117, -39, -21, 70, 99, 62, 73, -20, -2, -78, -23, 31, 88, -75, 86, 49, -64, -22, 64, -80, 13, 11, 104, -2, 71, -119, -66, 15, -59, -31, 92, -45, 104, -57, 54, 18, 75, -84, -59, 93, 78, 88, -55, -101, 119, -110, 70, 118, 4, -109, 104, 43, -101, -15, 4, -119, 2, -60, 16, 123, 66, -94, 34, 101, -65, 59, -116, 99, -90, 38, 100, 40, -42, 59, -41, -104, 25, -18, 112, 68, -105, -95, -25, 125, -112, -49, -79, -101, -42, -23, 101, 42, -18, -56, 98, 57, -52, -40, -49, 10, -9, -22, 41, 18, 34, -119, 97, -6, -64, -58, 39, -91, 25, -100, -74, -115, 43, -97, 117, -73, -51, 35, 56, 32, 111
        };
        private final DSAParams params = new DSAParams() {
            private final BigInteger p = new BigInteger("178011905478542266528237562450159990145232156369120674273274450314442865788737020770612695252123463079567156784778466449970650770920727857050009668388144034129745221171818506047231150039301079959358067395348717066319802262019714966524135060945913707594956514672855690606794135837542707371727429551343320695239");
            private final BigInteger q = new BigInteger("864205495604807476120572616017955259175325408501");
            private final BigInteger g = new BigInteger("174068207532402095185811980123523436538604490794561350978495831040599953488455823147851597408940950725307797094915759492368300574252438761037084473467180148876118103083043754985190983472601550494691329488083395492313850000361646482644608492304078721818959999056496097769368017749273708962006689187956744210730");

            @Override
            public final BigInteger getP() {
                return p;
            }

            @Override
            public final BigInteger getQ() {
                return q;
            }

            @Override
            public final BigInteger getG() {
                return g;
            }
        };

        @Override
        public final BigInteger getY() {
            return y;
        }

        @Override
        public final DSAParams getParams() {
            return params;
        }

        @Override
        public final String getAlgorithm() {
            return algorithm;
        }

        @Override
        public final String getFormat() {
            return format;
        }

        @Override
        public final byte[] getEncoded() {
            return encoded;
        }
    }

    /**
     * Gets path to the SNAMP license file.
     *
     * @return A path to the SNAMP license file.
     */
    public static String getLicenseFile() {
        return System.getProperty(LICENSE_FILE_PROPERTY, "./snamp.lic");
    }

    private Document loadLicense() {
        //This method is specially not optimized for security purposes!!!
        try (final InputStream licenseStream = new FileInputStream(getLicenseFile())) {
            final Key publicLicenseKey = new LicensePublicKey();
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document xmlLicense = builder.parse(licenseStream);
            final NodeList nl = xmlLicense.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            if (nl.getLength() == 0) throw new XMLSignatureException("License file has no digital signature.");
            //normal XML signature validation
            final DOMValidateContext valContext = new DOMValidateContext(publicLicenseKey, nl.item(0));
            final XMLSignatureFactory xmlsigfact = XMLSignatureFactory.getInstance("DOM");
            final XMLSignature signature = xmlsigfact.unmarshalXMLSignature(valContext);
            if (!signature.validate(valContext))
                throw new XMLSignatureException("Invalid license file signature.");
            return xmlLicense;
        } catch (final IOException | ParserConfigurationException | SAXException | MarshalException | XMLSignatureException e) {
            getLogger().log(Level.SEVERE, "Unable to load license file.", e);
            return null;
        }
    }

    /**
     * Reloads the current license file.
     */
    @Override
    public void reload() {
        final Document newLicenseContent = loadLicense();
        licensingContext.write(new ConsistentAction<LicensingContext, Void>() {
            @Override
            public final Void invoke(final LicensingContext resource) {
                resource.loadedLimitations.cleanUp();
                resource.loadedLicense = newLicenseContent;
                return null;
            }
        });
    }

    private <T extends LicenseLimitations> T getLimitations(final Class<T> descriptor, final String licensedObject, final Unmarshaller deserializer) throws JAXBException {
        return licensingContext.read(new Action<LicensingContext, T, JAXBException>() {
            @Override
            public T invoke(final LicensingContext resource) throws JAXBException {
                final NodeList target = resource.loadedLicense.getElementsByTagName(licensedObject);
                return target.getLength() > 0 ? descriptor.cast(deserializer.unmarshal(target.item(0))) : null;
            }
        });
    }

    /**
     * Returns the limitations from the currently loaded license.
     *
     * @param limitationsDescriptor The limitations descriptor.
     * @param fallback              The fallback factory that produces limitation holder if license is not available.
     * @param <T>                   Type of the license limitations descriptor.
     * @return A new instance of the license limitations.
     */
    public <T extends LicenseLimitations> T getLimitations(final Class<T> limitationsDescriptor, final Supplier<T> fallback) {
        if (limitationsDescriptor == null) return fallback.get();
        T result = licensingContext.read(new ConcurrentResourceAccess.ConsistentAction<LicensingContext, T>() {
            @Override
            public T invoke(final LicensingContext resource) {
                return resource.loadedLicense == null ?
                        fallback.get() :
                        limitationsDescriptor.cast(resource.loadedLimitations.getIfPresent(limitationsDescriptor));
            }
        });
        //limitations is not in cache, creates a new limitations reader
        if (result != null) return result;
        else try {
            final JAXBContext context = JAXBContext.newInstance(limitationsDescriptor);
            if (limitationsDescriptor.isAnnotationPresent(XmlRootElement.class)) {
                final XmlRootElement rootElement = limitationsDescriptor.getAnnotation(XmlRootElement.class);
                final LicenseLimitations limits = result = getLimitations(limitationsDescriptor, rootElement.name(), context.createUnmarshaller());
                //writes result to the cache
                licensingContext.write(new ConsistentAction<LicensingContext, Void>() {
                    @Override
                    public final Void invoke(final LicensingContext resource) {
                        resource.loadedLimitations.put(limitationsDescriptor, limits);
                        return null;
                    }
                });
            } else result = fallback.get();
        } catch (final JAXBException e) {
            getLogger().warning(e.getLocalizedMessage());
            result = fallback.get();
        }
        return result;
    }
}
