package com.itworks.jcommands.testing;

import com.itworks.jcommands.impl.XmlCommandLineToolOutputParser;
import com.itworks.jcommands.impl.XmlCommandLineToolProfile;
import com.itworks.jcommands.impl.XmlCommandLineToolReturnType;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

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
    public void arrayParsingUsingJavaScriptTest() throws ScriptException{
        final XmlCommandLineToolOutputParser parser = new XmlCommandLineToolOutputParser();
        parser.setReturnType(XmlCommandLineToolReturnType.ARRAY);
        parser.setParsingLanguage(XmlCommandLineToolOutputParser.JAVASCRIPT_LANG);
        parser.addParsingRule("if(!this.initialized){ stream.setupDefaultSyntax(); this.initialized = true; }; stream.parseWord();");
        parser.addArrayItem("stream.parseByte();", XmlCommandLineToolReturnType.BYTE);
        parser.addLineTermination("stream.parseWord();");
        final ScriptEngineManager manager = new ScriptEngineManager();
        final Object result = parser.parse("ab 1 ba ab 2 cd ef 3 gh", manager);
        assertTrue(result instanceof Byte[]);
        assertArrayEquals(new Byte[]{1, 2, 3}, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void tableParsingUsingJavaScriptTest() throws ScriptException{
        final XmlCommandLineToolOutputParser parser = new XmlCommandLineToolOutputParser();
        parser.setReturnType(XmlCommandLineToolReturnType.TABLE);
        parser.setParsingLanguage(XmlCommandLineToolOutputParser.JAVASCRIPT_LANG);
        parser.addParsingRule("if(!this.initialized){stream.setupDefaultSyntax(); this.initialized = true; }; stream.parseWord();");
        parser.addTableColumn("col1", "stream.parseInt();", XmlCommandLineToolReturnType.INTEGER);
        parser.addTableColumn("col2", "stream.parseWord();", XmlCommandLineToolReturnType.BOOLEAN);
        parser.addLineTermination("stream.parseWord();");
        final List<Map<String, Object>> result = (List<Map<String, Object>>)parser.parse("prefix 42 true postfix prefix 43 false postfix", new ScriptEngineManager());
        assertEquals(2, result.size());
        assertEquals(42, result.get(0).get("col1"));
        assertEquals(Boolean.TRUE, result.get(0).get("col2"));
    }

    @Test
    public void dictionaryParsingUsingJavaScriptTest() throws ScriptException{
        final XmlCommandLineToolOutputParser parser = new XmlCommandLineToolOutputParser();
        parser.setReturnType(XmlCommandLineToolReturnType.DICTIONARY);
        parser.setParsingLanguage(XmlCommandLineToolOutputParser.JAVASCRIPT_LANG);
        parser.addParsingRule("stream.setupDefaultSyntax();stream.parseWord();");
        parser.addDictionaryEntryRule("key1", "stream.parseLong();", XmlCommandLineToolReturnType.LONG);
        parser.addDictionaryEntryRule("key2", "stream.parseWord();", XmlCommandLineToolReturnType.STRING);
        parser.addDictionaryEntryRule("key3", "stream.parseBoolean();", XmlCommandLineToolReturnType.BOOLEAN);
        final Object result = parser.parse("ab 42 hello true", new ScriptEngineManager());
        assertTrue(result instanceof Map);
        assertEquals(3, ((Map)result).size());
        assertEquals(42L, ((Map)result).get("key1"));
        assertEquals("hello", ((Map)result).get("key2"));
        assertEquals(Boolean.TRUE, ((Map)result).get("key3"));
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
        //Test for parsing BLOB in HEX format
        parser.setReturnType(XmlCommandLineToolReturnType.BLOB);
        parser.removeParsingRules();
        parser.addParsingRule("stream.wordChars(48, 57); stream.parseWord();");
        result = parser.parse("20", manager);
        assertTrue(result instanceof Byte[]);
        assertArrayEquals(new Byte[]{0x20}, (Byte[])result);
    }
}
