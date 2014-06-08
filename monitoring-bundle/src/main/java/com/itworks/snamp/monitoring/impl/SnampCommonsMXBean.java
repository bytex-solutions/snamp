package com.itworks.snamp.monitoring.impl;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface SnampCommonsMXBean {
    public static final String BEAN_NAME = "com.itworks.snamp.monitoring:type=Commons";

    long getStatisticRenewalTime();

    void setStatisticRenewalTime(final long value);

    long getFaultsCount();

    long getWarningMessagesCount();

    long getDebugMessagesCount();

    long getInformationMessagesCount();

    Map<String, Integer> getInstalledConnectors();

    Map<String, Integer> getInstalledAdapters();

    Map<String, Integer> getInstalledComponents();
}
