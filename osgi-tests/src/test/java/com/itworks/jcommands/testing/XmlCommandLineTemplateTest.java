package com.itworks.jcommands.testing;

import com.google.common.collect.ImmutableMap;
import com.itworks.jcommands.impl.XmlCommandLineTemplate;
import com.itworks.jcommands.impl.XmlCommandLineToolProfile;
import com.itworks.jcommands.impl.XmlParserDefinition;
import com.itworks.jcommands.impl.XmlParsingResultType;
import com.itworks.snamp.jmx.CompositeDataBuilder;
import com.itworks.snamp.jmx.TabularDataBuilder;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class XmlCommandLineTemplateTest extends AbstractUnitTest<XmlCommandLineTemplate> {

    @Test
    public void serializationTest() throws IOException {
        final XmlCommandLineToolProfile profile = new XmlCommandLineToolProfile();
        profile.saveTo(new File("/home/roman/free-tool.xml"));
        try(final OutputStream s = new ByteArrayOutputStream(4096)){
            profile.saveTo(s);
        }
    }

    @Test
    public void renderDictionaryTest() throws OpenDataException {
        final XmlCommandLineTemplate template = new XmlCommandLineTemplate();
        template.setCommandTemplate("{dict.key1}, {dict.key2}");
        final CompositeData dict = new CompositeDataBuilder()
                .setTypeName("dict")
                .setTypeDescription("desr")
                .put("key1", "key1", "Hello")
                .put("key2", "key2", "world")
                .build();
        final String result = template.renderCommand(ImmutableMap.of("dict", dict));
        assertEquals("Hello, world", result);
    }

    @Test
    public void renderTableTest() throws OpenDataException {
        final XmlCommandLineTemplate template = new XmlCommandLineTemplate();
        template.setCommandTemplate("{table:{x | {x.column1} = {x.column2}}}");
        final TabularData table = new TabularDataBuilder()
                .columns()
                .addColumn("column1", "column1", SimpleType.STRING, true)
                .addColumn("column2", "column2", SimpleType.INTEGER, false)
                .queryObject(TabularDataBuilder.class)
                .add("A", 42)
                .add("B", 43)
                .build();
        final String result = template.renderCommand(ImmutableMap.of("table", table));
        assertEquals("A = 42B = 43", result);
    }

    @Test
    public void constantTest() throws ScriptException{
        final XmlParserDefinition parser = new XmlParserDefinition();
        parser.setParsingLanguage(XmlParserDefinition.JAVASCRIPT_LANG);
        parser.setParsingResultType(XmlParsingResultType.FLOAT);
        parser.addConstantDef("3.14");
        final Object result = parser.parse("fkneknehg", new ScriptEngineManager());
        assertTrue(result instanceof Float);
        assertEquals(3.14F, result);
    }

    @Test
    public void arrayParsingUsingJavaScriptTest() throws ScriptException{
        final XmlParserDefinition parser = new XmlParserDefinition();
        parser.setParsingResultType(XmlParsingResultType.ARRAY);
        parser.setParsingLanguage(XmlParserDefinition.JAVASCRIPT_LANG);
        parser.addParsingRule("scan.next('[a-z]+');");
        parser.addArrayItem("scan.nextByte();", XmlParsingResultType.BYTE);
        parser.addLineTermination("scan.next('[a-z]+');");
        final ScriptEngineManager manager = new ScriptEngineManager();
        final Object result = parser.parse("ab 1 ba ab 2 cd ef 3 gh", manager);
        assertTrue(result instanceof Byte[]);
        assertArrayEquals(new Byte[]{1, 2, 3}, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void tableParsingUsingJavaScriptTest() throws ScriptException{
        final XmlParserDefinition parser = new XmlParserDefinition();
        parser.setParsingResultType(XmlParsingResultType.TABLE);
        parser.setParsingLanguage(XmlParserDefinition.JAVASCRIPT_LANG);
        parser.addParsingRule("scan.next('[a-z]+');");
        parser.addTableColumn("col1", "scan.nextInt();", XmlParsingResultType.INTEGER);
        parser.addTableColumn("col2", "scan.nextBoolean();", XmlParsingResultType.BOOLEAN);
        parser.addLineTermination("scan.next('[a-z]+');");
        final List<Map<String, Object>> result = (List<Map<String, Object>>)parser.parse("prefix 42 true postfix prefix 43 false postfix", new ScriptEngineManager());
        assertEquals(2, result.size());
        assertEquals(42, result.get(0).get("col1"));
        assertEquals(Boolean.TRUE, result.get(0).get("col2"));
    }

    @Test
    public void dictionaryParsingUsingJavaScriptTest() throws ScriptException{
        final XmlParserDefinition parser = new XmlParserDefinition();
        parser.setParsingResultType(XmlParsingResultType.DICTIONARY);
        parser.setParsingLanguage(XmlParserDefinition.JAVASCRIPT_LANG);
        parser.addParsingRule("scan.next('[a-z]+');");
        parser.addDictionaryEntryRule("key1", "scan.nextLong();", XmlParsingResultType.LONG);
        parser.addDictionaryEntryRule("key2", "scan.next('hello');", XmlParsingResultType.STRING);
        parser.addDictionaryEntryRule("key3", "scan.nextBoolean();", XmlParsingResultType.BOOLEAN);
        final Object result = parser.parse("ab 42 hello true", new ScriptEngineManager());
        assertTrue(result instanceof Map);
        assertEquals(3, ((Map)result).size());
        assertEquals(42L, ((Map)result).get("key1"));
        assertEquals("hello", ((Map)result).get("key2"));
        assertEquals(Boolean.TRUE, ((Map)result).get("key3"));
    }


    @Test
    public void scalarParsingUsingJavaScriptTest() throws ScriptException {
        final XmlParserDefinition parser = new XmlParserDefinition();
        parser.setParsingLanguage(XmlParserDefinition.JAVASCRIPT_LANG);
        //Test for Byte
        parser.setParsingResultType(XmlParsingResultType.BYTE);
        parser.addParsingRule("scan.useDelimiter('[a-z]+').nextByte();");
        final ScriptEngineManager manager = new ScriptEngineManager();
        Object result = parser.parse("abbaa123abbaa", manager);
        assertTrue(result instanceof Byte);
        assertEquals(123, result);
        //Test for BigInteger
        parser.setParsingResultType(XmlParsingResultType.BIG_INTEGER);
        parser.removeParsingRules();
        parser.addParsingRule("scan.useDelimiter('[a-z]+').nextBigInteger();");
        result = parser.parse("abba1122264572456254624bbaaa", manager);
        assertTrue(result instanceof BigInteger);
        assertEquals(new BigInteger("1122264572456254624"), result);
        //Test for hexadecimal long
        parser.setParsingResultType(XmlParsingResultType.LONG);
        parser.removeParsingRules();
        parser.addParsingRule("scan.useDelimiter('[a-z]+').next('[0-9]+');");
        parser.setNumberParsingFormat("hex");
        result = parser.parse("abba20abba", manager);
        assertTrue(result instanceof Long);
        assertEquals(0x20L, result);
        //Test for float
        parser.setParsingResultType(XmlParsingResultType.FLOAT);
        parser.removeParsingRules();
        parser.addParsingRule("scan.useDelimiter('[a-z]+').nextFloat();");
        result = parser.parse("aabbaa20.1aabaa", manager);
        assertTrue(result instanceof Float);
        assertEquals(20.1F, result);
        //Test for parsing BLOB in HEX format
        parser.setParsingResultType(XmlParsingResultType.BLOB);
        parser.removeParsingRules();
        parser.addParsingRule("scan.next('[0-9]+');");
        result = parser.parse("20", manager);
        assertTrue(result instanceof Byte[]);
        assertArrayEquals(new Byte[]{0x20}, (Byte[])result);
    }

    @Test
    public void scalarParsingUsingRegexpTest() throws ScriptException {
        final XmlParserDefinition parser = new XmlParserDefinition();
        parser.setParsingLanguage(XmlParserDefinition.REGEXP_LANG);
        //INT test
        parser.setParsingResultType(XmlParsingResultType.INTEGER);
        parser.skipToken("[a-z]+");
        parser.addParsingRule("[0-9]+");
        Object result = parser.parse("abbba 90", new ScriptEngineManager());
        assertTrue(result instanceof Integer);
        assertEquals(90, result);
        //BOOLEAN test
        parser.setParsingResultType(XmlParsingResultType.BOOLEAN);
        parser.removeParsingRules();
        parser.skipToken("[a-z]+");
        parser.addParsingRule("[a-z]+");
        result = parser.parse("prefix true", new ScriptEngineManager());
        assertTrue(result instanceof Boolean);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void dictionaryParsingUsingRegexp() throws ScriptException {
        final XmlParserDefinition parser = new XmlParserDefinition();
        parser.setParsingLanguage(XmlParserDefinition.REGEXP_LANG);
        parser.setParsingResultType(XmlParsingResultType.DICTIONARY);
        parser.skipToken("[a-z]+");
        parser.addDictionaryEntryRule("key1", "[0-9]+", XmlParsingResultType.LONG);
        parser.skipToken("[a-z]+");
        parser.addDictionaryEntryRule("key2", "true", XmlParsingResultType.BOOLEAN);
        final Object dict = parser.parse("abba 90 middle true", new ScriptEngineManager());
        assertTrue(dict instanceof Map);
        assertEquals(90L, ((Map)dict).get("key1"));
        assertEquals(Boolean.TRUE, ((Map)dict).get("key2"));
    }

    @Test
    public void arrayParsingRule() throws ScriptException{
        final XmlParserDefinition parser = new XmlParserDefinition();
        parser.setParsingLanguage(XmlParserDefinition.REGEXP_LANG);
        parser.setParsingResultType(XmlParsingResultType.ARRAY);
        parser.skipToken("[a-z]+");
        parser.addArrayItem("[0-9]+", XmlParsingResultType.FLOAT);
        parser.addLineTermination("[a-z]*");
        final Object array = parser.parse("ab 1 ba ac 2 da ad 3", new ScriptEngineManager());
        assertTrue(array instanceof Float[]);
        assertArrayEquals(new Float[]{1F, 2F, 3F}, array);
    }
}
