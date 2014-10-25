package com.itworks.snamp.internal;

import java.util.HashMap;

/**
 * Represents reentrant read/write lock that supports
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class UpgradableReadWriteLock {

    private final HashMap<Thread, Integer> readers = new HashMap<>(10);
    private int writeAccesses = 0;
    private int writeRequests = 0;
    private Thread writingThread = null;

    public synchronized void lockRead() throws InterruptedException {
        final Thread callingThread = Thread.currentThread();
        while (!canGrantReadAccess(callingThread)) {
            wait();
        }

        readers.put(callingThread,
                (getReadAccessCount(callingThread) + 1));
    }

    private boolean canGrantReadAccess(final Thread callingThread) {
        if (isWriter(callingThread)) return true;
        else if (hasWriter()) return false;
        else if (isReader(callingThread)) return true;
        else if (hasWriteRequests()) return false;
        else return true;
    }


    public synchronized void unlockRead() throws IllegalMonitorStateException {
        final Thread callingThread = Thread.currentThread();
        if (!isReader(callingThread)) {
            throw new IllegalMonitorStateException("Calling Thread does not" +
                    " hold a read lock on this ReadWriteLock");
        }
        final int accessCount = getReadAccessCount(callingThread);
        if (accessCount == 1)
            readers.remove(callingThread);
        else
            readers.put(callingThread, (accessCount - 1));
        notifyAll();
    }

    public synchronized void lockWrite() throws InterruptedException {
        writeRequests++;
        final Thread callingThread = Thread.currentThread();
        while (!canGrantWriteAccess(callingThread))
            wait();
        writeRequests--;
        writeAccesses++;
        writingThread = callingThread;
    }

    public synchronized void unlockWrite() throws IllegalMonitorStateException {
        if (!isWriter(Thread.currentThread())) {
            throw new IllegalMonitorStateException("Calling Thread does not" +
                    " hold the write lock on this ReadWriteLock");
        }
        writeAccesses--;
        if (writeAccesses == 0)
            writingThread = null;
        notifyAll();
    }

    private boolean canGrantWriteAccess(Thread callingThread) {
        if (isOnlyReader(callingThread)) return true;
        if (hasReaders()) return false;
        if (writingThread == null) return true;
        if (!isWriter(callingThread)) return false;
        return true;
    }


    private int getReadAccessCount(Thread callingThread) {
        Integer accessCount = readers.get(callingThread);
        return accessCount == null ? 0 : accessCount;
    }

    private boolean hasReaders() {
        return readers.size() > 0;
    }

    private boolean isReader(final Thread callingThread) {
        return readers.containsKey(callingThread);
    }

    private boolean isOnlyReader(Thread callingThread) {
        return readers.size() == 1 &&
                isReader(callingThread);
    }

    private boolean hasWriter() {
        return writingThread != null;
    }

    private boolean isWriter(final Thread callingThread) {
        return writingThread == callingThread;
    }

    private boolean hasWriteRequests() {
        return writeRequests > 0;
    }

}
