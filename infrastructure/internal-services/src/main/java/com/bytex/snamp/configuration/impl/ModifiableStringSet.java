package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ModifiableStringSet extends ModifiableHashSet<String> {
    private static final long serialVersionUID = 2060614950842015901L;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public ModifiableStringSet() {
    }

    @Override
    void writeItem(final String item, final ObjectOutput out) throws IOException {
        out.writeUTF(item);
    }

    @Override
    String readItem(final ObjectInput in) throws IOException, ClassNotFoundException {
        return in.readUTF();
    }
}
