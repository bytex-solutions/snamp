package com.snamp.licensing;

import net.xeoh.plugins.base.Plugin;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.parsers.*;
import java.io.*;
import java.security.*;
import java.util.logging.*;

/**
 * @author roman
 */
public final class LicenseLoader implements LicenseReader {
    private static final Logger log = Logger.getLogger("com.snamp.licensing");

    /**
     * Represents name of the system property that contains path to the license.
     */
    public static final String licenseFileProperty = "com.snamp.license";

    /**
     * Represents currently loaded license.
     */
    public static final LicenseReader currentLicense;

    private LicenseLoader(final Document licenseContent){

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

    private static final class LicenseKeySelector extends KeySelector {

        @Override
        public KeySelectorResult select(final KeyInfo keyInfo, final Purpose purpose, final AlgorithmMethod algorithmMethod, final XMLCryptoContext xmlCryptoContext) throws KeySelectorException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    static{
        //This method is specially not optimized for security purposes!!!
        LicenseReader loadedLicense = null;
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
            loadedLicense = new LicenseLoader(xmlLicense);
        }
        catch (final IOException | ParserConfigurationException | SAXException | MarshalException | XMLSignatureException | InvalidKeyException e) {
            log.log(Level.SEVERE, "Unable to load license file.", e);
            loadedLicense = new EmptyLicenseReader();
        }
        currentLicense = loadedLicense;
    }

    /**
     * Represents empty license reader.
     */
    private static final class EmptyLicenseReader implements LicenseReader{

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
