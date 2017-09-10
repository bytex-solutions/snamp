package com.bytex.snamp.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.function.Function;

/**
 * Represents serialization mode.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class SerializationMode {
    /**
     * Default serialization mode without customization.
     */
    static final SerializationMode DEFAULT = new SerializationMode();

    protected SerializationMode(){

    }

    /**
     * Obtains serialization mode with customized object replacement.
     * @param typeToReplace The type to replace.
     * @param replacement Replacement function.
     * @param <T> The type to replace.
     * @return Serialization mode.
     */
    public static <T> SerializationMode objectReplacement(final Class<T> typeToReplace,
                                                          final Function<? super T, ? extends Serializable> replacement) {
        final class CustomObjectOutputStream extends ObjectOutputStream {
            CustomObjectOutputStream(final OutputStream out) throws IOException {
                super(out);
                enableReplaceObject(true);
            }

            @Override
            protected Object replaceObject(final Object obj) throws IOException {
                return typeToReplace.isInstance(obj) ? replacement.apply(typeToReplace.cast(obj)) : obj;
            }
        }

        return new SerializationMode() {
            @Override
            public ObjectOutputStream createStream(final OutputStream output) throws IOException {
                return new CustomObjectOutputStream(output);
            }
        };
    }

    public ObjectOutputStream createStream(final OutputStream output) throws IOException{
        return new ObjectOutputStream(output);
    }
}
