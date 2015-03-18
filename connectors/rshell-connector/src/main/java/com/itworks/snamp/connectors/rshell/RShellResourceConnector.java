package com.itworks.snamp.connectors.rshell;

import com.itworks.jcommands.CommandExecutionChannel;
import com.itworks.jcommands.impl.TypeTokens;
import com.itworks.jcommands.impl.XmlCommandLineToolProfile;
import com.itworks.jcommands.impl.XmlParserDefinition;
import com.itworks.jcommands.impl.XmlParsingResultType;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.OpenTypeAttributeInfo;
import com.itworks.snamp.internal.RecordReader;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import com.itworks.snamp.jmx.DescriptorUtils;
import com.itworks.snamp.jmx.TabularDataUtils;
import com.itworks.snamp.jmx.TabularTypeBuilder;
import com.itworks.snamp.scripting.OSGiScriptEngineManager;

import javax.management.*;
import javax.management.openmbean.*;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.TypeTokens.safeCast;
import static com.itworks.snamp.connectors.rshell.RShellConnectorConfigurationDescriptor.COMMAND_PROFILE_PATH_PARAM;

/**
 * Represents RShell resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RShellResourceConnector extends AbstractManagedResourceConnector implements AttributeSupport {
    final static String NAME = RShellConnectorHelpers.CONNECTOR_NAME;

    private static abstract class RShellAttributeInfo extends OpenTypeAttributeInfo{
        private static final long serialVersionUID = -403897890533078455L;
        protected final XmlCommandLineToolProfile commandProfile;
        private final Map<String, ?> parameters;

        private RShellAttributeInfo(final String attributeID,
                                    final XmlCommandLineToolProfile profile,
                                    final OpenType<?> attributeType,
                                    final AttributeDescriptor options){
            super(attributeID,
                    attributeType,
                    getDescription(options),
                    getSpecifier(profile),
                    options);
            commandProfile = profile;
            parameters = DescriptorUtils.toMap(options);
        }

        private static String getDescription(final AttributeDescriptor descriptor){
            final String result = descriptor.getDescription();
            return result == null || result.isEmpty() ? "RShell Attribute" : result;
        }

        private static AttributeSpecifier getSpecifier(final XmlCommandLineToolProfile commandProfile){
            return commandProfile.getModifierTemplate() != null ?
                    AttributeSpecifier.READ_WRITE:
                    AttributeSpecifier.READ_ONLY;
        }

        protected abstract Object getValue(final CommandExecutionChannel channel, final Map<String, ?> channelParams) throws IOException, ScriptException, OpenDataException;

        final Object getValue(final CommandExecutionChannel channel) throws IOException, ScriptException, OpenDataException {
            return getValue(channel, parameters);
        }

        void setValue(final CommandExecutionChannel channel, final Object value) throws ScriptException, IOException{
            commandProfile.writeToChannel(channel, value);
        }
    }

    private static final class SimpleAttributeInfo extends RShellAttributeInfo{

        private static final long serialVersionUID = 8087839188926328479L;

        private SimpleAttributeInfo(final String attributeID,
                                    final XmlCommandLineToolProfile profile,
                                    final AttributeDescriptor options) {
            super(attributeID, profile,
                    profile.getModifierTemplate().getCommandOutputParser().getParsingResultType().getOpenType(),
                    options);
        }

        @Override
        protected Object getValue(final CommandExecutionChannel channel, final Map<String, ?> channelParams) throws IOException, ScriptException {
            return commandProfile.readFromChannel(channel, channelParams);
        }
    }

    private static final class TableAttributeInfo extends RShellAttributeInfo{
        private static final String INDEX_COLUMN = "index";
        private static final long serialVersionUID = -3828510082280244717L;

        private TableAttributeInfo(final String attributeID,
                                   final XmlCommandLineToolProfile profile,
                                   final AttributeDescriptor descriptor) throws OpenDataException{
            super(attributeID,
                    profile,
                    getTabularType(descriptor, profile.getReaderTemplate().getCommandOutputParser()),
                    descriptor);
        }

        private static TabularType getTabularType(final AttributeDescriptor descriptor,
                                                      final XmlParserDefinition definition) throws OpenDataException{
            final TabularTypeBuilder builder = new TabularTypeBuilder();
            builder.addColumn(INDEX_COLUMN, "The index of the row", SimpleType.INTEGER, true);
            definition.exportTableOrDictionaryType(new RecordReader<String, XmlParsingResultType, ExceptionPlaceholder>() {
                @Override
                public void read(final String index, final XmlParsingResultType value) {
                    builder.addColumn(index, index, value.getOpenType(), false);
                }
            });
            builder.setTypeName(String.format("%sTabularType", descriptor.getAttributeName()), true);
            builder.setDescription(RShellAttributeInfo.getDescription(descriptor), true);
            return builder.build();
        }

        private TabularData convert(final List<? extends Map<String, ?>> rows) throws OpenDataException{
            final TabularDataSupport result = new TabularDataSupport((TabularType)getOpenType());
            for(int index = 0; index < rows.size(); index++){
                final Map<String, Object> row = new HashMap<>(rows.get(index));
                row.put(INDEX_COLUMN, index);
                result.put(new CompositeDataSupport(result.getTabularType().getRowType(), row));
            }
            return result;
        }

        @Override
        protected Object getValue(final CommandExecutionChannel channel, final Map<String, ?> channelParams) throws IOException, ScriptException, OpenDataException {
            final List<? extends Map<String, ?>> rows = com.itworks.snamp.TypeTokens.safeCast(commandProfile.readFromChannel(channel, channelParams), TypeTokens.TABLE_TYPE_TOKEN);
            return rows != null ? convert(rows) : null;
        }

        @Override
        void setValue(final CommandExecutionChannel channel, Object value) throws ScriptException, IOException {
            if(value instanceof TabularData)
                value = TabularDataUtils.getRows((TabularData) value);
            commandProfile.writeToChannel(channel, value);
        }
    }

    private static final class DictionaryAttributeInfo extends RShellAttributeInfo{

        private static final long serialVersionUID = 7974143091272614419L;

        private DictionaryAttributeInfo(final String attributeID,
                                        final XmlCommandLineToolProfile profile,
                                        final AttributeDescriptor descriptor) throws OpenDataException {
            super(attributeID,
                    profile,
                    getCompositeType(descriptor, profile.getReaderTemplate().getCommandOutputParser()),
                    descriptor);
        }

        private static CompositeType getCompositeType(final AttributeDescriptor descriptor,
                                                    final XmlParserDefinition definition) throws OpenDataException{
            final CompositeTypeBuilder builder = new CompositeTypeBuilder();
            definition.exportTableOrDictionaryType(new RecordReader<String, XmlParsingResultType, ExceptionPlaceholder>() {
                @Override
                public void read(final String index, final XmlParsingResultType value) {
                    builder.addItem(index, index, value.getOpenType());
                }
            });
            builder.setTypeName(String.format("%sCompositeType", descriptor.getAttributeName()));
            builder.setDescription(RShellAttributeInfo.getDescription(descriptor));
            return builder.build();
        }

        private CompositeData convert(final Map<String, ?> value) throws OpenDataException{
            return new CompositeDataSupport((CompositeType)getOpenType(), value);
        }

        @Override
        protected CompositeData getValue(final CommandExecutionChannel channel, final Map<String, ?> channelParams) throws IOException, ScriptException, OpenDataException {
            final Map<String, ?> dict = safeCast(commandProfile.readFromChannel(channel, channelParams), TypeTokens.DICTIONARY_TYPE_TOKEN);
            return dict != null ? convert(dict) : null;
        }
    }

    private static final class RShellAttributes extends AbstractAttributeSupport<RShellAttributeInfo> {

        private final CommandExecutionChannel executionChannel;
        private final ScriptEngineManager scriptEngineManager;

        private RShellAttributes(final CommandExecutionChannel channel,
                                 final ScriptEngineManager engineManager) {
            super(RShellAttributeInfo.class);
            this.executionChannel = channel;
            this.scriptEngineManager = engineManager;
        }

        /**
         * Reports an error when connecting attribute.
         *
         * @param attributeID   The attribute identifier.
         * @param attributeName The name of the attribute.
         * @param e             Internal connector error.
         * @see #failedToConnectAttribute(java.util.logging.Logger, java.util.logging.Level, String, String, Exception)
         */
        @Override
        protected void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
            RShellConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
                @Override
                public void accept(final Logger logger) {
                    failedToConnectAttribute(logger, Level.SEVERE, attributeID, attributeName, e);
                }
            });
        }

        /**
         * Reports an error when getting attribute.
         *
         * @param attributeID The attribute identifier.
         * @param e           Internal connector error.
         * @see #failedToGetAttribute(java.util.logging.Logger, java.util.logging.Level, String, Exception)
         */
        @Override
        protected void failedToGetAttribute(final String attributeID, final Exception e) {
            RShellConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
                @Override
                public void accept(final Logger logger) {
                    failedToGetAttribute(logger, Level.WARNING, attributeID, e);
                }
            });
        }

        /**
         * Reports an error when updating attribute.
         *
         * @param attributeID The attribute identifier.
         * @param value       The value of the attribute.
         * @param e           Internal connector error.
         * @see #failedToSetAttribute(java.util.logging.Logger, java.util.logging.Level, String, Object, Exception)
         */
        @Override
        protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
            RShellConnectorHelpers.withLogger(new SafeConsumer<Logger>() {
                @Override
                public void accept(final Logger logger) {
                    failedToSetAttribute(logger, Level.WARNING, attributeID, value, e);
                }
            });
        }

        /**
         * Connects to the specified attribute.
         *
         * @param attributeID The id of the attribute.
         * @param descriptor  Attribute descriptor.
         * @return The description of the attribute.
         * @throws Exception Internal connector error.
         */
        @Override
        protected RShellAttributeInfo connectAttribute(final String attributeID,
                                                       final AttributeDescriptor descriptor) throws Exception {
            if (descriptor.hasField(COMMAND_PROFILE_PATH_PARAM)) {
                final XmlCommandLineToolProfile profile = XmlCommandLineToolProfile.loadFrom(new File(descriptor.getField(COMMAND_PROFILE_PATH_PARAM, String.class)));
                if (profile != null) {
                    profile.setScriptManager(scriptEngineManager);
                    switch (profile.getReaderTemplate().getCommandOutputParser().getParsingResultType()){
                        case DICTIONARY:
                            return new DictionaryAttributeInfo(attributeID, profile, descriptor);
                        case TABLE:
                            return new TableAttributeInfo(attributeID, profile, descriptor);
                        default:
                            return new SimpleAttributeInfo(attributeID, profile, descriptor);
                    }
                }
                else
                    throw new CommandProfileNotFoundException(descriptor.getFieldValue(COMMAND_PROFILE_PATH_PARAM));
            }
            throw new UndefinedCommandProfileException();
        }

        /**
         * Obtains the value of a specific attribute of the managed resource.
         *
         * @param metadata The metadata of the attribute.
         * @return The value of the attribute retrieved.
         * @throws Exception Internal connector error.
         */
        @Override
        protected Object getAttribute(final RShellAttributeInfo metadata) throws Exception {
            return metadata.getValue(executionChannel);
        }

        /**
         * Set the value of a specific attribute of the managed resource.
         *
         * @param attribute The attribute of to set.
         * @param value     The value of the attribute.
         * @throws Exception                                       Internal connector error.
         * @throws javax.management.InvalidAttributeValueException Incompatible attribute type.
         */
        @Override
        protected void setAttribute(final RShellAttributeInfo attribute, final Object value) throws Exception {
            attribute.setValue(executionChannel, value);
        }
    }

    private final CommandExecutionChannel executionChannel;
    private final RShellAttributes attributes;

    /**
     * Initializes a new management connector.
     *
     * @param connectionOptions Management connector initialization options.
     */
    RShellResourceConnector(final RShellConnectionOptions connectionOptions) throws Exception {
        executionChannel = connectionOptions.createExecutionChannel();
        attributes = new RShellAttributes(executionChannel, new OSGiScriptEngineManager(Utils.getBundleContextByObject(this)));
    }

    RShellResourceConnector(final String connectionString,
                                   final Map<String, String> connectionOptions) throws Exception{
        this(new RShellConnectionOptions(connectionString, connectionOptions));
    }

    /**
     * Obtain the value of a specific attribute of the managed resource.
     *
     * @param attributeID The name of the attribute to be retrieved
     * @return The value of the attribute retrieved.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.MBeanException             Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's getter.
     * @throws javax.management.ReflectionException        Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the getter.
     * @see #setAttribute(javax.management.Attribute)
     */
    @Override
    public Object getAttribute(final String attributeID) throws AttributeNotFoundException, MBeanException, ReflectionException {
        verifyInitialization();
        return attributes.getAttribute(attributeID);
    }

    /**
     * Set the value of a specific attribute of the managed resource.
     *
     * @param attribute The identification of the attribute to
     *                  be set and  the value it is to be set to.
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.InvalidAttributeValueException
     * @throws javax.management.MBeanException                 Wraps a <CODE>java.lang.Exception</CODE> thrown by the MBean's setter.
     * @throws javax.management.ReflectionException            Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the MBean's setter.
     * @see #getAttribute
     */
    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        verifyInitialization();
        attributes.setAttribute(attribute);
    }

    /**
     * Get the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of the attributes to be retrieved.
     * @return The list of attributes retrieved.
     * @see #setAttributes
     */
    @Override
    public AttributeList getAttributes(final String[] attributes) {
        verifyInitialization();
        return this.attributes.getAttributes(attributes);
    }

    /**
     * Sets the values of several attributes of the Dynamic MBean.
     *
     * @param attributes A list of attributes: The identification of the
     *                   attributes to be set and  the values they are to be set to.
     * @return The list of attributes that were set, with their new values.
     * @see #getAttributes
     */
    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        verifyInitialization();
        return this.attributes.setAttributes(attributes);
    }

    /**
     * Connects to the specified attribute.
     *
     * @param id               A key string that is used to invoke attribute from this connector.
     * @param attributeName    The name of the attribute.
     * @param readWriteTimeout A read/write timeout using for attribute read/write operation.
     * @param options          The attribute discovery options.
     * @return The description of the attribute.
     * @throws javax.management.AttributeNotFoundException The managed resource doesn't provide the attribute with the specified name.
     * @throws javax.management.JMException                Internal connector error.
     */
    @Override
    public MBeanAttributeInfo connectAttribute(final String id, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) throws JMException {
        verifyInitialization();
        return attributes.connectAttribute(id, attributeName, readWriteTimeout, options);
    }

    /**
     * Gets an array of connected attributes.
     *
     * @return An array of connected attributes.
     */
    @Override
    public MBeanAttributeInfo[] getAttributeInfo() {
        return attributes.getAttributeInfo();
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param id The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    @Override
    public boolean disconnectAttribute(final String id) {
        return attributes.disconnectAttribute(id);
    }



    /**
     * Gets a logger associated with this platform service.
     *
     * @return A logger associated with this platform service.
     */
    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        try {
            executionChannel.close();
        } finally {
            super.close();
        }
    }
}
