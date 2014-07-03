package com.itworks.snamp.testing.connectors.snmp;

import com.itworks.snamp.testing.connectors.AbstractManagementConnectorTest;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.snmp4j.smi.OctetString;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Represents SNMPv2 test that uses demo.snmplabs.com simulator.
 * <p>
 *     snmpwalk -v 2c -c public demo.snmplabs.com:1161
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class PublicSnmpV2ConnectorTest extends AbstractSnmpConnectorTest {
    private static Map<String, String> getParameters(){
        final Map<String, String> params = new HashMap<>(1);
        params.put("community", "public");
        return params;
    }

    public PublicSnmpV2ConnectorTest(){
        super("demo.snmplabs.com", 1161, getParameters());
    }



    @Test
    public void testForStringReadOnlyProperty() throws TimeoutException, IOException {
        final String ATTRIBUTE_NAME = "1.3.6.1.2.1.1.4.0";
        //plain text
        testAttribute(ATTRIBUTE_NAME,
                String.class,
                "SNMP Laboratories, info@snmplabs.com",
                AbstractManagementConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1) {{
                    put("snmpConversionFormat", "text");
                }},
                true);
        //hexadecimal test
        testAttribute(ATTRIBUTE_NAME,
                String.class,
                new OctetString("SNMP Laboratories, info@snmplabs.com").toHexString(),
                AbstractManagementConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1){{
                    put("snmpConversionFormat", "hex");
                }},
                true);
        //byte array test
        testAttribute(ATTRIBUTE_NAME,
                Object[].class,
                ArrayUtils.toObject(new OctetString("SNMP Laboratories, info@snmplabs.com").toByteArray()),
                arrayEquator(),
                new HashMap<String, String>(1){{
                    put("snmpConversionFormat", "raw");
                }},
                true);
    }

    @Test
    public void testForReadWriteStringProperty() throws TimeoutException, IOException {
        final String ATTRIBUTE_NAME = "1.3.6.1.2.1.1.9.1.3.1";
        //plain text
        testAttribute(ATTRIBUTE_NAME,
                String.class,
                "Frank Underwood",
                AbstractManagementConnectorTest.<String>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }
}
