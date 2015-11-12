package com.bytex.snamp.core;

import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents generator of unique identifiers scoped by this process.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class InMemoryIDGenerator implements IDGenerator {
    private final ConcurrentMap<String, AtomicLong> generators;

    InMemoryIDGenerator(){
        generators = Maps.newConcurrentMap();
    }

    private synchronized AtomicLong getGeneratorSync(final String generatorName){
        final AtomicLong result;
        if(generators.containsKey(generatorName))
            result = generators.get(generatorName);
        else generators.put(generatorName, result = new AtomicLong(0L));
        return result;
    }

    private AtomicLong getGenerator(final String generatorName){
        return generators.containsKey(generatorName) ?
                generators.get(generatorName) :
                getGeneratorSync(generatorName);
    }

    @Override
    public long generateID(final String generatorName) {
        return getGenerator(generatorName).getAndIncrement();
    }

    /**
     * Resets the specified generator.
     *
     * @param generatorName The name of the generator to reset.
     */
    @Override
    public synchronized void reset(final String generatorName) {
        generators.remove(generatorName);
    }
}
