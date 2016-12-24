package com.bytex.snamp.web.data;

import com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor.Action;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class SimpleDocumentDatabase<M> implements DocumentDatabase<M> {
    private final File databasePath;
    private final AbstractConcurrentResourceAccessor<Map<String, M>> cachedObjects;

    public SimpleDocumentDatabase(final File path){
        this.databasePath = Objects.requireNonNull(path);
        cachedObjects = new ConcurrentResourceAccessor<>(new HashMap<String, M>());
    }

    protected abstract void writeObject(final M object, final OutputStream stream) throws IOException;

    protected abstract M readObject(final InputStream input) throws IOException;

    public final void reload() throws IOException {
        cachedObjects.write((Action<Map<String, M>, Void, IOException>) objects -> {
            final File[] files = databasePath.listFiles();
            if (files == null)
                throw new IOException(String.format("Folder %s is invalid", databasePath));
            objects.clear();
            for (final File file : files)
                try (final FileInputStream input = new FileInputStream(file)) {
                    objects.put(file.getName(), readObject(input));
                }
            return null;
        });
    }

    public final <O> Optional<O> getDocument(final String name, final Function<? super M, ? extends O> reader) {
        final M result = cachedObjects.read(objects -> objects.get(name));
        return Optional.ofNullable(result).map(reader);
    }

    public final M getOrCreateDocument(final String name, final Supplier<? extends M> documentFactory) throws IOException{
        M result = cachedObjects.read(objects -> objects.get(name));
        if(result != null)
            return result;
        result = cachedObjects.write(objects -> {
            final M newDocument = documentFactory.get();
            objects.put(name, newDocument);
            final File documentFile = Paths.get(databasePath.getAbsolutePath(), name).toFile();

            return newDocument;
        });
        return result;
    }
}
