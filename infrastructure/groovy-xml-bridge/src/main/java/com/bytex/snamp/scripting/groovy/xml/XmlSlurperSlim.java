package com.bytex.snamp.scripting.groovy.xml;

import groovy.util.XmlSlurper;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

/**
 * Provides correct wiring to {@link groovy.util.XmlSlurper}
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class XmlSlurperSlim extends XmlSlurper {
    /**
     * Creates a non-validating and namespace-aware <code>XmlSlurper</code> which does not allow DOCTYPE declarations in documents.
     *
     * @throws ParserConfigurationException if no parser which satisfies the requested configuration can be created.
     * @throws SAXException                 for SAX errors.
     */
    public XmlSlurperSlim() throws ParserConfigurationException, SAXException {
    }

    /**
     * Creates a <code>XmlSlurper</code> which does not allow DOCTYPE declarations in documents.
     *
     * @param validating     <code>true</code> if the parser should validate documents as they are parsed; false otherwise.
     * @param namespaceAware <code>true</code> if the parser should provide support for XML namespaces; <code>false</code> otherwise.
     * @throws ParserConfigurationException if no parser which satisfies the requested configuration can be created.
     * @throws SAXException                 for SAX errors.
     */
    public XmlSlurperSlim(final boolean validating, final boolean namespaceAware) throws ParserConfigurationException, SAXException {
        super(validating, namespaceAware);
    }

    /**
     * Creates a <code>XmlSlurper</code>.
     *
     * @param validating              <code>true</code> if the parser should validate documents as they are parsed; false otherwise.
     * @param namespaceAware          <code>true</code> if the parser should provide support for XML namespaces; <code>false</code> otherwise.
     * @param allowDocTypeDeclaration <code>true</code> if the parser should provide support for DOCTYPE declarations; <code>false</code> otherwise.
     * @throws ParserConfigurationException if no parser which satisfies the requested configuration can be created.
     * @throws SAXException                 for SAX errors.
     */
    public XmlSlurperSlim(final boolean validating, final boolean namespaceAware, final boolean allowDocTypeDeclaration) throws ParserConfigurationException, SAXException {
        super(validating, namespaceAware, allowDocTypeDeclaration);
    }

    public XmlSlurperSlim(final XMLReader reader) {
        super(reader);
    }

    public XmlSlurperSlim(final SAXParser parser) throws SAXException {
        super(parser);
    }
}
