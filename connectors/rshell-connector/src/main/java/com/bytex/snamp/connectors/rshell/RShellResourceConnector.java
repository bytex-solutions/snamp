package com.bytex.snamp.connectors.rshell;

import com.bytex.jcommands.CommandExecutionChannel;
import com.bytex.jcommands.impl.TypeTokens;
import com.bytex.jcommands.impl.XmlCommandLineToolProfile;
import com.bytex.jcommands.impl.XmlParserDefinition;
import com.bytex.jcommands.impl.XmlParsingResultType;
import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.connectors.ResourceEventListener;
import com.bytex.snamp.connectors.attributes.*;
import com.bytex.snamp.internal.EntryReader;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.TabularDataUtils;
import com.bytex.snamp.jmx.TabularTypeBuilder;
import com.bytex.snamp.scripting.OSGiScriptEngineManager;
import com.google.common.base.Function;

import javax.management.openmbean.*;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.TypeTokens.safeCast;

/**
 * Represents RShell resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RShellResourceConnector extends AbstractManagedResourceConnector implements AttributeSupport {
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
            definition.exportTableOrDictionaryType(new EntryReader<String, XmlParsingResultType, ExceptionPlaceholder>() {
                @Override
                public boolean read(final String index, final XmlParsingResultType value) {
                    builder.addColumn(index, index, value.getOpenType(), false);
                    return true;
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
            final List<? extends Map<String, ?>> rows = com.bytex.snamp.TypeTokens.safeCast(commandProfile.readFromChannel(channel, channelParams), TypeTokens.TABLE_TYPE_TOKEN);
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
            definition.exportTableOrDictionaryType(new EntryReader<String, XmlParsingResultType, ExceptionPlaceholder>() {
                @Override
                public boolean read(final String index, final XmlParsingResultType value) {
                    builder.addItem(index, index, value.getOpenType());
                    return true;
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

    private static final class RShellAttributes extends AbstractAttributeRepository<RShellAttributeInfo> {
        private final CommandExecutionChannel executionChannel;
        private final ScriptEngineManager scriptEngineManager;
        private final Logger logger;

        private RShellAttributes(final String resourceName,
                                 final CommandExecutionChannel channel,
                                 final ScriptEngineManager engineManager,
                                 final Logger logger) {
            super(resourceName, RShellAttributeInfo.class);
            this.executionChannel = Objects.requireNonNull(channel);
            this.scriptEngineManager = engineManager;
            this.logger = Objects.requireNonNull(logger);
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
            failedToConnectAttribute(logger, Level.WARNING, attributeID, attributeName, e);
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
            failedToGetAttribute(logger, Level.WARNING, attributeID, e);
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
            failedToSetAttribute(logger, Level.WARNING, attributeID, value, e);
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
            final String commandProfileFilePath = descriptor.getAttributeName();
            final XmlCommandLineToolProfile profile = XmlCommandLineToolProfile.loadFrom(new File(commandProfileFilePath));
            if (profile != null) {
                profile.setScriptManager(scriptEngineManager);
                switch (profile.getReaderTemplate().getCommandOutputParser().getParsingResultType()) {
                    case DICTIONARY:
                        return new DictionaryAttributeInfo(attributeID, profile, descriptor);
                    case TABLE:
                        return new TableAttributeInfo(attributeID, profile, descriptor);
                    default:
                        return new SimpleAttributeInfo(attributeID, profile, descriptor);
                }
            } else
                throw new FileNotFoundException(commandProfileFilePath + " RShell command profile doesn't exist");
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

    RShellResourceConnector(final String resourceName,
                            final RShellConnectionOptions connectionOptions) throws Exception {
        executionChannel = connectionOptions.createExecutionChannel();
        if(executionChannel == null)
            throw new InstantiationException(String.format("Unknown channel: %s", connectionOptions));
        attributes = new RShellAttributes(resourceName,
                executionChannel,
                new OSGiScriptEngineManager(Utils.getBundleContextByObject(this)), getLogger());
    }

    RShellResourceConnector(final String resourceName,
                            final String connectionString,
                            final Map<String, String> connectionOptions) throws Exception{
        this(resourceName, new RShellConnectionOptions(connectionString, connectionOptions));
    }

    boolean addAttribute(final String id, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
        return attributes.addAttribute(id, attributeName, readWriteTimeout, options) != null;
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p/>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes);
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return findObject(objectType,
                new Function<Class<T>, T>() {
                    @Override
                    public T apply(final Class<T> objectType) {
                        return RShellResourceConnector.super.queryObject(objectType);
                    }
                }, attributes);
    }

    void removeAttributesExcept(final Set<String> attributes) {
        this.attributes.removeAllExcept(attributes);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        attributes.removeAll(true);
        super.close();
        executionChannel.close();
    }
}
