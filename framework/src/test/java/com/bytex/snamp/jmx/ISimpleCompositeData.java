package com.bytex.snamp.jmx;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface ISimpleCompositeData extends CompositeDataBean {
    String KEY1_NAME = "key1";
    OpenType<Integer> KEY1_TYPE = SimpleType.INTEGER;
    String KEY2_NAME = "key2";
    OpenType<Double> KEY2_TYPE = SimpleType.DOUBLE;
    String KEY3_NAME = "key3";
    OpenType<String> KEY3_TYPE = SimpleType.STRING;


    int getKey1();
    void setKey1(final int value) throws OpenDataException;
    double getKey2();
    void setKey2(final double value) throws OpenDataException;
    String getKey3();
    void setKey3(final String value) throws OpenDataException;
}
