package com.snamp.licensing.generator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.parsers.*;
import java.io.*;
import java.security.*;
import java.util.Arrays;

/**
 * Represents license manager.
 * @author roman
 */
public final class LicenseManager {
    private static void displayHelp(){
        System.out.println("-h, --help          Displays this message");
        System.out.println("-g, --generate      Generates 1024-bit DSA key pair");
        System.out.println("-p, --public-key    Displays public key information");
        System.out.println("-s, --sign          Signs the specified license file");
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

    private static void signLicense(final KeyPair pair, final InputStream licenseFile) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        final DocumentBuilder builder = dbf.newDocumentBuilder();
        final Document license = builder.parse(licenseFile);
        final DOMSignContext dsc = new DOMSignContext(pair.getPrivate(), license.getDocumentElement());
        final XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

    }

    private static void signLicense(final InputStream keyFile, final InputStream licenseFile) throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException {
        //restore private key
        try(final ObjectInputStream is = new ObjectInputStream(keyFile)){
            signLicense((KeyPair)is.readObject(), licenseFile);
        }
    }

    private static void signLicense(final String keyFile, final String licenseFile){

        try(final InputStream licenseFileStream = new FileInputStream(licenseFile); final InputStream keyFileStream = new FileInputStream(keyFile)){
            signLicense(keyFileStream, licenseFileStream);
        }
        catch (final IOException | ParserConfigurationException | SAXException | ClassNotFoundException e) {
            System.err.println(e);
        }
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
            default: System.out.println("Unknown command"); return;
        }
    }

    private static void doCommand(final String commandName, final String arg0, final String arg1){
        switch (commandName){
            case "-s":
            case "-sign":
                signLicense(arg0, arg1);
                return;
            default: System.out.println("Unknown command"); return;
        }
    }

    public static void main(final String[] args){
        switch (args.length){
            default: System.out.println("snmplicgen [command] [args]");
            case 1: doCommand(args[0]); return;
            case 2: doCommand(args[0], args[1]); return;
            case 3: doCommand(args[0], args[1], args[2]); return;
        }
    }
}
