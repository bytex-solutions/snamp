package com.itworks.snamp.testing.connectors.snmp;

import com.itworks.snamp.testing.connectors.AbstractManagementConnectorTest;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.snmp4j.smi.OID;
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
    public void testForOctetStringPropertyParsing() throws TimeoutException, IOException {
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
    public void testForOctetStringProperty() throws TimeoutException, IOException {
        final String ATTRIBUTE_NAME = "1.3.6.1.2.1.1.9.1.3.1";
        //plain text
        testAttribute(ATTRIBUTE_NAME,
                String.class,
                "Frank Underwood",
                AbstractManagementConnectorTest.<String>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForOidProperty() throws TimeoutException, IOException {
        final String ATTRIBUTE_NAME = "1.3.6.1.2.1.1.2.0";
        //default format (int array)
        testAttribute(ATTRIBUTE_NAME,
                Object[].class,
                ArrayUtils.toObject(new OID("1.3.6.1.4.1.20408").getValue()),
                arrayEquator(),
                Collections.<String, String>emptyMap(),
                false);
        //text format
        testAttribute(ATTRIBUTE_NAME,
                String.class,
                "1.3.6.1.4.1.20408",
                AbstractManagementConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1) {{
                    put("snmpConversionFormat", "text");
                }},
                false);
    }

    @Test
    public void testForInteger32Property() throws TimeoutException, IOException {
        final String ATTRIBUTE_NAME = "1.3.6.1.2.1.1.7.0";
        testAttribute(ATTRIBUTE_NAME,
                Integer.class,
                72,
                AbstractManagementConnectorTest.<Integer>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForCount32Property() throws TimeoutException, IOException{
        final String ATTRIBUTE_NAME = "1.3.6.1.2.1.11.32.0";
        testAttribute(ATTRIBUTE_NAME,
                Long.class,
                0L,
                AbstractManagementConnectorTest.<Long>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForGauge32Property() throws TimeoutException, IOException{
        final String ATTRIBUTE_NAME = "1.3.6.1.2.1.2.2.1.5.1";
        testAttribute(ATTRIBUTE_NAME,
                Long.class,
                100000000L,
                AbstractManagementConnectorTest.<Long>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForTimeTicksProperty() throws TimeoutException, IOException{
        final String ATTRIBUTE_NAME = "1.3.6.1.2.1.1.9.1.4.1";
        //default - long time ticks
        testAttribute(ATTRIBUTE_NAME,
                Long.class,
                641835941L,
                AbstractManagementConnectorTest.<Long>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
        //text
        testAttribute(ATTRIBUTE_NAME,
                String.class,
                "74 days, 6:52:39.41",
                AbstractManagementConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1){{
                    put("snmpConversionFormat", "text");
                }},
                false);
    }
}
