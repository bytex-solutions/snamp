package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.SpecialUse;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

/**
 * Represents replica of data stream connector.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
public class Replica implements Externalizable {
    private static final long serialVersionUID = 1773952916599839148L;
    private final SyntheticAttributeRepositoryReplica attributes;
    private SpanArrivalsRecorder arrivals;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public Replica(){
        attributes = new SyntheticAttributeRepositoryReplica();
    }

    final void addToReplica(final Iterable<SyntheticAttribute> attributes){
        this.attributes.init(attributes);
    }

    final void addToReplica(final SpanArrivalsRecorder arrivals){
        this.arrivals = arrivals.clone();
    }

    final void restoreFromReplica(final Iterable<SyntheticAttribute> attributes){
        this.attributes.restore(attributes);
    }

    final SpanArrivalsRecorder restoreFromReplica(){
        return arrivals.clone();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        attributes.writeExternal(out);
        final boolean hasArrivals = Objects.nonNull(arrivals);
        out.writeBoolean(hasArrivals);
        if (hasArrivals)
            out.writeObject(arrivals);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        attributes.readExternal(in);
        final boolean hasArrivals = in.readBoolean();
        if (hasArrivals)
            arrivals = (SpanArrivalsRecorder) in.readObject();
    }
}
