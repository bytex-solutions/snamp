package com.itworks.snamp.connectors.channels;

import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.SimpleType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Simple set of tests for SNAMP Channels infrastructure.
 */
public final class ChannelsTest extends Assert {
    @Test
    public void collectioChannel() throws ExecutionException, InterruptedException {
        final List<Integer> lst = new ArrayList<>(10);
        lst.add(5);
        lst.add(6);
        lst.add(7);
        final DataChannel<Integer> channel = WellKnownChannel.toChannel(lst, SimpleType.INTEGER);
        channel.write(10);
        channel.write(20);
        assertEquals(Integer.valueOf(5), channel.read().get());
        assertEquals(Integer.valueOf(6), channel.read().get());
        assertEquals(Integer.valueOf(7), channel.read().get());
        assertEquals(Integer.valueOf(10), channel.read().get());
        assertEquals(Integer.valueOf(20), channel.read().get());
    }
}
