package com.bytex.snamp.cluster;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.server.config.OServerConfiguration;
import com.orientechnologies.orient.server.config.OServerStorageConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class DatabaseNodeTest extends Assert {
    private HazelcastInstance hazelcast;

    @Before
    public void setupHazelcast(){
        hazelcast = Hazelcast.newHazelcastInstance();
    }

    @After
    public void destroyHazelcast(){
        hazelcast.shutdown();
        hazelcast = null;
    }

    @Test
    public void databaseTest() throws Exception{
        final DatabaseNode service = new DatabaseNode(hazelcast);
        service.startupFromConfiguration();
        ODatabaseDocumentTx documentDatabase = new ODatabaseDocumentTx(service.getStoragePath("dashboards"));
        if(!documentDatabase.exists())
            documentDatabase.create();
        final ODocument document = new ODocument();
        document.field("field1", 10L);
        document.field("field2", "Hello, world!");
        document.save();
        service.shutdown();
    }
}
