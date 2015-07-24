package com.itworks.snamp.testing.connectors.modbus;

import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;
import com.itworks.snamp.TypeTokens;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.modbus.slave.DigitalInputAccessor;
import com.itworks.snamp.connectors.modbus.transport.ModbusSlave;
import com.itworks.snamp.connectors.modbus.transport.ModbusTransportType;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.Attribute;
import javax.management.JMException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ModbusConnectorTest extends AbstractModbusConnectorTest {
    private static final int UNIT_ID = 10;

    public ModbusConnectorTest(){
        super("tcp://localhost:3967");
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
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
                .register(0, OR_0);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }

    @Test
    public void coilReadWriteTest() throws JMException{
        testAttribute("C_0", TypeTokens.BOOLEAN, true);
    }

    @Test
    public void inputDiscreteReadTest() throws JMException{
        testAttribute("ID_0", TypeTokens.BOOLEAN, DI_0.getValue(), true);
        testAttribute("ID_1", TypeTokens.BOOLEAN, DI_1.getValue(), true);
    }

    @Test
    public void inputDiscreteBatchReadTest() throws JMException {
        testAttribute("ID_01",
                TypeToken.of(boolean[].class),
                new boolean[]{DI_0.getValue(), DI_1.getValue()},
                AbstractModbusConnectorTest.<boolean[]>arrayEquator(),
                true);
    }

    @Test
    public void inputRegisterReadTest() throws JMException, URISyntaxException {
        testAttribute("IR_0", TypeTokens.SHORT, IR_0.getValue(), true);
        testAttribute("IR_1", TypeTokens.SHORT, IR_1.getValue(), true);
    }

    @Test
    public void inputRegisterBatchReadTest() throws JMException {
        testAttribute("IR_01",
                TypeToken.of(short[].class),
                new short[]{IR_0.getValue(), IR_1.getValue()},
                AbstractModbusConnectorTest.<short[]>arrayEquator(),
                true);
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes,
                                  final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attr = attributeFactory.get();
        attr.setAttributeName("inputDiscrete");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "0");
        attributes.put("ID_0", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("inputDiscrete");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "1");
        attributes.put("ID_1", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("coil");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "0");
        attributes.put("C_0", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("inputDiscrete");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "0");
        attr.getParameters().put("count", "2");
        attributes.put("ID_01", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("inputRegister");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "0");
        attributes.put("IR_0", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("inputRegister");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "1");
        attributes.put("IR_1", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("inputRegister");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "0");
        attr.getParameters().put("count", "2");
        attributes.put("IR_01", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("holdingRegister");
        attr.getParameters().put("unitID", Integer.toString(UNIT_ID));
        attr.getParameters().put("offset", "0");
        attributes.put("OR_0", attr);
    }
}
