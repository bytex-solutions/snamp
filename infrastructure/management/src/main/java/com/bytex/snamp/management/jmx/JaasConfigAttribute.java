package com.bytex.snamp.management.jmx;

import com.bytex.snamp.jmx.OpenMBean;
import com.google.common.base.Strings;

import javax.management.openmbean.SimpleType;
import java.io.*;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 */
final class JaasConfigAttribute extends OpenMBean.OpenAttribute<String, SimpleType<String>> {
    private static final String NAME = "jaasConfig";

    /**
     * Instantiates a new Jaas config attribute.
     *
     */
    JaasConfigAttribute() {
        super(NAME, SimpleType.STRING);
    }

    @Override
    public String getValue() throws IOException {
        try(final Writer out = new CharArrayWriter(1024)){
            SnampManagerImpl.dumpJaasConfiguration(getBundleContextOfObject(this), out);
            return out.toString();
        }
    }

    @Override
    public void setValue(final String content) throws IOException {
        if (Strings.isNullOrEmpty(content))
            SnampManagerImpl.saveJaasConfiguration(getBundleContextOfObject(this), null);
        else try (final Reader reader = new StringReader(content)) {
            SnampManagerImpl.saveJaasConfiguration(getBundleContextOfObject(this), reader);
        }
    }

    @Override
    protected String getDescription() {
        return "SNAMP JAAS Configuration";
    }
}