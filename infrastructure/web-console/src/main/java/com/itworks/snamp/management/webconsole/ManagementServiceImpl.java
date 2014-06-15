package com.itworks.snamp.management.webconsole;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.configuration.ConfigurationManager;
import com.itworks.snamp.connectors.AbstractManagedResourceActivator;
import com.itworks.snamp.licensing.LicenseReader;
import com.itworks.snamp.management.SnampManager;
import com.sun.jersey.spi.resource.Singleton;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.ws.rs.*;
import java.io.*;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Path("/")
@Singleton
public final class ManagementServiceImpl {
    private final ConfigurationManager configManager;
    private final SnampManager snampManager;

    public ManagementServiceImpl(final ConfigurationManager configManager,
                                 final SnampManager snampManager){
        this.configManager = configManager;
        this.snampManager = snampManager;
    }

    private static Gson createSerializer(){
        return new Gson();
    }

    /**
     * Gets SNAMP configuration in JSON format.
     * @return SNAMP configuration in JSON format.
     */
    @GET
    @Path("/configuration")
    @Produces("application/json")
    public String getConfiguration() {
        final Gson jsonSerializer = new Gson();
        return jsonSerializer.toJson(JsonAgentConfiguration.read(configManager.getCurrentConfiguration()));
    }

    /**
     * Stores SNAMP configuration.
     *
     * @param value SNAMP configuration in JSON format.
     */
    @POST
    @Path("/configuration")
    @Consumes("application/json")
    public void setConfiguration(final String value) {
        final JsonParser parser = new JsonParser();
        JsonAgentConfiguration.write(parser.parse(value), configManager.getCurrentConfiguration());
        configManager.sync();
    }

    private static void restart(final BundleContext context) throws BundleException {
        //first, stop all adapters
        AbstractResourceAdapterActivator.stopResourceAdapters(context);
        //second, stop all connectors
        AbstractManagedResourceActivator.stopResourceConnectors(context);
        //third, start all connectors
        AbstractManagedResourceActivator.startResourceConnectors(context);
        //fourth, start all adapters
        AbstractResourceAdapterActivator.startResourceAdapters(context);
    }

    /**
     * Restarts all adapters and connectors.
     */
    @POST
    @Path("/restart")
    public void restart() throws BundleException {
        restart(getBundleContextByObject(this));
    }

    /**
     * Gets path to the SNAMP license file.
     * @return A path to the SNAMP license file.
     */
    public static String getLicenseFile(){
        return System.getProperty(LicenseReader.LICENSE_FILE_PROPERTY, "./snamp.lic");
    }

    @GET
    @Path("/license")
    @Produces("application/xml")
    public String getLicense() throws IOException{
        final StringBuilder result = new StringBuilder(14);
        try(final InputStreamReader is = new InputStreamReader(new FileInputStream(getLicenseFile()), LicenseReader.LICENSE_FILE_ENCODING)){
            final char[] buffer = new char[1024];
            int count;
            while ((count = is.read(buffer)) > 0)
                result.append(buffer, 0, count);
        }
        return result.toString();
    }

    @POST
    @Path("/license")
    @Consumes("application/xml")
    public void setLicense(final String licenseContent) throws IOException{
        try(final OutputStream os = new FileOutputStream(getLicenseFile(), false)){
            os.write(licenseContent.getBytes(LicenseReader.LICENSE_FILE_ENCODING));
        }
    }

    @Path("/installed-components")
    @GET
    @Produces("application/xml")
    public String getInstalledComponents(){
        final JsonObject result = new JsonObject();
        final String ADAPTERS_SECTION = "adapters";
        final String CONNECTORS_SECTION = "connectors";
        final String COMPONENTS_SECTION = "components";

        return createSerializer().toJson(result);
    }
}
