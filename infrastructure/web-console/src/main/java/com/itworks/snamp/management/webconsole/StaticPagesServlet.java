package com.itworks.snamp.management.webconsole;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.URLResource;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class StaticPagesServlet extends DefaultServlet {
    private static final class BundleResource extends URLResource{
        public BundleResource(final Class<?> callerClass){
            this(Loader.getResource(callerClass, "/pages"));
        }

        private BundleResource(final URL bundleURL){
            super(bundleURL, null, true);
        }

        //this override is required for org.eclipse.jetty.http.HttpContent$ResourceAsHttpContent.getDirectBuffer class
        //because it tries load resource from file
        //if this method returns, then Jetty will use getInputStream() instead
        @Override
        public File getFile() {
            return null;
        }

        @Override
        public Resource addPath(String path) throws IOException {
            if (path == null) return null;
            else path = URIUtil.canonicalPath(path);
            path = URIUtil.addPaths(_url.toExternalForm(), path);
            return new BundleResource(new URL(path));
        }
    }

    private final Resource baseResource;

    public StaticPagesServlet(){
        baseResource = new BundleResource(getClass());
    }

    @Override
    public final Resource getResource(final String pathInContext) {
        return baseResource.getResource(pathInContext);
    }

    @Override
    public final void destroy() {
        try {
            baseResource.close();
        }
        finally {
            super.destroy();
        }
    }
}