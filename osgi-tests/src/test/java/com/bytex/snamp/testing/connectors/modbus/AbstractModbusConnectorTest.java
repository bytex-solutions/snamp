package com.bytex.snamp.testing.connectors.modbus;

import com.google.common.collect.ImmutableMap;
import com.bytex.snamp.connectors.modbus.slave.*;
import com.bytex.snamp.connectors.modbus.transport.ModbusSlave;
import com.bytex.snamp.io.Buffers;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.AbstractResourceConnectorTest;
import org.osgi.framework.BundleContext;

import java.nio.ShortBuffer;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@SnampDependencies(SnampFeature.MODBUS_CONNECTOR)
public abstract class AbstractModbusConnectorTest extends AbstractResourceConnectorTest {
    protected static final String CONNECTOR_TYPE = "modbus";
    private ModbusSlave device;
    //virtual device image
    protected final DigitalInputAccessor DI_0 = () -> true;
    protected final DigitalInputAccessor DI_1 = () -> false;
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
    protected final InputRegisterAccessor IR_0 = (InputRegisterAccessor) () -> (short)42;
    protected final InputRegisterAccessor IR_1 = (InputRegisterAccessor) () -> (short)56;
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
    protected final FileRecordAccessor FI_0 = new FileRecordAccessor() {
        private final ShortBuffer record0 = Buffers.wrap((short)5, (short)6, (short)7, (short)8);
        private final ShortBuffer record1 = Buffers.wrap((short)8, (short)9, (short)10, (short)11);
        private final ShortBuffer record2 = Buffers.wrap((short)56, (short)57, (short)58, (short)67);

        @Override
        public ShortBuffer getRecord(final int recordNumber) {
            switch (recordNumber){
                case 0: return record0;
                case 1: return record1;
                case 2: return record2;
                default:return null;
            }
        }

        @Override
        public int getRecords() {
            return 3;
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
