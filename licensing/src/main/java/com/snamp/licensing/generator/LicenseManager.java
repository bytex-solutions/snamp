package com.snamp.licensing.generator;

import java.io.*;
import java.security.*;

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

    public static void main(final String[] args){
        switch (args.length){
            default: System.out.println("snmplicgen [command] [args]");
            case 1: doCommand(args[0]); return;
            case 2: doCommand(args[0], args[1]); return;
        }
    }
}
