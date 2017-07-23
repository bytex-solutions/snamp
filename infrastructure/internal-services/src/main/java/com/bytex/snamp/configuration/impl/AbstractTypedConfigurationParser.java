package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.BooleanBox;
import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.TypedEntityConfiguration;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ImmutableSet;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.bytex.snamp.MapUtils.getValue;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterators.forEnumeration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractTypedConfigurationParser<E extends SerializableEntityConfiguration & TypedEntityConfiguration> extends AbstractConfigurationParser<E> {
    private final String identityHolderName;

    AbstractTypedConfigurationParser(final String identityHolder,
                                     final SerializableEntityMapResolver<SerializableAgentConfiguration, E> resolver) {
        super(resolver);
        identityHolderName = identityHolder;
    }

    abstract String getFactoryPersistentID(final String entityType);

    private String getFactoryPersistentID(final TypedEntityConfiguration entity) {
        return isNullOrEmpty(entity.getType()) ? null : getFactoryPersistentID(entity.getType());
    }

    private String getIdentityName(final Dictionary<String, ?> config) {
        return getValue(config, identityHolderName, Objects::toString).orElse("");
    }

    @Nonnull
    abstract Dictionary<String, Object> serialize(final E entity) throws IOException;

    private void serialize(final String identityName,
                                  final E entity,
                                  final Configuration output) throws IOException {
        final Dictionary<String, Object> configuration = serialize(entity);
        configuration.put(identityHolderName, identityName);
        entity.forEach((name, value) -> {
            if (!IGNORED_PROPERTIES.contains(name))
                configuration.put(name, value);
        });
        output.update(configuration);
    }

    private String createIdentityFilter(final String identityName, final E entity){
        final String factoryPID = getFactoryPersistentID(entity);
        return String.format("(&(%s=%s)(%s=%s))", identityHolderName, identityName, ConfigurationAdmin.SERVICE_FACTORYPID, factoryPID);
    }

    private void serialize(final String identityName, final E entity, final ConfigurationAdmin admin) throws IOException {
        //find existing configuration of gateway
        final BooleanBox updated = BooleanBox.of(false);
        forEachConfiguration(admin, createIdentityFilter(identityName, entity), config -> {
            serialize(identityName, entity, config);
            updated.set(true);
        });
        //no existing configuration, creates a new configuration
        if (!updated.get()) {
            final String persistentID = getFactoryPersistentID(entity);
            if (isNullOrEmpty(persistentID))
                throw new IOException(String.format("Configuration entity %s could not be saved because its type is not specified", identityName));
            else
                serialize(identityName,
                        entity,
                        admin.createFactoryConfiguration(persistentID, null));
        }
    }

    abstract String getType(final Configuration config);

    private void populateRepository(final Configuration config, final Map<String, E> output) throws IOException {
        final Dictionary<String, ?> properties = config.getProperties();
        final SingletonMap<String, E> instance;
        if (properties == null)
            return;
        else
            instance = parse(properties);
        instance.getValue().setType(getType(config));
        instance.getValue().reset();
        assert !instance.getValue().isModified();
        output.putAll(instance);
    }

    final void populateRepository(final ConfigurationAdmin admin,
                                  final String filter,
                                  final EntityMap<E> output) throws IOException {
        forEachConfiguration(admin, filter, config -> populateRepository(config, output));
    }

    private static void fillProperties(final Dictionary<String, ?> input,
                                       final Map<String, String> output,
                                       final Set<String> ignoredProperties) {
        forEnumeration(input.keys()).forEachRemaining(name -> {
            if (ignoredProperties.contains(name) || IGNORED_PROPERTIES.contains(name)) return;
            final Object value = input.get(name);
            if (value != null)
                output.put(name, value.toString());
        });
    }

    final SingletonMap<String, E> createParserResult(final Dictionary<String, ?> configuration,
                                                     final E entity,
                                                     final String... ignoredProperties) {
        entity.reset();
        fillProperties(configuration, entity, ImmutableSet.<String>builder().add(ignoredProperties).add(identityHolderName).build());
        return new SingletonMap<>(getIdentityName(configuration), entity);
    }

    @Override
    @Nonnull
    abstract SingletonMap<String, E> parse(final Dictionary<String, ?> config) throws IOException;

    final void saveChanges(final SerializableEntityMap<E> resources,
                     final String filter,
                     final ConfigurationAdmin admin) throws IOException {
        //remove all unnecessary entities
        forEachConfiguration(admin, filter, output -> {
            final String entityType = getType(output);
            final E resourceConfig = resources.get(getIdentityName(output.getProperties()));
            //delete entity if its type was changed
            if (resourceConfig == null || !Objects.equals(resourceConfig.getType(), entityType))
                output.delete();
        });
        //save each modified entity
        resources.modifiedEntries((resourceName, resource) -> {
            serialize(resourceName, resource, admin);
            return true;
        });
    }

    static void removeAll(final ConfigurationAdmin admin, final String filter) throws IOException {
        forEachConfiguration(admin, filter, Configuration::delete);
    }

    private static <E extends Exception> void forEachConfiguration(final ConfigurationAdmin admin,
                                                                   final String filter,
                                                                   final Acceptor<Configuration, E> reader) throws E, IOException {
        final Configuration[] configs = Utils.callAndWrapException(() -> admin.listConfigurations(filter), IOException::new);
        if (configs != null)
            for (final Configuration config : configs)
                reader.accept(config);
    }
}
