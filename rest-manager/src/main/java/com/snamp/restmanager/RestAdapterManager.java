package com.snamp.restmanager;

import com.snamp.hosting.AgentConfiguration;
import com.snamp.hosting.HostingContext;
import com.snamp.hosting.management.AgentManagerBase;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.*;
import com.snamp.hosting.*;

import javax.servlet.Servlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * User: agrishin85
 * Date: 11/3/13
 * Time: 7:11 PM
 * Rest Manager plugin
 */
final class RestAdapterManager extends AgentManagerBase {

    public static final String MANAGER_NAME = "restconfiguration";
    private final Server server;
    public RestAdapterManager(){
        super(MANAGER_NAME);
        server = new Server(8081);
    }

    private Servlet createRestServlet(final Agent agent, final AgentConfigurationStorage storage){
        return new RestAdapterManagerServlet(agent, storage);
    }

    private void initServer(final Server serv, final HostingContext context){
        Agent agent = context.queryObject(HostingContext.AGENT);
        AgentConfigurationStorage storage = context.queryObject(HostingContext.CONFIG_STORAGE);
        final ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        contextHandler.setContextPath("/snamp");
        final ServletHolder holder = new ServletHolder(createRestServlet(agent, storage));
        contextHandler.addServlet(holder, "/*");
        serv.setHandler(contextHandler);
    }

    @Override
    public void close() throws Exception {
        server.stop();
    }

    /**
     * Starts this manager.
     */
    @Override
    protected final void startCore(final HostingContext context) {
        initServer(server, context);
        try{
            server.start();
            server.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*try(final BufferedReader reader = new BufferedReader(new InputStreamReader(input))){
            while (doCommand(reader.readLine(), output)){
            }
        }
        catch (final IOException e) {
            errors.println(e);
        }       */
    }
}
