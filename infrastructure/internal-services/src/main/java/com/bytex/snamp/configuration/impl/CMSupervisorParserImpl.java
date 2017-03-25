package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.configuration.internal.CMSupervisorParser;

import java.util.regex.Pattern;

import static com.bytex.snamp.connector.supervision.ManagedResourceGroupSupervisor.CAPABILITY_NAMESPACE;

/**
 * Represents parser of {@link SerializableSupervisorConfiguration}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class CMSupervisorParserImpl extends AbstractConfigurationParser<SerializableSupervisorConfiguration> implements CMSupervisorParser {
    private static final String SUPERVISOR_PID_TEMPLATE = CAPABILITY_NAMESPACE + ".%s";
    private static final String GROUP_NAME_PROPERTY = "$groupName$";
    private static final String ALL_SUPERVISORS_QUERY = String.format("(%s=%s)", SERVICE_PID, String.format(GATEWAY_PID_TEMPLATE, "*"));
    private static final Pattern GATEWAY_PID_REPLACEMENT = Pattern.compile(String.format(GATEWAY_PID_TEMPLATE, ""), Pattern.LITERAL);

}
