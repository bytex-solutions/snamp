package com.itworks.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.facade.ModbusUDPMaster;
import com.ghgande.j2mod.modbus.net.ModbusMasterFactory;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.ResourceEventListener;

import java.util.logging.Logger;

/**
 * Represents Modbus connector.
 */
final class ModbusResourceConnector extends AbstractManagedResourceConnector implements Modbus {
    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {

    }

    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {

    }
}
