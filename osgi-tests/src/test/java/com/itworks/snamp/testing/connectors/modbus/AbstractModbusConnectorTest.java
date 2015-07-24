package com.itworks.snamp.testing.connectors.modbus;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.connectors.modbus.slave.DigitalInputAccessor;
import com.itworks.snamp.connectors.modbus.slave.DigitalOutputAccessor;
import com.itworks.snamp.connectors.modbus.slave.InputRegisterAccessor;
import com.itworks.snamp.connectors.modbus.slave.OutputRegisterAccessor;
import com.itworks.snamp.connectors.modbus.transport.ModbusSlave;
import com.itworks.snamp.testing.SnampDependencies;
import com.itworks.snamp.testing.SnampFeature;
import com.itworks.snamp.testing.connectors.AbstractResourceConnectorTest;
import org.osgi.framework.BundleContext;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.MODBUS_CONNECTOR)
public abstract class AbstractModbusConnectorTest extends AbstractResourceConnectorTest {
    protected static final String CONNECTOR_TYPE = "modbus";
    private ModbusSlave device;
    //virtual device image
    protected final DigitalInputAccessor DI_0 = new DigitalInputAccessor() {
        @Override
        public boolean getValue() {
            return true;
        }
    };
    protected final DigitalInputAccessor DI_1 = new DigitalInputAccessor() {
        @Override
        public boolean getValue() {
            return false;
        }
    };
    protected final DigitalOutputAccessor DO_0 = new DigitalOutputAccessor() {
        private volatile boolean value;

        @Override
        public void setValue(final boolean value) {
            this.value = value;
        }

        @Override
        public boolean getValue() {
            return value;
        }
    };
    protected final InputRegisterAccessor IR_0 = new InputRegisterAccessor() {
        @Override
        public short getValue() {
            return 42;
        }
    };
    protected final InputRegisterAccessor IR_1 = new InputRegisterAccessor() {
        @Override
        public short getValue() {
            return 56;
        }
    };
    protected final OutputRegisterAccessor OR_0 = new OutputRegisterAccessor() {
        private volatile short register;

        @Override
        public void setValue(final short value) {
            register = value;
        }

        @Override
        public short getValue() {
            return register;
        }
    };

    protected AbstractModbusConnectorTest(final String connectionString){
        super(CONNECTOR_TYPE, connectionString, ImmutableMap.of("retryCount", "10"));
    }

    protected abstract ModbusSlave createSlaveDevice();

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        device = createSlaveDevice();
        assertNotNull(device);
        device.listen();
        int count = 1000;
        while (!device.isListening() && count > 0){
            Thread.sleep(100);
            count -= 1;
        }
        assertTrue("Modbus master timeout", count > 0);
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        device.close();
        assertFalse(device.isListening());
        device = null;
    }
}
