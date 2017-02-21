package com.bytex.snamp.connector.rshell;

import com.bytex.jcommands.CommandExecutionChannel;
import com.bytex.jcommands.impl.TypeTokens;
import com.bytex.jcommands.impl.XmlCommandLineToolProfile;
import com.bytex.jcommands.impl.XmlParserDefinition;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.AbstractManagedResourceConnector;
import com.bytex.snamp.connector.ResourceEventListener;
import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AbstractOpenAttributeInfo;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.connector.operations.AbstractOpenOperationInfo;
import com.bytex.snamp.connector.operations.AbstractOperationRepository;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.*;
import com.bytex.snamp.scripting.OSGiScriptEngineManager;

import javax.management.MBeanOperationInfo;
import javax.management.openmbean.*;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.bytex.snamp.Convert.toTypeToken;

/**
 * Represents RShell resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class RShellResourceConnector extends AbstractManagedResourceConnector {

    private static abstract class RShellOperationInfo extends AbstractOpenOperationInfo {
        private static final long serialVersionUID = -403897890533078455L;
        final XmlCommandLineToolProfile commandProfile;

        private RShellOperationInfo(String name, OpenType<?> returnType,
                            final XmlCommandLineToolProfile profile, OperationDescriptor descriptor) {
            super(name, getDescription(descriptor), getSignature(profile, getDescription(descriptor)),
                    returnType, getImpact(profile), descriptor);
            commandProfile = profile;
        }

        private static String getDescription(final OperationDescriptor descriptor){
            final String result = descriptor.getAlternativeName();
            return result == null || result.isEmpty() ? "RShell Operation" : result;
        }

        private static OpenMBeanParameterInfoSupplier<?>[] getSignature(final XmlCommandLineToolProfile profile,
                                                                     final String description) {
            final List<String> token = profile.getReaderTemplate().extractTemplateParameters();
            if (!token.isEmpty()) {
                return token.stream()
                        .map(param -> new OpenMBeanParameterInfoSupplier<>(param,
                                String.format("Argument %s for %s", param, description),
                                SimpleType.STRING))
                        .collect(Collectors.toList())
                        .toArray(new OpenMBeanParameterInfoSupplier<?>[token.size()]);
            } else {
                return new OpenMBeanParameterInfoSupplier[0];
            }
        }

        static OpenType<?> getType(final XmlCommandLineToolProfile profile) {
            if (profile.getReaderTemplate().getCommandOutputParser() == null) {
                return SimpleType.VOID;
            } else {
                return profile.getReaderTemplate().getCommandOutputParser().getParsingResultType().getOpenType();
            }
        }

        static int getImpact(final XmlCommandLineToolProfile profile) {
            return (profile.getReaderTemplate().getCommandOutputParser() == null)
                    ? MBeanOperationInfo.ACTION : MBeanOperationInfo.ACTION_INFO;
        }

        protected abstract Object invoke(final CommandExecutionChannel channel, final Map<String, ?> channelParams) throws IOException, ScriptException, OpenDataException;
    }

    private static final class SimpleOperationInfo extends RShellOperationInfo {
        private static final long serialVersionUID = 84553453453432379L;

        private SimpleOperationInfo(final String operationName,
                                    final XmlCommandLineToolProfile profile,
                                    final OperationDescriptor options) {
            super(operationName, getType(profile), profile, options);

        }

        @Override
        protected Object invoke(CommandExecutionChannel channel, Map<String, ?> channelParams) throws IOException, ScriptException, OpenDataException {
            return commandProfile.readFromChannel(channel, channelParams);
        }
    }

    private static final class TableOperationInfo extends RShellOperationInfo {
        private static final String INDEX_COLUMN = "index";
        private static final long serialVersionUID = -3828510082280244717L;

        private TableOperationInfo(final String operationName,
                                   final XmlCommandLineToolProfile profile,
                                   final OperationDescriptor descriptor) throws OpenDataException{
            super(operationName,
                    getTabularType(operationName, descriptor, profile.getReaderTemplate().getCommandOutputParser()),
                    profile,
                    descriptor);
        }

        private static TabularType getTabularType(final String attributeName,
                                                  final OperationDescriptor descriptor,
                                                  final XmlParserDefinition definition) throws OpenDataException{
            final TabularTypeBuilder builder = new TabularTypeBuilder();
            builder.addColumn(INDEX_COLUMN, "The index of the row", SimpleType.INTEGER, true);
            definition.exportTableOrDictionaryType((index, value) -> {
                builder.addColumn(index, index, value.getOpenType(), false);
                return true;
            });
            builder.setTypeName(String.format("%sTabularType", descriptor.getName(attributeName)), true);
            builder.setDescription(RShellOperationInfo.getDescription(descriptor), true);
            return builder.build();
        }

        private TabularData convert(final List<? extends Map<String, ?>> rows) throws OpenDataException{
            final TabularDataSupport result = new TabularDataSupport((TabularType)getReturnOpenType());
            for(int index = 0; index < rows.size(); index++){
                final Map<String, Object> row = new HashMap<>(rows.get(index));
                row.put(INDEX_COLUMN, index);
                result.put(new CompositeDataSupport(result.getTabularType().getRowType(), row));
            }
            return result;
        }

        @Override
        protected Object invoke(CommandExecutionChannel channel, Map<String, ?> channelParams) throws IOException, ScriptException, OpenDataException {
            final List<? extends Map<String, ?>> rows = toTypeToken(commandProfile.readFromChannel(channel, channelParams), TypeTokens.TABLE_TYPE_TOKEN);
            return rows != null ? convert(rows) : null;
        }
    }

    private static final class DictionaryOperationInfo extends RShellOperationInfo {

        private static final long serialVersionUID = 7974143091272614419L;

        private DictionaryOperationInfo(final String operationName,
                                        final XmlCommandLineToolProfile profile,
                                        final OperationDescriptor descriptor) throws OpenDataException {
            super(operationName,
                    getCompositeType(operationName, descriptor, profile.getReaderTemplate().getCommandOutputParser()),
                    profile,
                    descriptor);
        }

        private static CompositeType getCompositeType(final String attributeName,
                                                      final OperationDescriptor descriptor,
                                                      final XmlParserDefinition definition) throws OpenDataException{
            final CompositeTypeBuilder builder = new CompositeTypeBuilder();
            definition.exportTableOrDictionaryType((index, value) -> {
                builder.addItem(index, index, value.getOpenType());
                return true;
            });
            builder.setTypeName(String.format("%sCompositeType", descriptor.getName(attributeName)));
            builder.setDescription(RShellOperationInfo.getDescription(descriptor));
            return builder.build();
        }

        private CompositeData convert(final Map<String, ?> value) throws OpenDataException{
            return new CompositeDataSupport((CompositeType)getReturnOpenType(), value);
        }

        @Override
        protected Object invoke(CommandExecutionChannel channel, Map<String, ?> channelParams) throws IOException, ScriptException, OpenDataException {
            final Map<String, ?> dict = toTypeToken(commandProfile.readFromChannel(channel, channelParams), TypeTokens.DICTIONARY_TYPE_TOKEN);
            return dict != null ? convert(dict) : null;
        }
    }


    private static abstract class RShellAttributeInfo extends AbstractOpenAttributeInfo {
        private static final long serialVersionUID = -403897890533078455L;
        final XmlCommandLineToolProfile commandProfile;
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
            return result.isEmpty() ? "RShell Attribute" : result;
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

        private TableAttributeInfo(final String attributeName,
                                   final XmlCommandLineToolProfile profile,
                                   final AttributeDescriptor descriptor) throws OpenDataException{
            super(attributeName,
                    profile,
                    getTabularType(attributeName, descriptor, profile.getReaderTemplate().getCommandOutputParser()),
                    descriptor);
        }

        private static TabularType getTabularType(final String attributeName,
                                                    final AttributeDescriptor descriptor,
                                                      final XmlParserDefinition definition) throws OpenDataException{
            final TabularTypeBuilder builder = new TabularTypeBuilder();
            builder.addColumn(INDEX_COLUMN, "The index of the row", SimpleType.INTEGER, true);
            definition.exportTableOrDictionaryType((index, value) -> {
                builder.addColumn(index, index, value.getOpenType(), false);
                return true;
            });
            builder.setTypeName(String.format("%sTabularType", descriptor.getName(attributeName)), true);
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
            final List<? extends Map<String, ?>> rows = toTypeToken(commandProfile.readFromChannel(channel, channelParams), TypeTokens.TABLE_TYPE_TOKEN);
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

        private DictionaryAttributeInfo(final String attributeName,
                                        final XmlCommandLineToolProfile profile,
                                        final AttributeDescriptor descriptor) throws OpenDataException {
            super(attributeName,
                    profile,
                    getCompositeType(attributeName, descriptor, profile.getReaderTemplate().getCommandOutputParser()),
                    descriptor);
        }

        private static CompositeType getCompositeType(final String attributeName,
                                                      final AttributeDescriptor descriptor,
                                                    final XmlParserDefinition definition) throws OpenDataException{
            final CompositeTypeBuilder builder = new CompositeTypeBuilder();
            definition.exportTableOrDictionaryType((index, value) -> {
                builder.addItem(index, index, value.getOpenType());
                return true;
            });
            builder.setTypeName(String.format("%sCompositeType", descriptor.getName(attributeName)));
            builder.setDescription(RShellAttributeInfo.getDescription(descriptor));
            return builder.build();
        }

        private CompositeData convert(final Map<String, ?> value) throws OpenDataException{
            return new CompositeDataSupport((CompositeType)getOpenType(), value);
        }

        @Override
        protected CompositeData getValue(final CommandExecutionChannel channel, final Map<String, ?> channelParams) throws IOException, ScriptException, OpenDataException {
            final Map<String, ?> dict = toTypeToken(commandProfile.readFromChannel(channel, channelParams), TypeTokens.DICTIONARY_TYPE_TOKEN);
            return dict != null ? convert(dict) : null;
        }
    }

    private static final class RShellAttributes extends AbstractAttributeRepository<RShellAttributeInfo> {
        private final CommandExecutionChannel executionChannel;
        private final ScriptEngineManager scriptEngineManager;

        private RShellAttributes(final String resourceName,
                                 final CommandExecutionChannel channel,
                                 final ScriptEngineManager engineManager) {
            super(resourceName, RShellAttributeInfo.class, false);
            this.executionChannel = Objects.requireNonNull(channel);
            this.scriptEngineManager = engineManager;
        }

        /**
         * Connects to the specified attribute.
         *
         * @param attributeName The id of the attribute.
         * @param descriptor  Attribute descriptor.
         * @return The description of the attribute.
         * @throws Exception Internal connector error.
         */
        @Override
        protected RShellAttributeInfo connectAttribute(final String attributeName,
                                                       final AttributeDescriptor descriptor) throws Exception {
            final String commandProfileFilePath = descriptor.getName(attributeName);
            final XmlCommandLineToolProfile profile = XmlCommandLineToolProfile.loadFrom(new File(commandProfileFilePath));
            if (profile != null) {
                profile.setScriptManager(scriptEngineManager);
                switch (profile.getReaderTemplate().getCommandOutputParser().getParsingResultType()) {
                    case DICTIONARY:
                        return new DictionaryAttributeInfo(attributeName, profile, descriptor);
                    case TABLE:
                        return new TableAttributeInfo(attributeName, profile, descriptor);
                    default:
                        return new SimpleAttributeInfo(attributeName, profile, descriptor);
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

    private static final class RShellOperations extends AbstractOperationRepository<RShellOperationInfo> {
            private final CommandExecutionChannel executionChannel;
            private final ScriptEngineManager scriptEngineManager;

            private RShellOperations(final String resourceName,
                                     final CommandExecutionChannel channel,
                                     final ScriptEngineManager engineManager) {
                super(resourceName, RShellOperationInfo.class, false);
                this.executionChannel = Objects.requireNonNull(channel);
                this.scriptEngineManager = engineManager;
            }

            @Override
            protected RShellOperationInfo connectOperation(String operationName, OperationDescriptor descriptor) throws Exception {
                final String commandProfileFilePath = descriptor.getName(operationName);
                final XmlCommandLineToolProfile profile = XmlCommandLineToolProfile.loadFrom(new File(commandProfileFilePath));
                if (profile != null) {
                    profile.setScriptManager(scriptEngineManager);
                    switch (profile.getReaderTemplate().getCommandOutputParser().getParsingResultType()) {
                        case DICTIONARY:
                            return new DictionaryOperationInfo(operationName, profile, descriptor);
                        case TABLE:
                            return new TableOperationInfo(operationName, profile, descriptor);
                        default:
                            return new SimpleOperationInfo(operationName, profile, descriptor);
                    }
                } else
                    throw new FileNotFoundException(commandProfileFilePath + " RShell command profile doesn't exist");
            }

            @Override
            protected Object invoke(OperationCallInfo<RShellOperationInfo> callInfo) throws Exception {
                return callInfo.getOperation().invoke(executionChannel, callInfo.toNamedArguments());
            }
    }

    private final CommandExecutionChannel executionChannel;

    @Aggregation(cached = true)
    private final RShellAttributes attributes;
    @Aggregation(cached = true)
    private final RShellOperations operations;

    RShellResourceConnector(final String resourceName,
                            final ManagedResourceInfo configuration) throws Exception{
        super(configuration);
        final RShellConnectionOptions connectionOptions = new RShellConnectionOptions(configuration.getConnectionString(), configuration);
        executionChannel = connectionOptions.createExecutionChannel();
        if(executionChannel == null)
            throw new InstantiationException(String.format("Unknown channel: %s", connectionOptions));
        attributes = new RShellAttributes(resourceName,
                executionChannel,
                new OSGiScriptEngineManager(Utils.getBundleContextOfObject(this)));
        operations = new RShellOperations(resourceName,
                executionChannel,
                new OSGiScriptEngineManager(Utils.getBundleContextOfObject(this)));
    }

    @Override
    protected MetricsSupport createMetricsReader() {
        return assembleMetricsReader(attributes, operations);
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
        addResourceEventListener(listener, attributes, operations);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes, operations);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        attributes.close();
        operations.close();
        super.close();
        executionChannel.close();
    }
}
