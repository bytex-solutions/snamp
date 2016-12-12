package com.bytex.snamp.database;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OrientDatabaseService /*extends OServer implements DatabaseService, ManagedService*/ {
//    public static final String PERSISTENCE_ID = "com.bytex.snamp.db.configuration";
//    private final Marshaller xmlSerializer;
//    private final Unmarshaller xmlDeserializer;
//
//    public OrientDatabaseService() throws ReflectiveOperationException, JMException, JAXBException {
//        super(false);
//        final JAXBContext context = JAXBContext.newInstance(OServerConfiguration.class);
//        xmlSerializer = context.createMarshaller();
//        xmlDeserializer = context.createUnmarshaller();
//    }
//
//    @Override
//    public void restart() throws IOException {
//        try {
//            super.restart();
//        } catch (final ReflectiveOperationException e) {
//            throw new IOException(e);
//        }
//    }
//
//    /**
//     * Sets a new configuration.
//     *
//     * @param input Reader of configuration content.
//     * @throws IOException Unable to parse new configuration.
//     */
//    @Override
//    public void setupConfiguration(final Reader input) throws IOException {
//        final OServerConfiguration configuration;
//        try {
//            configuration = (OServerConfiguration) xmlDeserializer.unmarshal(input);
//        } catch (final JAXBException | ClassCastException e) {
//            throw new IOException(e);
//        }
//        serverCfg = new OServerConfigurationManager(configuration);
//    }
//
//    @Override
//    public void readConfiguration(final Writer output) throws IOException {
//        try {
//            xmlSerializer.marshal(getConfiguration(), output);
//        } catch (final JAXBException e) {
//            throw new IOException(e);
//        }
//    }
//
//    @Override
//    public void updated(final Dictionary<String, ?> properties) throws ConfigurationException {
//
//    }
}
