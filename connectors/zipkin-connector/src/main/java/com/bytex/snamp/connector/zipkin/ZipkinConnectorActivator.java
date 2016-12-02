package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.instrumentation.Identifier;
import com.bytex.snamp.io.Buffers;
import org.apache.log4j.Logger;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.powerassert.ValueRecorder;
import org.codehaus.groovy.transform.BaseScriptASTTransformation;
import org.codehaus.groovy.vmplugin.v7.IndyInterface;
import org.ietf.jgss.GSSException;
import org.osgi.service.http.HttpService;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.sasl.SaslException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Collects spans compatible with Twitter Zipkin.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@ImportClass({BaseScriptASTTransformation.class,
        ClassInfo.class,
        ValueRecorder.class,
        DefaultGroovyMethods.class,
        IndyInterface.class,
        Identifier.class,
        Buffers.class,
        ObjectName.class,
        Logger.class,
        LoggerFactory.class,
        SaslException.class,
        Subject.class,
        LoginException.class,
        GSSException.class,
        CallbackHandler.class})
public final class ZipkinConnectorActivator extends ManagedResourceActivator<ZipkinConnector> {
    private static final ActivationProperty<HttpService> HTTP_SERVICE_ACTIVATION_PROPERTY = defineActivationProperty(HttpService.class);

    @SpecialUse
    public ZipkinConnectorActivator(){
        super(ZipkinConnectorActivator::createConnector, configurationDescriptor(ZipkinConnectorConfigurationDescriptionProvider::getInstance));
    }

    private static ZipkinConnector createConnector(final String resourceName,
                                                   final String connectionString,
                                                   final Map<String, String> connectionParameters,
                                                   final RequiredService<?>... dependencies) throws URISyntaxException {
        return new ZipkinConnector(resourceName, connectionString, connectionParameters);
    }

    @Override
    protected void addDependencies(final Collection<RequiredService<?>> dependencies) {
        dependencies.add(new SimpleDependency<>(HttpService.class));
    }

    /**
     * Activates this service library.
     *
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         A collection of resolved library-level dependencies.
     * @throws Exception Unable to activate this library.
     */
    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {
        super.activate(activationProperties, dependencies);
        @SuppressWarnings("unchecked")
        final HttpService publisher = getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies);
        assert publisher != null;
        activationProperties.publish(HTTP_SERVICE_ACTIVATION_PROPERTY, publisher);
        //register servlet
        publisher.registerServlet(ZipkinServlet.CONTEXT, new ZipkinServlet(), new Hashtable<>(), null);
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) {
        final HttpService publisher = activationProperties.getProperty(HTTP_SERVICE_ACTIVATION_PROPERTY);
        publisher.unregister(ZipkinServlet.CONTEXT);
    }
}
