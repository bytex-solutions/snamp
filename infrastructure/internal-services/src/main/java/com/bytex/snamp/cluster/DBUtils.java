package com.bytex.snamp.cluster;

import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;

import java.util.function.Supplier;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class DBUtils {
    static <V> V supplyWithDatabase(final ODatabaseDocumentInternal database, final Supplier<V> callable) {
        if (!database.isActiveOnCurrentThread())
            database.activateOnCurrentThread();
        return callable.get();
    }

    static void runWithDatabase(final ODatabaseDocumentInternal database, final Runnable runnable) {
        if (!database.isActiveOnCurrentThread())
            database.activateOnCurrentThread();
        runnable.run();
    }
}
