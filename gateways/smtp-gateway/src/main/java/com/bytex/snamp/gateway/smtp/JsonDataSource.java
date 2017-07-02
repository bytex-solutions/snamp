package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.json.JsonUtils;
import com.google.common.net.MediaType;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class JsonDataSource implements DataSource {
    private static final ObjectWriter JSON_SERIALIZER;

    static {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JsonUtils());
        JSON_SERIALIZER = mapper.writerWithDefaultPrettyPrinter();
    }

    private final String name;
    private final Object serializableObject;

    JsonDataSource(final String name,
                   final Object obj){
        this.name = name;
        this.serializableObject = obj;
    }

    /**
     * This method returns an <code>InputStream</code> representing
     * the data and throws the appropriate exception if it can
     * not do so.  Note that a new <code>InputStream</code> object must be
     * returned each time this method is called, and the stream must be
     * positioned at the beginning of the data.
     *
     * @return an InputStream
     */
    @Override
    public ByteArrayInputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(JSON_SERIALIZER.writeValueAsBytes(serializableObject));
    }

    /**
     * This method returns an <code>OutputStream</code> where the
     * data can be written and throws the appropriate exception if it can
     * not do so.  Note that a new <code>OutputStream</code> object must
     * be returned each time this method is called, and the stream must
     * be positioned at the location the data is to be written.
     *
     * @return an OutputStream
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("Uploading is not supported");
    }

    /**
     * This method returns the MIME type of the data in the form of a
     * string. It should always return a valid type. It is suggested
     * that getContentType return "application/octet-stream" if the
     * DataSource implementation can not determine the data type.
     *
     * @return the MIME Type
     */
    @Override
    public String getContentType() {
        return MediaType.JSON_UTF_8.toString();
    }

    /**
     * Return the <i>name</i> of this object where the name of the object
     * is dependant on the nature of the underlying objects. DataSources
     * encapsulating files may choose to return the filename of the object.
     * (Typically this would be the last component of the filename, not an
     * entire pathname.)
     *
     * @return the name of the object.
     */
    @Override
    public String getName() {
        return name;
    }
}
