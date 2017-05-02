package com.bytex.snamp.moa;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.io.SerializableSnapshotSupport;
import com.bytex.snamp.io.SerializedState;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.io.Serializable;

/**
 * Abstract class for constructing reservoirs.
 */
abstract class AbstractReservoir extends ThreadSafeObject implements Reservoir, SerializableSnapshotSupport<AbstractReservoir> {
    abstract static class ReservoirSnapshot<R extends AbstractReservoir> extends SerializedState<R> {
        private static final long serialVersionUID = -6080572664395210069L;
        private final int actualSize;

        ReservoirSnapshot(final int actualSize){
            this.actualSize = actualSize;
        }
    }

    int actualSize;

    AbstractReservoir() {
        super(SingleResourceGroup.class);
        actualSize = 0;
    }

    AbstractReservoir(final ReservoirSnapshot<?> snapshot) {
        super(SingleResourceGroup.class);
        actualSize = snapshot.actualSize;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void reset() {
        actualSize = 0;
    }

    @Override
    public abstract ReservoirSnapshot takeSnapshot();

    @Override
    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public final Object writeReplace() {
        return takeSnapshot();
    }

    final SafeCloseable acquireWriteLock(){
        return writeLock.acquireLock(SingleResourceGroup.INSTANCE);
    }

    final SafeCloseable acquireReadLock(){
        return readLock.acquireLock(SingleResourceGroup.INSTANCE);
    }

    @Override
    public final int getSize() {
        return actualSize;
    }

    static float getIndexForQuantile(final float q, final int length) {
        if (Float.compare(q, 0) == 0)
            return 0F;
        else if (Float.compare(q, 1) == 0)
            return length;
        else
            return (length + 1) * q;
    }
}
