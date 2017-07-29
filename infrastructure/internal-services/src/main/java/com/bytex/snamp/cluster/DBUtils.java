package com.bytex.snamp.cluster;

import com.bytex.snamp.Acceptor;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import java.util.function.Supplier;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class DBUtils {
    static <V> V supplyWithDatabase(final ODatabaseDocumentInternal database, final Supplier<V> callable) {
        if (!database.isActiveOnCurrentThread())
            database.activateOnCurrentThread();
        try {
            return callable.get();
        } finally {
            ODatabaseRecordThreadLocal.INSTANCE.remove();
        }
    }

    static void runWithDatabase(final ODatabaseDocumentInternal database, final Runnable runnable) {
        if (!database.isActiveOnCurrentThread())
            database.activateOnCurrentThread();
        try {
            runnable.run();
        } finally {
            ODatabaseRecordThreadLocal.INSTANCE.remove();
        }
    }

    static <E extends Throwable> void acceptWithDatabase(final ODatabaseDocumentTx database, final Acceptor<? super ODatabaseDocumentTx, E> acceptor) throws E{
        if (!database.isActiveOnCurrentThread())
            database.activateOnCurrentThread();
        try {
            acceptor.accept(database);
        } finally {
            ODatabaseRecordThreadLocal.INSTANCE.remove();
        }
    }
}
