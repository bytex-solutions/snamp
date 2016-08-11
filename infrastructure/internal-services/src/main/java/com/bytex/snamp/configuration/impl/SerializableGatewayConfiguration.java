package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.GatewayConfiguration;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents gateway instance settings. This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.2
 * @version 2.0
 */
final class SerializableGatewayConfiguration extends AbstractEntityConfiguration implements GatewayConfiguration {
    private static final byte FORMAT_VERSION = 3;
    private static final long serialVersionUID = 7926704115151740217L;
    private String gatewayType;

    /**
     * Initializes a new empty gateway instance settings.
     */
    @SpecialUse
    public SerializableGatewayConfiguration(){
        gatewayType = "";
    }

    /**
     * Gets the type of gateway
     *
     * @return The type of gateway.
     */
    @Override
    public String getType() {
        return gatewayType;
    }

    /**
     * Sets the gateway type.
     *
     * @param gatewayType Type of gateway.
     */
    @Override
    public void setType(final String gatewayType) {
        markAsModified();
        this.gatewayType = gatewayType != null ? gatewayType : "";
    }

    private boolean equals(final GatewayConfiguration other){
        return getParameters().equals(other.getParameters());
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof GatewayConfiguration &&
                equals((GatewayConfiguration)other);
    }

    @Override
    public int hashCode() {
        return getParameters().hashCode() ^ gatewayType.hashCode();
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeByte(FORMAT_VERSION);
        out.writeUTF(gatewayType != null ? gatewayType : "");
        writeParameters(out);
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException    if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final byte version = in.readByte();
        if(version != FORMAT_VERSION)
            throw new IOException(String.format("Gateway configuration has invalid binary format version. Expected %s but actual %s", FORMAT_VERSION, version));
        gatewayType = in.readUTF();
        readParameters(in);
    }
}
