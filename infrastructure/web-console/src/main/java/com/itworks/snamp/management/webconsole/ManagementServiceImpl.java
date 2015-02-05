package com.itworks.snamp.management.webconsole;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.itworks.snamp.Box;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.adapters.SelectableAdapterParameterDescriptor;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.connectors.SelectableConnectorParameterDescriptor;
import com.itworks.snamp.connectors.discovery.DiscoveryService;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.internal.TransformerClosure;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.licensing.LicensingDescriptionService;
import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.management.SnampManager;
import com.itworks.snamp.security.LoginConfigurationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Path("/")
@Singleton
public final class ManagementServiceImpl {
    private static final String LOGGER_NAME = "com.itworks.snamp.management";
    private static final String CONNECTION_STRING_QUERY_PARAM = "connectionString";
    private static final String LOCALE_QUERY_PARAM = "locale";
    private final PersistentConfigurationManager configManager;
    private final SnampManager snampManager;
    private final Gson jsonFormatter;
    private final JsonParser jsonParser;

    ManagementServiceImpl(final PersistentConfigurationManager configManager,
                          final SnampManager snampManager){
        this.configManager = Objects.requireNonNull(configManager);
        this.snampManager = Objects.requireNonNull(snampManager);
        jsonFormatter = new Gson();
        jsonParser = new JsonParser();
    }

