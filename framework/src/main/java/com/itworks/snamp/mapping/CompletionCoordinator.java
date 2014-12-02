package com.itworks.snamp.mapping;

import com.itworks.snamp.WriteOnceRef;

import java.util.concurrent.CountDownLatch;

/**
 * Represents completion coordinator used to synchronize multiple tasks.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CompletionCoordinator extends CountDownLatch {
    private final WriteOnceRef<Exception> errorHolder;

    CompletionCoordinator(final int numberOfTasks){
        super(numberOfTasks);
        errorHolder = new WriteOnceRef<>(null);
    }

    void complete(final Exception e){
        countDown();
        errorHolder.set(e);
    }

    Exception getError(){
        return errorHolder.get();
    }
}
