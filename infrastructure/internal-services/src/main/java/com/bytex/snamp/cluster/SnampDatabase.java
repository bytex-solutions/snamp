package com.bytex.snamp.cluster;

import com.bytex.snamp.MethodStub;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnampDatabase extends ODatabaseDocumentTx {
    SnampDatabase(final String databasePath){
        super(databasePath);
    }

    @Override
    @MethodStub
    public void checkIfActive() {

    }
}
