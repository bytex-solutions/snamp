package com.itworks.snamp.licensing.generator;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.*;
import java.util.*;

import static javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS;

/**
 * Represents license manager.
 * @author Roman Sakno
 */
public final class LicenseManager {
    private static void displayHelp(){
        System.out.println("-h, --help          Displays this message");
        System.out.println("-g, --generate      Generates 1024-bit DSA key pair");
        System.out.println("-p, --public-key    Displays public key information");
        System.out.println("-s, --sign          Signs the specified license file");
        System.out.println("-v, --verify        Verifies the license file");
    }

    private static void doCommand(final String commandName){
        switch (commandName){
            case "-h":
            case "--help":
                displayHelp();
                return;
            default: System.out.println("Unknown command");

        }
    }

    private static void displayPublicKey(final String inputFile){
        try(final InputStream fs = new FileInputStream(inputFile); final ObjectInputStream deserializer = new ObjectInputStream(fs)){
            final KeyPair kp = (KeyPair)deserializer.readObject();
            final Key publicKey = kp.getPublic();
            System.out.println("Public key information");
            System.out.println(String.format("Algorithm name: %s", publicKey.getAlgorithm()));
            System.out.println(String.format("Format name: %s", publicKey.getFormat()));
            System.out.println(String.format("Bytes: %s", Arrays.toString(publicKey.getEncoded())));
        }
        catch (final IOException | ClassNotFoundException e) {
            System.err.println(e);
        }
    }

    private static void generateKeyPair(final String outputFile){
        try {
            final KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
            kpg.initialize(1024);
            final KeyPair kp = kpg.generateKeyPair();
            try(final OutputStream fs = new FileOutputStream(outputFile); final ObjectOutputStream serializer = new ObjectOutputStream(fs)){
                serializer.writeObject(kp);
            }
            displayPublicKey(outputFile);
        }
        catch (final NoSuchAlgorithmException | IOException e) {
            System.err.println(e);
        }

    }

    private static Collection<Node> copyNodeList(final NodeList nodes){
        final Collection<Node> result = new ArrayList<>(nodes.getLength());
        for(int i = 0; i < nodes.getLength(); i++)
            result.add(nodes.item(i));
        return result;
    }

    private static Document signLicense(final KeyPair pair, final InputStream licenseFile) throws ParserConfigurationException, IOException, SAXException, GeneralSecurityException, MarshalException, XMLSignatureException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        final DocumentBuilder builder = dbf.newDocumentBuilder();
        final Document license = builder.parse(licenseFile);
        //remove other signatures
        for(final Node sig: copyNodeList(license.getElementsByTagName("Signature")))
            sig.getParentNode().removeChild(sig);
        final DOMSignContext dsc = new DOMSignContext(pair.getPrivate(), license.getDocumentElement());
        final XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        final Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA1, null),
                        Collections.singletonList
                                (fac.newTransform(Transform.ENVELOPED,
                                        (TransformParameterSpec) null)), null, null);
        final SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod
                        (INCLUSIVE_WITH_COMMENTS,
                                (C14NMethodParameterSpec) null),
                        fac.newSignatureMethod(SignatureMethod.DSA_SHA1, null),
                        Collections.singletonList(ref));
        final KeyInfoFactory kif = fac.getKeyInfoFactory();
        final KeyValue kv = kif.newKeyValue(pair.getPublic());
        final KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));
        final XMLSignature signature = fac.newXMLSignature(si, ki);
        signature.sign(dsc);
        return license;
    }

    private static Document signLicense(final InputStream keyFile, final InputStream licenseFile) throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException, GeneralSecurityException, MarshalException, XMLSignatureException {
        //restore private key
        try(final ObjectInputStream is = new ObjectInputStream(keyFile)){
            return signLicense((KeyPair)is.readObject(), licenseFile);
        }
    }

    private static void signLicense(final String keyFile, final String licenseFile){
        Document license = null;
        try(final InputStream licenseFileStream = new FileInputStream(licenseFile); final InputStream keyFileStream = new FileInputStream(keyFile)){
            license = signLicense(keyFileStream, licenseFileStream);
        }
        catch (final IOException | ParserConfigurationException | SAXException | ClassNotFoundException | GeneralSecurityException | MarshalException | XMLSignatureException e) {
            System.err.println(e);
        }
        if(license != null)
            try(final OutputStream os = new FileOutputStream(licenseFile)){
                final TransformerFactory tf = TransformerFactory.newInstance();
                final Transformer trans = tf.newTransformer();
                trans.transform(new DOMSource(license), new StreamResult(os));

            } catch (final IOException | TransformerException e) {
                System.err.println(e);
            }
    }

    private static void verifyLicense(final String keyFile, final String licenseFile){

    }

    private static void doCommand(final String commandName, final String arg0){
        switch (commandName){
            case "-g":
            case "-generate":
                generateKeyPair(arg0);
                return;
            case "-p":
            case "--public-key":
                displayPublicKey(arg0);
                return;
            default: System.out.println("Unknown command");
        }
    }

    private static void doCommand(final String commandName, final String arg0, final String arg1){
        switch (commandName){
            case "-s":
            case "--sign":
                signLicense(arg0, arg1);
                return;
            case "-v":
            case "--verify":
                verifyLicense(arg0, arg1);
                return;
            default: System.out.println("Unknown command");
        }
    }

    public static void main(final String[] args){
        switch (args.length){
            default: System.out.println("snmplicgen [command] [args]");return;
            case 1: doCommand(args[0]); return;
            case 2: doCommand(args[0], args[1]); return;
            case 3: doCommand(args[0], args[1], args[2]);
        }
    }
}
