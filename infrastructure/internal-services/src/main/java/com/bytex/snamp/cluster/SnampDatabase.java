package com.bytex.snamp.cluster;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.SafeCloseable;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexManager;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import static com.bytex.snamp.cluster.DBUtils.withDatabase;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class SnampDatabase extends ODatabaseDocumentTx {
    /*
        Database structured in the following way:
        1. Single class for all collections
        2. Each collection has separated index and cluster
     */
    private static final String CLASS_NAME = "SNAMP_KEY_VALUE_STORAGE";

    SnampDatabase(final String databasePath){
        super(databasePath);
    }

    @Override
    @MethodStub
    public void checkIfActive() {

    }

    void init() {
        try (final SafeCloseable ignored = withDatabase(this)) {
            final OClass documentClass = getMetadata().getSchema().getOrCreateClass(CLASS_NAME);
            if(!documentClass.isAbstract())
                documentClass.setAbstract(true);
            PersistentRecord.defineFields(documentClass);
        }
    }

    private OClass getParentClass(){
        final OClass parentClass = getMetadata().getSchema().getClass(CLASS_NAME);
        if(parentClass == null)
            throw new IllegalStateException(String.format("Parent class %s not found", CLASS_NAME));
        else
            return parentClass;
    }

    OrientKeyValueStorage getKeyValueStorage(final String collectionName) {
        final OClass collectionClass = getMetadata().getSchema().getOrCreateClass(collectionName, getParentClass());
        final String indexName = collectionName + "_INDEX";
        final OIndexManager indexManager = getMetadata().getIndexManager();
        final OIndex<?> index = getMetadata().getIndexManager().existsIndex(indexName) ?
                getMetadata().getIndexManager().getClassIndex(collectionClass.getName(), indexName) :
                RecordKey.defineIndex(collectionClass, indexName);
        return new OrientKeyValueStorage(this, collectionClass.getName(), index.getName());
    }

    void dropKeyValueStorage(final String collectionName) {
        getMetadata().getSchema().dropClass(collectionName);
    }
}
