package com.bytex.snamp;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ResettableIteratorTest extends Assert {
    @Test
    public void collectionIteratorTest(){
        final ResettableIterator<String> list = ResettableIterator.of(ImmutableList.of("A", "B", "C"));
        assertTrue(list.hasNext());
        assertEquals("A", list.next());
        assertTrue(list.hasNext());
        assertEquals("B", list.next());
        assertTrue(list.hasNext());
        assertEquals("C", list.next());
        assertFalse(list.hasNext());
        list.reset();
        assertTrue(list.hasNext());
        assertEquals("A", list.next());
        assertTrue(list.hasNext());
        assertEquals("B", list.next());
        assertTrue(list.hasNext());
        assertEquals("C", list.next());
        assertFalse(list.hasNext());
    }

    @Test
    public void arrayIteratorTest(){
        final ResettableIterator<String> list = ResettableIterator.of("A", "B", "C");
        assertTrue(list.hasNext());
        assertEquals("A", list.next());
        assertTrue(list.hasNext());
        assertEquals("B", list.next());
        assertTrue(list.hasNext());
        assertEquals("C", list.next());
        assertFalse(list.hasNext());
        list.reset();
        assertTrue(list.hasNext());
        assertEquals("A", list.next());
        assertTrue(list.hasNext());
        assertEquals("B", list.next());
        assertTrue(list.hasNext());
        assertEquals("C", list.next());
        assertFalse(list.hasNext());
    }

    @Test
    public void booleanArrayIteratorTest(){
        final ResettableIterator<Boolean> list = ResettableIterator.of(new boolean[]{true, true, false});
        assertTrue(list.hasNext());
        assertTrue(list.next());
        assertTrue(list.hasNext());
        assertTrue(list.next());
        assertTrue(list.hasNext());
        assertFalse(list.next());
        assertFalse(list.hasNext());
    }

    @Test
    public void stringIteratorTest(){
        final ResettableIterator<Character> iterator = ResettableIterator.of("Test");
        assertEquals(new Character('T'), iterator.next());
        assertEquals(new Character('e'), iterator.next());
    }
}
