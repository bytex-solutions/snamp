package com.itworks.jcommands.testing;

import com.itworks.jcommands.impl.XmlCommandLineToolOutputParser;
import com.itworks.jcommands.impl.XmlCommandLineToolProfile;
import com.itworks.jcommands.impl.XmlCommandLineToolReturnType;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigInteger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class XmlCommandLineToolProfileTest extends AbstractUnitTest<XmlCommandLineToolProfile> {
    public XmlCommandLineToolProfileTest(){
        super(XmlCommandLineToolProfile.class);
    }

    @Test
    public void serializationDeserializationTest(){
        XmlCommandLineToolProfile profile = new XmlCommandLineToolProfile();
        profile.setCommandTemplate("Hello, {name}");
        profile.getCommandOutputParser().setParsingLanguage("Groovy");
        profile.getCommandOutputParser().setReturnType(XmlCommandLineToolReturnType.TABLE);
        profile.getCommandOutputParser().addParsingRule("Prefix");
        profile.getCommandOutputParser().addTableColumn("key1", "value-exact", XmlCommandLineToolReturnType.STRING);
        profile.getCommandOutputParser().addLineTermination("termination");
        profile.getCommandOutputParser().addParsingRule("Postfix");
        String s = profile.toString();
        assertNotNull(s);
        assertTrue(s.startsWith("<?xml version"));
        s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<ns1:profile xmlns:ns1=\"http://itworks.com/snamp/command-line-tool-profile/v1.0\">\n" +
                "    <ns1:output ns1:language=\"JavaScript\" ns1:type=\"table\">Prefix<ns1:column ns1:name=\"key1\" ns1:type=\"string\">value-exact</ns1:column>\n" +
                "        <ns1:line-terminator>termination</ns1:line-terminator>Postfix</ns1:output>\n" +
                "    <ns1:input>Hello, {name}</ns1:input>\n" +
                "</ns1:profile>\n";
        profile = XmlCommandLineToolProfile.loadFrom(s);
        assertEquals("JavaScript", profile.getCommandOutputParser().getParsingLanguage());
        assertEquals("Hello, {name}", profile.getCommandTemplate());
        assertEquals("#,##0.###", profile.getCommandOutputParser().getNumberParsingFormat());
    }

    @Test
    public void scalarParsingUsingJavaScriptTest() throws ScriptException {
        final XmlCommandLineToolOutputParser parser = new XmlCommandLineToolOutputParser();
        parser.setParsingLanguage(XmlCommandLineToolOutputParser.JAVASCRIPT_LANG);
        //Test for Byte
        parser.setReturnType(XmlCommandLineToolReturnType.BYTE);
        parser.addParsingRule("stream.whitespaceChars(97, 98); stream.parseNumbers(); stream.parseByte();");
        final ScriptEngineManager manager = new ScriptEngineManager();
        Object result = parser.parse("abbaa123abbaa", manager);
        assertTrue(result instanceof Byte);
        assertEquals((byte) 123, result);
        //Test for BigInteger
        parser.setReturnType(XmlCommandLineToolReturnType.BIG_INTEGER);
        parser.removeParsingRules();
        parser.addParsingRule("stream.whitespaceChars(97, 98); stream.wordChars(48, 57); stream.parseBigInt();");
        result = parser.parse("abba1122264572456254624bbaaa", manager);
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger("1122264572456254624"), result);
        //Test for hexadecimal long
        parser.setReturnType(XmlCommandLineToolReturnType.LONG);
        parser.removeParsingRules();
        parser.addParsingRule("stream.whitespaceChars(97, 98); stream.wordChars(48, 57); stream.parseBigInt();");
        parser.setNumberParsingFormat("hex");
        result = parser.parse("abba20abba", manager);
        assertTrue(result instanceof Long);
        assertEquals(0x20L, result);
        //Test for float
        parser.setReturnType(XmlCommandLineToolReturnType.FLOAT);
        parser.removeParsingRules();
        parser.addParsingRule("stream.setupDefaultSyntax(); stream.whitespaceChars(97, 98); stream.parseFloat();");
        result = parser.parse("aabbaa20.1aabaa", manager);
        assertTrue(result instanceof Float);
        assertEquals(20.1F, result);
    }
}
