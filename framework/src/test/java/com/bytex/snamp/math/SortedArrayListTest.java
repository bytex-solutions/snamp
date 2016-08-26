package com.bytex.snamp.math;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Represents tests for {@link SortedArrayList}.
 */
public final class SortedArrayListTest extends Assert {
    @Test
    public void sortingTest(){
        final SortedArrayList<Integer> actual = new SortedArrayList<>(Integer::compare);
        actual.add(70);
        actual.add(20);
        actual.add(10);
        actual.add(30);
        actual.add(5);
        actual.add(30);
        actual.add(2);
        actual.add(50);
        final ArrayList<Integer> expected = new ArrayList<>();
        expected.add(70);
        expected.add(20);
        expected.add(10);
        expected.add(30);
        expected.add(5);
        expected.add(30);
        expected.add(2);
        expected.add(50);
        expected.sort(Integer::compare);
        for(int i = 0; i < actual.size(); i++)
            assertEquals(expected.get(i), actual.get(i));
    }
}
