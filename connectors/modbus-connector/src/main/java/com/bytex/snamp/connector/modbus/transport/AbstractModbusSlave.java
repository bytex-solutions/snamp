package com.bytex.snamp.connector.modbus.transport;

import com.bytex.snamp.connector.modbus.slave.*;
import com.ghgande.j2mod.modbus.ModbusCoupler;
import com.ghgande.j2mod.modbus.net.ModbusListener;
import com.ghgande.j2mod.modbus.procimg.*;
import com.google.common.primitives.Shorts;

import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.Objects;

/**
 * Represents an abstract class for building virtual Slave device.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class AbstractModbusSlave<L extends ModbusListener> implements ModbusSlave {
    static {
        ModbusCoupler.getReference().setMaster(false);
    }

    protected final L listener;
    private final SimpleProcessImage processImage;
    private Thread listenThread;

    protected AbstractModbusSlave(final L listener){
        this.listener = Objects.requireNonNull(listener);
        this.processImage = new SimpleProcessImage();
        ModbusCoupler.getReference().setProcessImage(processImage);
    }

    private static DigitalOut cast(final DigitalOutputAccessor out){
        return new DigitalOut() {
            @Override
            public boolean isSet() {
                return out.getValue();
            }

            @Override
            public void set(final boolean b) {
                out.setValue(b);
            }
        };
    }

    private static InputRegister cast(final InputRegisterAccessor input){
        return new InputRegister() {
            @Override
            public int getValue() {
                return toUnsignedShort();
            }

            @Override
            public int toUnsignedShort() {
                return toShort() & 0xFF_FF;
            }

            @Override
            public short toShort() {
                return input.getValue();
            }

            @Override
            public byte[] toBytes() {
                return Shorts.toByteArray(toShort());
            }
        };
    }

    private static Register cast(final OutputRegisterAccessor output){
        return new Register() {
            @Override
            public void setValue(int value) {
                value &= 0xFF_FF_FF_FF;
                setValue((short) value);
            }

            @Override
            public void setValue(final short s) {
                output.setValue(s);
            }

            @Override
            public void setValue(final byte[] bytes) {
                setValue(Shorts.fromByteArray(bytes));
            }

            @Override
            public int getValue() {
                return toUnsignedShort();
            }

            @Override
            public int toUnsignedShort() {
                return toShort() & 0xFF_FF;
            }

            @Override
            public short toShort() {
                return output.getValue();
            }

            @Override
            public byte[] toBytes() {
                return Shorts.toByteArray(output.getValue());
            }
        };
    }

    private static File cast(final int fileNumber, final FileRecordAccessor accessor){
        final File result = new File(fileNumber, accessor.getRecords());
        for(int recordIndex = 0; recordIndex < accessor.getRecords(); recordIndex++){
            final int recordSize = accessor.getRecord(recordIndex).limit();
            final Record rec = new Record(recordIndex, recordSize);
            result.setRecord(recordIndex, rec);
            //registers
            for(int register = 0; register < recordSize; register++){
                final ShortBuffer recordBuffer = accessor.getRecord(recordIndex);
                final int registerIndex = register;
                rec.setRegister(register, new Register() {
                    @Override
                    public void setValue(int value) {
                        value &= 0xFF_FF_FF_FF;
                        setValue((short)value);
                    }

                    @Override
                    public void setValue(final short value) {
                        recordBuffer.put(registerIndex, value);
                    }

                    @Override
                    public void setValue(final byte[] bytes) {
                        setValue(Shorts.fromByteArray(bytes));
                    }

                    @Override
                    public int getValue() {
                        return toUnsignedShort();
                    }

                    @Override
                    public int toUnsignedShort() {
                        return toShort() & 0xFF_FF;
                    }

                    @Override
                    public short toShort() {
                        return recordBuffer.get(registerIndex);
                    }

                    @Override
                    public byte[] toBytes() {
                        return Shorts.toByteArray(toShort());
                    }
                });
            }
        }
        return result;
    }

    @Override
    public final AbstractModbusSlave<L> register(final int ref, final DigitalOutputAccessor output){
        if(processImage.getDigitalOutCount() <= ref)
            processImage.addDigitalOut(cast(output));
        else
            processImage.setDigitalOut(ref, cast(output));
        return this;
    }

    @Override
    public final AbstractModbusSlave<L> register(final int ref, final DigitalInputAccessor input) {
        if(processImage.getDigitalInCount() <= ref)
            processImage.addDigitalIn(input::getValue);
        else
            processImage.setDigitalIn(ref, input::getValue);
        return this;
    }

    @Override
    public final AbstractModbusSlave<L> register(final int ref, final InputRegisterAccessor input) {
        if(processImage.getInputRegisterCount() <= ref)
            processImage.addInputRegister(cast(input));
        else
            processImage.setInputRegister(ref, cast(input));
        return this;
    }

    @Override
    public final AbstractModbusSlave<L> register(final int ref, final OutputRegisterAccessor output) {
        if(processImage.getRegisterCount() <= ref)
            processImage.addRegister(cast(output));
        else
            processImage.setRegister(ref, cast(output));
        return this;
    }

    @Override
    public final AbstractModbusSlave<L> register(final int fileNumber, final FileRecordAccessor file){
        if(processImage.getFileCount() <= fileNumber)
            processImage.addFile(cast(fileNumber, file));
        else
            processImage.setFile(fileNumber, cast(fileNumber, file));
        return this;
    }

    @Override
    public final void setUnitID(final int value){
        listener.setUnit(value);
        ModbusCoupler.getReference().setUnitID(value);
    }

    @Override
    public final boolean isListening(){
        return listenThread != null && listener.isListening();
    }

    public final void listen(){
        listenThread = new Thread(listener, "Modbus Slave Device");
        listenThread.start();
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public final void close() throws IOException {
        listener.stop();
        listenThread.interrupt();
        try {
            listenThread.join();
        } catch (final InterruptedException e) {
            throw new IOException(e);
        }
        finally {
            listenThread = null;
        }
    }
}