    /**
     * Gets SNAMP configuration in JSON format.
     * @return SNAMP configuration in JSON format.
     * @todo move as property
     */
    @GET
    @Path("/configuration")
    @Produces(MediaType.APPLICATION_JSON)
    public String getConfiguration() throws WebApplicationException{
        try {
            configManager.load();
        }
        catch (final IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return jsonFormatter.toJson(JsonAgentConfiguration.read(configManager.getCurrentConfiguration()));
    }

    /**
     * Stores SNAMP configuration.
     *
     * @param value SNAMP configuration in JSON format.
     *              * @todo move as property
     */
    @POST
    @Path("/configuration")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setConfiguration(final String value,
                                 @Context final SecurityContext context) throws WebApplicationException{
        SecurityUtils.adminRequired(context);
        JsonAgentConfiguration.write(jsonParser.parse(value), configManager.getCurrentConfiguration());
        try {
            configManager.save();
        } catch (final IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private static void restart(final BundleContext context) throws BundleException {
        //first, stop all adapters
        ResourceAdapterActivator.stopResourceAdapters(context);
        //second, stop all connectors
        ManagedResourceActivator.stopResourceConnectors(context);
        //third, start all connectors
        ManagedResourceActivator.startResourceConnectors(context);
        //fourth, start all adapters
        ResourceAdapterActivator.startResourceAdapters(context);
    }

    /**
     * Restarts all adapters and connectors.
     */
    @POST
    @Path("/restart")
    // todo move as operation
    public void restart(@Context final SecurityContext context) throws WebApplicationException {
        SecurityUtils.adminRequired(context);
        try {
            restart(getBundleContextByObject(this));
        }
        catch (final BundleException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }



    @GET
    @Path("/license")
    @Produces(MediaType.APPLICATION_XML)
    // todo move as property
    public String getLicense(final @Context SecurityContext context) throws WebApplicationException {
        SecurityUtils.adminRequired(context);
        try {
            return LicenseManager.getLicenseContent(getBundleContextByObject(this));
        } catch (final IOException e) {
            throw new WebApplicationException(e);
        }
    }

    @POST
    @Path("/license")
    @Consumes(MediaType.APPLICATION_XML)
    // todo move as property
    public void setLicense(final String licenseContent, @Context final SecurityContext context) throws WebApplicationException{
        SecurityUtils.adminRequired(context);
        try{
            LicenseManager.setLicenseContent(getBundleContextByObject(this), licenseContent);
        }
        catch (final IOException e) {
            throw new WebApplicationException(e);
        }
    }

    private static JsonObject getConfigurationSchema(final ConfigurationEntityDescription<?> description, final Locale loc){
        final JsonObject result = new JsonObject();
        if(description != null)
            for(final String parameterName: description){
                final JsonObject parameter = new JsonObject();
                final ConfigurationEntityDescription.ParameterDescription descriptor = description.getParameterDescriptor(parameterName);
                parameter.addProperty("defaultValue", descriptor.getDefaultValue(loc));
                parameter.addProperty("description", descriptor.getDescription(loc));
                parameter.addProperty("inputPattern", descriptor.getValuePattern(loc));
                parameter.addProperty("required", descriptor.isRequired());
                parameter.addProperty("suggestionSupported", description instanceof SelectableAdapterParameterDescriptor || description instanceof SelectableConnectorParameterDescriptor);
                //related params
                for(final ConfigurationEntityDescription.ParameterRelationship rel: ConfigurationEntityDescription.ParameterRelationship.values()){
                    final JsonArray relationship = new JsonArray();
                    for(final String relatedParameter: descriptor.getRelatedParameters(rel))
                        relationship.add(new JsonPrimitive(relatedParameter));
                    parameter.add(rel.name(), relationship);
                }
                result.add(parameterName, parameter);
            }
        return result;
    }

    private static JsonObject getConfigurationSchema(final ConfigurationEntityDescriptionProvider schemaProvider, final Locale loc){
       final JsonObject result = new JsonObject();
       result.add("managedResourceParameters", getConfigurationSchema(schemaProvider.getDescription(ManagedResourceConfiguration.class), loc));
       result.add("resourceAdapterParameters", getConfigurationSchema(schemaProvider.getDescription(ResourceAdapterConfiguration.class), loc));
       result.add("attributeParameters", getConfigurationSchema(schemaProvider.getDescription(ManagedResourceConfiguration.AttributeConfiguration.class), loc));
       result.add("eventParameters", getConfigurationSchema(schemaProvider.getDescription(ManagedResourceConfiguration.EventConfiguration.class), loc));
       return result;
    }

    private static String getConfigurationSchema(final SnampComponentDescriptor component,
                                                 final Gson jsonFormatter,
                                                 final String locale) throws Exception {
        final TransformerClosure<ConfigurationEntityDescriptionProvider, String> closure = new TransformerClosure<ConfigurationEntityDescriptionProvider, String>("{}") {
            @Override
            public String apply(final ConfigurationEntityDescriptionProvider input) {
                final JsonObject result = getConfigurationSchema(input, locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale));
                return jsonFormatter.toJson(result);
            }
        };
        component.invokeSupportService(ConfigurationEntityDescriptionProvider.class, closure);
        return closure.get();
    }

    //example of configuration schema:
/*
    {
    "managedResourceParameters": {},
    "resourceAdapterParameters": {},
    "attributeParameters": {
        "objectName": {
            "defaultValue": "",
            "description": "Represents MBean object name that exposes the management attribute",
            "inputPattern": "",
            "required": true,
            "ASSOCIATION": [],
            "EXTENSION": [],
            "EXCLUSION": []
        }
    },
    "eventParameters": {}

     */
    @GET
    @Path("/connectors/{connectorType}/configurationSchema")
    @Produces(MediaType.APPLICATION_JSON)
    // move as composite data object
    public String getConnectorConfigurationSchema(@PathParam("connectorType") final String connectorName,
                                                  @QueryParam(LOCALE_QUERY_PARAM)final String locale,
                                                  @Context final SecurityContext context) throws WebApplicationException {
        SecurityUtils.adminRequired(context);
        final SnampComponentDescriptor connector = getResourceConnector(snampManager, connectorName);
        if(connector == null) throw connectorNotFound(connectorName);
        else try {
            return getConfigurationSchema(connector, jsonFormatter, locale);
        }
        catch (final IllegalStateException e){
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        catch (final Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private static JsonObject getComponentInfo(final SnampComponentDescriptor component, final Locale loc) throws Exception{
        final JsonObject result = new JsonObject();
        result.addProperty("Version", Objects.toString(component.getVersion(), "0.0"));
        result.addProperty("State", component.getState());
        result.addProperty("DisplayName", component.getName(loc));
        result.addProperty("Description", component.getDescription(loc));
        component.invokeSupportService(LicensingDescriptionService.class, new SafeConsumer<LicensingDescriptionService>() {
            @Override
            public void accept(final LicensingDescriptionService input) {
                final JsonObject limitations = new JsonObject();
                for (final String limitation : input.getLimitations())
                    limitations.addProperty(limitation, input.getDescription(limitation, loc));
                result.add("Licensing", limitations);
            }
        });
        return result;
    }

    @GET
    @Path("/connectors")
    @Produces(MediaType.APPLICATION_JSON)
    // move as attribute (read only if no setters exist)
    public String getInstalledConnectors(@Context final SecurityContext context){
        SecurityUtils.wellKnownRoleRequired(context);
        final JsonArray result = new JsonArray();
        for(final String connector: ManagedResourceActivator.getInstalledResourceConnectors(Utils.getBundleContextByObject(this)))
            result.add(new JsonPrimitive(connector));
        return jsonFormatter.toJson(result);
    }

    @GET
    @Path("/connectors/{connectorType}")
    // move as operation (due to existence params)
    public String getConnectorInfo(@PathParam("connectorType")final String connectorName,
                                   @QueryParam(LOCALE_QUERY_PARAM)final String locale,
                                   @Context final SecurityContext context) throws WebApplicationException {
        SecurityUtils.wellKnownRoleRequired(context);
        final SnampComponentDescriptor connector = getResourceConnector(snampManager, connectorName);
        if (connector == null)
            throw connectorNotFound(connectorName);
        else try {
            return jsonFormatter.toJson(getComponentInfo(connector, locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale)));
        }
        catch (final IllegalStateException e){
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        catch (final Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private static SnampComponentDescriptor getResourceConnector(final SnampManager snampManager,
                                                                 final String connectorName){
        for(final SnampComponentDescriptor connector: snampManager.getInstalledResourceConnectors())
            if(Objects.equals(connectorName, connector.get(SnampComponentDescriptor.CONNECTOR_SYSTEM_NAME_PROPERTY)))
                return connector;
        return null;
    }

    private static WebApplicationException connectorNotFound(final String connectorName){
        return new WebApplicationException(new IllegalArgumentException(String.format("Connector %s doesn't exist", connectorName)), Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/adapters")
    @Produces(MediaType.APPLICATION_JSON)
    // move as read only attribute
    public String getInstalledAdapters(@Context SecurityContext context){
        SecurityUtils.wellKnownRoleRequired(context);
        final JsonArray result = new JsonArray();
        for(final String adapter: ResourceAdapterActivator.getInstalledResourceAdapters(Utils.getBundleContextByObject(this)))
            result.add(new JsonPrimitive(adapter));
        return jsonFormatter.toJson(result);
    }

    private static WebApplicationException adapterNotFoundException(final String adapterName){
        return new WebApplicationException(new IllegalArgumentException(String.format("Adapter %s doesn't exist", adapterName)), Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/adapters/{adapterName}")
    public String getAdapterInfo(@PathParam("adapterName")final String adapterName,
                                 @QueryParam(LOCALE_QUERY_PARAM)final String locale,
                                 @Context SecurityContext context) throws WebApplicationException {
        // all the exception should be wrapped into JMException
        SecurityUtils.wellKnownRoleRequired(context);
        final SnampComponentDescriptor adapter = getResourceAdapter(snampManager, adapterName);
        if (adapter == null)
            throw adapterNotFoundException(adapterName);
        else try {
            return jsonFormatter.toJson(getComponentInfo(adapter, locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale)));
        }
        catch (final IllegalStateException e){
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        catch (final Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private static SnampComponentDescriptor getResourceAdapter(final SnampManager snampManager,
                                                               final String adapterName){
        for(final SnampComponentDescriptor adapter: snampManager.getInstalledResourceAdapters())
            if(Objects.equals(adapterName, adapter.get(SnampComponentDescriptor.ADAPTER_SYSTEM_NAME_PROPERTY)))
                return adapter;
        return null;
    }

    @GET
    @Path("/adapters/{adapterName}/configurationSchema")
    @Produces(MediaType.APPLICATION_JSON)
    // operation
    public String getAdapterConfigurationSchema(@PathParam("adapterName")final String adapterName,
                                                @QueryParam(LOCALE_QUERY_PARAM)final String locale,
                                                @Context SecurityContext context) throws WebApplicationException {
        SecurityUtils.adminRequired(context);
        final SnampComponentDescriptor adapter = getResourceAdapter(snampManager, adapterName);
        if (adapter == null) throw adapterNotFoundException(adapterName);
        else try {
            return getConfigurationSchema(adapter, jsonFormatter, locale);
        }
        catch (final IllegalStateException e){
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        catch (final Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/connectors/{connectorType}/configurationSchema/attribute/{parameterName}")
    @Produces(MediaType.APPLICATION_JSON)
    // operation
    public String suggestConnectorAttributeParameterValues(@PathParam("connectorType")final String connectorName,
                                                            @PathParam("parameterName")final String parameterName,
                                                            @QueryParam(LOCALE_QUERY_PARAM)final String locale,
                                                            @QueryParam(CONNECTION_STRING_QUERY_PARAM) final String connectionString,
                                                            @Context final UriInfo url,
                                                            @Context final SecurityContext context) throws WebApplicationException{
        SecurityUtils.wellKnownRoleRequired(context);
        return suggestParameterValues(connectorName,
                parameterName,
                locale,
                connectionString,
                url.getQueryParameters(true),
                ManagedResourceConfiguration.AttributeConfiguration.class);
    }

    @GET
    @Path("/connectors/{connectorType}/configurationSchema/event/{parameterName}")
    @Produces(MediaType.APPLICATION_JSON)
    // operation (array in return)

    public String suggestConnectorEventParameterValues(@PathParam("connectorType")final String connectorName,
                                                       @PathParam("parameterName")final String parameterName,
                                                       @QueryParam(LOCALE_QUERY_PARAM)final String locale,
                                                       @QueryParam(CONNECTION_STRING_QUERY_PARAM)final String connectionString,
                                                       @Context final UriInfo url,
                                                       @Context final SecurityContext context) throws WebApplicationException{
        SecurityUtils.wellKnownRoleRequired(context);
        return suggestParameterValues(connectorName,
                parameterName,
                locale,
                connectionString,
                url.getQueryParameters(true),
                ManagedResourceConfiguration.EventConfiguration.class);
    }

    private static Map<String, JsonElement> deserializeQueryParams(final JsonParser parser,
                                                                   final MultivaluedMap<String, String> queryParams,
                                                                   final Set<String> exclusion){
        final Map<String, JsonElement> result = Maps.newHashMapWithExpectedSize(queryParams.size());
        for(final String param: queryParams.keySet())
            if(!exclusion.contains(param))
                result.put(param, parser.parse(queryParams.getFirst(param)));
        return result;
    }

    private String suggestParameterValues(final String connectorName,
                                          final String parameterName,
                                          final String locale,
                                          final String connectionString,
                                          final MultivaluedMap<String, String> queryParams,
                                          final Class<? extends AgentConfiguration.ConfigurationEntity> configurationEntity) throws WebApplicationException{
        final SnampComponentDescriptor connector = getResourceConnector(snampManager, connectorName);
        final Map<String, JsonElement> connectionOptions = deserializeQueryParams(jsonParser, queryParams, ImmutableSet.of(LOCALE_QUERY_PARAM, CONNECTION_STRING_QUERY_PARAM));
        if (connector == null) throw connectorNotFound(connectorName);
        else try {
            final Box<JsonArray> result = new Box<>(new JsonArray());
            connector.invokeSupportService(ConfigurationEntityDescriptionProvider.class, new Consumer<ConfigurationEntityDescriptionProvider, Exception>() {
                @Override
                public void accept(final ConfigurationEntityDescriptionProvider input) throws Exception {
                    final ConfigurationEntityDescription<?> description = input.getDescription(configurationEntity);
                    if (description != null)
                        result.set(JsonAgentConfiguration.getSuggestedValues(description.getParameterDescriptor(parameterName),
                                jsonParser.parse(connectionString),
                                connectionOptions,
                                locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale)));
                }
            });
            return jsonFormatter.toJson(result.get());
        }
        catch (final IllegalStateException e){
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        catch (final Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/connectors/{connectorType}/configurationSchema/{parameterName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String suggestConnectorParameterValues(@PathParam("connectorType")final String connectorName,
                                                  @PathParam("parameterName")final String parameterName,
                                                  @QueryParam(LOCALE_QUERY_PARAM)final String locale,
                                                  @QueryParam(CONNECTION_STRING_QUERY_PARAM)final String connectionString,
                                                  @Context final UriInfo url,
                                                  @Context final SecurityContext context) throws WebApplicationException{
        SecurityUtils.wellKnownRoleRequired(context);
        return suggestParameterValues(connectorName,
                parameterName,
                locale,
                connectionString,
                url.getQueryParameters(true),
                ManagedResourceConfiguration.class);
    }

    @GET
    @Path("/connectors/{connectorType}/availableMetadata")
    @Produces(MediaType.APPLICATION_JSON)
    //
    public String discoverManagementMetadata(@PathParam("connectorType")final String connectorName,
                                             @QueryParam(CONNECTION_STRING_QUERY_PARAM)final String connectionString,
                                             @QueryParam(LOCALE_QUERY_PARAM)final String locale,
                                             @Context final UriInfo url,
                                             @Context final SecurityContext context) throws WebApplicationException {
        SecurityUtils.wellKnownRoleRequired(context);
        final Map<String, JsonElement> connectionParams = deserializeQueryParams(jsonParser,
                url.getQueryParameters(true),
                ImmutableSet.of(CONNECTION_STRING_QUERY_PARAM, LOCALE_QUERY_PARAM));
        final SnampComponentDescriptor connector = getResourceConnector(snampManager, connectorName);
        if (connector == null) throw connectorNotFound(connectorName);
        else try {
            final Box<JsonObject> result = new Box<>(new JsonObject());
            connector.invokeSupportService(DiscoveryService.class, new Consumer<DiscoveryService, Exception>() {
                @Override
                public void accept(final DiscoveryService input) throws Exception {
                    result.set(JsonAgentConfiguration.discoverManagementMetadata(
                            input,
                            jsonParser.parse(connectionString),
                            connectionParams));
                }
            });
            return jsonFormatter.toJson(result.get());
        } catch (final IllegalStateException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        } catch (final Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/adapters/{adapterName}/configurationSchema/{parameterName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String suggestAdapterParameterValues(@PathParam("adapterName")final String adapterName,
                                                @PathParam("parameterName")final String parameterName,
                                                @QueryParam(LOCALE_QUERY_PARAM)final String locale,
                                                @Context final UriInfo url,
                                                @Context final SecurityContext context) throws WebApplicationException {
        SecurityUtils.wellKnownRoleRequired(context);
        final Map<String, JsonElement> adapterOptions = deserializeQueryParams(jsonParser,
                url.getQueryParameters(true),
                ImmutableSet.of(LOCALE_QUERY_PARAM));
        final SnampComponentDescriptor adapter = getResourceAdapter(snampManager, adapterName);
        if (adapter == null) throw adapterNotFoundException(adapterName);
        else try {
            final Box<JsonArray> result = new Box<>(new JsonArray());
            adapter.invokeSupportService(ConfigurationEntityDescriptionProvider.class, new Consumer<ConfigurationEntityDescriptionProvider, Exception>() {
                @Override
                public void accept(final ConfigurationEntityDescriptionProvider input) throws Exception {
                    final ConfigurationEntityDescription<?> description = input.getDescription(ResourceAdapterConfiguration.class);
                    if (description != null)
                        result.set(JsonAgentConfiguration.getSuggestedValues(description.getParameterDescriptor(parameterName),
                                adapterOptions,
                                locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale)));
                }
            });
            return jsonFormatter.toJson(result.get());
        }
        catch (final IllegalStateException e){
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        catch (final Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/components")
    @Produces(MediaType.APPLICATION_JSON)
    public String getInstalledComponents(@QueryParam("locale")final String locale,
                                         @Context final SecurityContext context) throws WebApplicationException{
        SecurityUtils.wellKnownRoleRequired(context);
        final JsonArray result = new JsonArray();
        final Locale loc = locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale);
        try {
            for (final SnampComponentDescriptor connector : snampManager.getInstalledResourceConnectors())
                result.add(getComponentInfo(connector, loc));
            for (final SnampComponentDescriptor adapter : snampManager.getInstalledResourceAdapters())
                result.add(getComponentInfo(adapter, loc));
            for (final SnampComponentDescriptor component : snampManager.getInstalledComponents())
                result.add(getComponentInfo(component, loc));
        }
        catch (final IllegalStateException e){
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        catch (final Exception e){
            throw new WebApplicationException(e);
        }
        return jsonFormatter.toJson(result);
    }

    @Path("/connectors/{connectorType}/start")
    @POST
    public void startConnector(@PathParam("connectorType")final String connectorType,
                               final String reason,
                               @Context final SecurityContext context) throws WebApplicationException{
        SecurityUtils.adminRequired(context);
        try(final OSGiLoggingContext logger = getLoggingContext()) {
            ManagedResourceActivator.startResourceConnector(getBundleContextByObject(this), connectorType);
                logger.info(String.format("User %s starts the %s connectors. Reason: %s",
                        context.getUserPrincipal().getName(),
                        connectorType,
                        reason));
        } catch (BundleException e) {
            throw new WebApplicationException(e);
        }
    }

    @Path("/connectors/{connectorType}/stop")
    @POST
    public void stopConnector(@PathParam("connectorType")final String connectorType,
                              final String reason,
                              @Context final SecurityContext context) throws WebApplicationException{
        SecurityUtils.adminRequired(context);
        try(final OSGiLoggingContext logger = getLoggingContext()) {
            ManagedResourceActivator.stopResourceConnector(getBundleContextByObject(this), connectorType);
            logger.info(String.format("User %s stops the %s connectors. Reason: %s",
                        context.getUserPrincipal().getName(),
                        connectorType,
                        reason));
        } catch (final BundleException e) {
            throw new WebApplicationException(e);
        }
    }

    @Path("/adapters/{adapterName}/start")
    @POST
    public void startAdapter(@PathParam("adapterName")final String adapterName,
                             final String reason,
                             @Context final SecurityContext context) throws WebApplicationException{
        SecurityUtils.adminRequired(context);
        try(final OSGiLoggingContext logger = getLoggingContext()) {
            ResourceAdapterActivator.startResourceAdapter(getBundleContextByObject(this), adapterName);
            logger.info(String.format("User %s starts the %s adapter. Reason: %s",
                    context.getUserPrincipal().getName(),
                    adapterName,
                    reason));
        } catch (final BundleException e) {
            throw new WebApplicationException(e);
        }
    }

    @Path("/adapters/{adapterName}/stop")
    @POST
    public void stopAdapter(@PathParam("adapterName")final String adapterName,
                            final String reason,
                            @Context final SecurityContext context){
        SecurityUtils.adminRequired(context);
        try(final OSGiLoggingContext logger = getLoggingContext()) {
            ResourceAdapterActivator.stopResourceAdapter(getBundleContextByObject(this), adapterName);
            logger.info(String.format("User %s stops the %s adapter. Reason: %s",
                    context.getUserPrincipal().getName(),
                    adapterName,
                    reason));
        } catch (final BundleException e) {
            throw new WebApplicationException(e);
        }
    }

    private OSGiLoggingContext getLoggingContext(){
        return OSGiLoggingContext.getLogger(LOGGER_NAME, getBundleContextByObject(this));
    }

    @GET
    @Path("/jaas/config")
    @Produces(MediaType.WILDCARD)
    // operation return byteArray
    public Response getJaasConfig(@Context final SecurityContext security) {
        SecurityUtils.adminRequired(security);
        final BundleContext context = getBundleContextByObject(this);
        final ServiceReference<LoginConfigurationManager> managerRef =
                context.getServiceReference(LoginConfigurationManager.class);
        if (managerRef == null) return Response.status(Response.Status.NOT_FOUND).build();
        final ServiceReferenceHolder<LoginConfigurationManager> manager = new ServiceReferenceHolder<>(context, managerRef);
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
            manager.get().dumpConfiguration(out);
            final byte[] content = out.toByteArray();
            return Response.ok(content,
                    Objects.toString(manager.getProperty(LoginConfigurationManager.CONFIGURATION_MIME_TYPE), MediaType.APPLICATION_OCTET_STREAM))
                    .header(HttpHeaders.CONTENT_LENGTH, content.length).build();
        } catch (final Exception e) {
            return Response.serverError().entity(e.toString()).build();
        } finally {
            manager.release(context);
        }
    }

    @POST
    @Path("/jaas/config")
    @Consumes(MediaType.WILDCARD)
    // setter byteArray type of argument
    public void setJaasConfig(@Context final SecurityContext security,
                              @Context final HttpHeaders headers,
                              final InputStream content) throws WebApplicationException{
        SecurityUtils.adminRequired(security);
        final BundleContext context = getBundleContextByObject(this);
        final ServiceReference<LoginConfigurationManager> managerRef =
                context.getServiceReference(LoginConfigurationManager.class);
        if (managerRef == null) throw new WebApplicationException(Response.Status.NOT_FOUND);
        final ServiceReferenceHolder<LoginConfigurationManager> manager = new ServiceReferenceHolder<>(context, managerRef);
        try {
            final MediaType actualContentType = headers.getMediaType();
            final MediaType requiredContentType =
                    MediaType.valueOf(Objects.toString(manager.getProperty(LoginConfigurationManager.CONFIGURATION_MIME_TYPE), MediaType.APPLICATION_OCTET_STREAM));
            if(!requiredContentType.isCompatible(actualContentType))
                throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
            manager.get().loadConfiguration(content);
        }
        catch (final IOException | IllegalArgumentException e){
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        catch (final Exception e) {
            throw new WebApplicationException(e);
        } finally {
            manager.release(context);
        }
    }
}
