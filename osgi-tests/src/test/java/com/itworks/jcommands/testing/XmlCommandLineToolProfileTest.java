package com.itworks.jcommands.testing;

import com.itworks.jcommands.impl.XmlCommandLineToolOutputParser;
import com.itworks.jcommands.impl.XmlCommandLineToolProfile;
import com.itworks.jcommands.impl.XmlCommandLineToolReturnType;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
        final String s = profile.toString();
        assertNotNull(s);
        assertTrue(s.startsWith("<?xml version"));
    }

    @Test
    public void dummyTest() throws ScriptException {
        XmlCommandLineToolProfile profile = new XmlCommandLineToolProfile();
        profile.getCommandOutputParser().setParsingLanguage(XmlCommandLineToolOutputParser.JAVASCRIPT_LANG);
        profile.getCommandOutputParser().addParsingRule("contr.print();");
        final Object r = profile.getCommandOutputParser().parse("123", new ScriptEngineManager());
    }
}
