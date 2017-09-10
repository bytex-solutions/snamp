package com.bytex.snamp.cluster;

import com.bytex.snamp.SafeCloseable;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class DBUtils {
    static SafeCloseable withDatabase(final ODatabase<?> database) {
        if (database.isActiveOnCurrentThread())
            return () -> {
            };
        else {
            database.activateOnCurrentThread();
            return () -> ODatabaseRecordThreadLocal.INSTANCE.remove();
        }
    }
}
