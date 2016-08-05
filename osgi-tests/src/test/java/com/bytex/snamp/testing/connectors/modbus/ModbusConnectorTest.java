package com.bytex.snamp.testing.connectors.modbus;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;
import com.bytex.snamp.connectors.modbus.transport.ModbusSlave;
import com.bytex.snamp.connectors.modbus.transport.ModbusTransportType;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.JMException;
import java.net.URISyntaxException;

import com.bytex.snamp.configuration.EntityMap;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class ModbusConnectorTest extends AbstractModbusConnectorTest {
    private static final int UNIT_ID = 10;

    public ModbusConnectorTest(){
        super("tcp://localhost:3967");
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Override
    protected ModbusSlave createSlaveDevice() {
        final ModbusSlave result =  ModbusTransportType.TCP.createSlave(3967);
        result.setUnitID(UNIT_ID);
        return result
                .register(0, DI_0)
                .register(1, DI_1)
                .register(0, DO_0)
                .register(0, IR_0)
                .register(1, IR_1)
                .register(0, OR_0)
                .register(0, FI_0);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }

    @Test
    public void coilReadWriteTest() throws JMException{
        testAttribute("C_0", TypeToken.of(Boolean.class), true);
    }

    @Test
    public void inputDiscreteReadTest() throws JMException{
        testAttribute("ID_0", TypeToken.of(Boolean.class), DI_0.getValue(), true);
        testAttribute("ID_1", TypeToken.of(Boolean.class), DI_1.getValue(), true);
    }

    @Test
    public void inputDiscreteBatchReadTest() throws JMException {
        testAttribute("ID_01",
                TypeToken.of(boolean[].class),
                new boolean[]{DI_0.getValue(), DI_1.getValue()},
                ArrayUtils::strictEquals,
                true);
    }

    @Test
    public void inputRegisterReadTest() throws JMException, URISyntaxException {
        testAttribute("IR_0", TypeToken.of(Short.class), IR_0.getValue(), true);
        testAttribute("IR_1", TypeToken.of(Short.class), IR_1.getValue(), true);
    }

    @Test
    public void inputRegisterBatchReadTest() throws JMException {
        testAttribute("IR_01",
                TypeToken.of(short[].class),
                new short[]{IR_0.getValue(), IR_1.getValue()},
                ArrayUtils::strictEquals,
                true);
    }

    @Test
    public void registerReadWriteTest() throws JMException{
        testAttribute("OR_0", TypeToken.of(Short.class), (short)97);
    }

    @Test
    public void fileReadWriteTest() throws JMException {
        final short[] array = new short[]{4, 6, 7, 10, //record 0
                15, 89, 34, 33,          //record 1
                78, 0, 12, -56};                //record 2
        testAttribute("FI_0", TypeToken.of(short[].class), array, ArrayUtils::strictEquals);
    }

    @Test
    public void configurationDescriptionTest(){
        testConfigurationDescriptor(ManagedResourceConfiguration.class, ImmutableSet.of(
                "connectionTimeout",
                "retryCount"
        ));
        testConfigurationDescriptor(AttributeConfiguration.class, ImmutableSet.of(
                "offset",
                "count",
                "recordSize",
                "unitID"
        ));
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attr = attributes.getOrAdd("ID_0");
        setFeatureName(attr, "inputDiscrete");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "0");

        attr = attributes.getOrAdd("ID_1");
        setFeatureName(attr, "inputDiscrete");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "1");

        attr = attributes.getOrAdd("C_0");
        setFeatureName(attr, "coil");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "0");

        attr = attributes.getOrAdd("ID_01");
        setFeatureName(attr, "inputDiscrete");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "0");
        attr.getParameters().put("count", "2");

        attr = attributes.getOrAdd("IR_0");
        setFeatureName(attr, "inputRegister");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "0");

        attr = attributes.getOrAdd("IR_1");
        setFeatureName(attr, "inputRegister");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "1");

        attr = attributes.getOrAdd("IR_01");
        setFeatureName(attr, "inputRegister");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "0");
        attr.getParameters().put("count", "2");

        attr = attributes.getOrAdd("OR_0");
        setFeatureName(attr, "holdingRegister");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "0");

        attr = attributes.getOrAdd("FI_0");
        setFeatureName(attr, "file");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "0");
        attr.getParameters().put("count", "3");
        attr.getParameters().put("recordSize", "4");
    }
}
