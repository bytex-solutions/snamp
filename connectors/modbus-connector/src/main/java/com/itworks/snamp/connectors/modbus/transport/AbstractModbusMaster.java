package com.itworks.snamp.connectors.modbus.transport;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransport;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;

/**
 * Represents an abstract class for all Modbus master apps.
 */
abstract class AbstractModbusMaster implements ModbusMaster {
    private int retryCount = 3;

    protected abstract ModbusTransaction createTransaction();

    @Override
    public final void setRetryCount(final int value){
        retryCount = value;
    }

    private static <R extends ModbusResponse> R executeWithResponse(final ModbusTransaction transaction, final Class<R> responseType) throws ModbusException {
        transaction.execute();
        final ModbusResponse response = transaction.getResponse();
        if(responseType.isInstance(response))
            return responseType.cast(response);
        else throw new ModbusException("Unexpected response " + response);
    }

    @Override
    public final BitVector readCoils(final int unitID, final int ref, final int count) throws ModbusException {
        final ModbusTransaction transaction = createTransaction();
        transaction.setRetries(retryCount);
        final ReadCoilsRequest request = new ReadCoilsRequest();
        request.setReference(ref);
        request.setUnitID(unitID);
        request.setBitCount(count);
        transaction.setRequest(request);
        final BitVector response = executeWithResponse(transaction, ReadCoilsResponse.class).getCoils();
        response.forceSize(count);
        return response;
    }

    @Override
    public final boolean readCoil(final int unitID, final int ref) throws ModbusException{
        return readCoils(unitID, ref, 1).getBit(0);
    }

    @Override
    public final void writeCoils(final int unitID, final int ref, final BitVector coils) throws ModbusException {
        final ModbusTransaction transaction = createTransaction();
        transaction.setRetries(retryCount);
        final WriteMultipleCoilsRequest request = new WriteMultipleCoilsRequest(ref, coils);
        request.setUnitID(unitID);
        transaction.setRequest(request);
        transaction.execute();
    }

    @Override
    public final boolean writeCoil(final int unitID, final int ref, final boolean state) throws ModbusException {
        final ModbusTransaction transaction = createTransaction();
        transaction.setRetries(retryCount);
        final WriteCoilRequest request = new WriteCoilRequest(ref, state);
        request.setUnitID(unitID);
        transaction.setRequest(request);
        return executeWithResponse(transaction, WriteCoilResponse.class).getCoil();
    }

    @Override
    public final BitVector readInputDiscretes(final int unitID, final int ref, final int count) throws ModbusException {
        final ModbusTransaction transaction = createTransaction();
        transaction.setRetries(retryCount);
        final ReadInputDiscretesRequest request = new ReadInputDiscretesRequest(ref, count);
        request.setUnitID(unitID);
        transaction.setRequest(request);
        final BitVector result = executeWithResponse(transaction, ReadInputDiscretesResponse.class).getDiscretes();
        result.forceSize(count);
        return result;
    }

    @Override
    public final boolean readInputDiscrete(final int unitID, final int ref) throws ModbusException {
        return readInputDiscretes(unitID, ref, 1).getBit(0);
    }

    @Override
    public final InputRegister[] readInputRegisters(final int unitID, final int ref, final int count) throws ModbusException {
        final ModbusTransaction transaction = createTransaction();
        transaction.setRetries(retryCount);
        final ReadInputRegistersRequest request = new ReadInputRegistersRequest(ref, count);
        request.setUnitID(unitID);
        transaction.setRequest(request);
        return executeWithResponse(transaction, ReadInputRegistersResponse.class).getRegisters();
    }

    @Override
    public final InputRegister readInputRegister(final int unitID, final int ref) throws ModbusException {
        return readInputRegisters(unitID, ref, 1)[0];
    }

    @Override
    public final Register[] readHoldingRegisters(final int unitID, final int ref, final int count) throws ModbusException {
        final ModbusTransaction transaction = createTransaction();
        transaction.setRetries(retryCount);
        final ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(ref, count);
        request.setUnitID(unitID);
        transaction.setRequest(request);
        return executeWithResponse(transaction, ReadMultipleRegistersResponse.class).getRegisters();
    }

    @Override
    public final Register readHoldingRegister(final int unitID, final int ref) throws ModbusException {
        return readHoldingRegisters(unitID, ref, 1)[0];
    }

    @Override
    public final void writeHoldingRegisters(final int unitID, final int ref, final Register[] regs) throws ModbusException {
        final ModbusTransaction transaction = createTransaction();
        transaction.setRetries(retryCount);
        final WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(ref, regs);
        request.setUnitID(unitID);
        transaction.setRequest(request);
        transaction.execute();
    }

    @Override
    public final void writeHoldingRegister(final int unitID, final int ref, final Register register) throws ModbusException {
        writeHoldingRegisters(unitID, ref, new Register[]{register});
    }
}
