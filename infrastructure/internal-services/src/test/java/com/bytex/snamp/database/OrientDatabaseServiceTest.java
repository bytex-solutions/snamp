package com.bytex.snamp.database;

import com.bytex.snamp.cluster.OrientDatabaseService;
import com.hazelcast.core.Hazelcast;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class OrientDatabaseServiceTest extends Assert {
    @Test
    public void databaseTest() throws Exception{
        final OrientDatabaseService service = new OrientDatabaseService(Hazelcast.newHazelcastInstance());
        service.startupFromConfiguration();
        ODatabaseDocumentTx document = new ODatabaseDocumentTx(service.getStoragePath("dashboards"));
        if(!document.exists())
            document.create();
        document = service.openDatabase(document, "snamp", "snamp", null, false);
        service.shutdown();
    }
}
