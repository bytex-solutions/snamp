package com.bytex.snamp.connectors.modbus.transport;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;
import com.bytex.snamp.io.Buffers;

import java.nio.ShortBuffer;
import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.ArrayUtils.getFirst;

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

    private static void executeWithRetry(final ModbusTransaction transaction, int retryCount) throws ModbusException {
        while (retryCount-- > 0) {
            try {
                transaction.execute();
                return;
            } catch (final ModbusException e) {
                if (retryCount == 0) throw e;
            }
        }
    }

    private static <R extends ModbusResponse> R executeWithResponse(final ModbusTransaction transaction, final int retryCount, final Class<R> responseType) throws ModbusException {
        executeWithRetry(transaction, retryCount);
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
        final BitVector response = executeWithResponse(transaction, retryCount, ReadCoilsResponse.class).getCoils();
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
        executeWithRetry(transaction, retryCount);
    }

    @Override
    public final boolean writeCoil(final int unitID, final int ref, final boolean state) throws ModbusException {
        final ModbusTransaction transaction = createTransaction();
        transaction.setRetries(retryCount);
        final WriteCoilRequest request = new WriteCoilRequest(ref, state);
        request.setUnitID(unitID);
        transaction.setRequest(request);
        return executeWithResponse(transaction, retryCount, WriteCoilResponse.class).getCoil();
    }

    @Override
    public final BitVector readInputDiscretes(final int unitID, final int ref, final int count) throws ModbusException {
        final ModbusTransaction transaction = createTransaction();
        transaction.setRetries(retryCount);
        final ReadInputDiscretesRequest request = new ReadInputDiscretesRequest(ref, count);
        request.setUnitID(unitID);
        transaction.setRequest(request);
        final BitVector result = executeWithResponse(transaction, retryCount, ReadInputDiscretesResponse.class).getDiscretes();
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
        return executeWithResponse(transaction, retryCount, ReadInputRegistersResponse.class).getRegisters();
    }

    @Override
    public final InputRegister readInputRegister(final int unitID, final int ref) throws ModbusException {
        return getFirst(readInputRegisters(unitID, ref, 1));
    }

    @Override
    public final Register[] readHoldingRegisters(final int unitID, final int ref, final int count) throws ModbusException {
        final ModbusTransaction transaction = createTransaction();
        transaction.setRetries(retryCount);
        final ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(ref, count);
        request.setUnitID(unitID);
        transaction.setRequest(request);
        return executeWithResponse(transaction, retryCount, ReadMultipleRegistersResponse.class).getRegisters();
    }

    @Override
    public final Register readHoldingRegister(final int unitID, final int ref) throws ModbusException {
        return getFirst(readHoldingRegisters(unitID, ref, 1));
    }

    @Override
    public final void writeHoldingRegisters(final int unitID, final int ref, final Register[] regs) throws ModbusException {
        final ModbusTransaction transaction = createTransaction();
        transaction.setRetries(retryCount);
        final WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(ref, regs);
        request.setUnitID(unitID);
        transaction.setRequest(request);
        executeWithRetry(transaction, retryCount);
    }

    @Override
    public final void writeHoldingRegister(final int unitID, final int ref, final Register register) throws ModbusException {
        writeHoldingRegisters(unitID, ref, new Register[]{register});
    }

    @Override
    public final short[] readFile(final int unitID,
                                  final int file,
                                  final int recordCount,
                                  final int recordSize) throws ModbusException {
        final ShortBuffer buffer = Buffers.wrap(new short[recordCount * recordSize]);
        final ModbusTransaction transaction = createTransaction();
        transaction.setRetries(retryCount);
        for(int recordNumber = 0; recordNumber < recordCount; recordNumber++) {
            final ReadFileRecordRequest request = new ReadFileRecordRequest();
            request.addRequest(request.new RecordRequest(file, recordNumber, recordSize));
            request.setUnitID(unitID);
            transaction.setRequest(request);
            final ReadFileRecordResponse response = executeWithResponse(transaction, retryCount, ReadFileRecordResponse.class);
            if (response.getRecordCount() > 0) {
                final ReadFileRecordResponse.RecordResponse fileRecord = response.getRecord(0);
                for (int i = 0; i < fileRecord.getWordCount(); i++)
                    buffer.put(fileRecord.getRegister(i).toShort());
            } else return emptyArray(short[].class);
        }
        return buffer.array();
    }

    private void writeFile(final int unitID, final int file, final int recordSize, final ShortBuffer buffer) throws ModbusException {
        final ModbusTransaction transaction = createTransaction();
        transaction.setRetries(retryCount);
        int recordNumber = 0;
        while (buffer.hasRemaining()){
            final WriteFileRecordRequest request = new WriteFileRecordRequest();
            request.setUnitID(unitID);
            final short[] recordContent = new short[recordSize];
            for(int i = 0; i < recordSize && buffer.hasRemaining(); i++)
                recordContent[i] = buffer.get();
            final WriteFileRecordRequest.RecordRequest fileRecord =
                    request.new RecordRequest(file, recordNumber++, recordContent);
            request.addRequest(fileRecord);
            transaction.setRequest(request);
            executeWithRetry(transaction, retryCount);
        }
    }

    @Override
    public final void writeFile(final int unitID, final int file, final int recordSize, final short[] value) throws ModbusException {
        writeFile(unitID, file, recordSize, Buffers.wrap(value));
    }
}
