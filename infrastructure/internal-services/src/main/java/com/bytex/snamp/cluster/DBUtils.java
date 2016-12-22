package com.bytex.snamp.cluster;

import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;

import java.util.function.Supplier;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class DBUtils {
    static <V> V supplyWithDatabase(final ODatabaseDocumentInternal database, final Supplier<V> callable) {
        ODatabaseRecordThreadLocal.INSTANCE.set(database);
        try {
            return callable.get();
        } finally {
            ODatabaseRecordThreadLocal.INSTANCE.remove();
        }
    }

    static void runWithDatabase(final ODatabaseDocumentInternal database, final Runnable runnable) {
        ODatabaseRecordThreadLocal.INSTANCE.set(database);
        try {
            runnable.run();
        } finally {
            ODatabaseRecordThreadLocal.INSTANCE.remove();
        }
    }
}
