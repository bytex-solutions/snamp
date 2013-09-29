package com.snamp.hosting;

import com.snamp.connectors.ManagementConnector;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Represents configuration file parser.
 */
enum ConfigurationFileFormat implements ConfigurationParser {

    /**
	 * Формат файла YAML
	 */
	YAML("yaml");

	private final String _formatName;
	
	private ConfigurationFileFormat(String formatName) {
		_formatName = formatName;
	}
	
	/**
	 * Returns a string representation of this file format.
     * @return The name of the format.
	 */
	@Override
	public final String toString() {
		return _formatName;
	}

    /**
     * Represents YAML-compliant configuration.
     */
    private static final class YamlAgentConfiguration extends HashMap<String, Object> implements AgentConfiguration{
        private static final String hostingPortKey = "port";
        private static final String hostingAddressKey = "address";

        /**
         * Initializes a new YAML-compliant configuration.
         * @param rawConfiguration The raw configuration in the form of the YAML DOM.
         */
        public YamlAgentConfiguration(final Map<String, Object> rawConfiguration){
            super(rawConfiguration);
        }

        /**
         * Returns the agent hosting configuration.
         *
         * @return The agent hosting configuration.
         */
        @Override
        public HostingConfiguration getAgentHostingConfig() {
            return null;
        }

        /**
         * Represents management targets.
         *
         * @return The dictionary of management targets (management back-ends).
         */
        @Override
        public Map<String, ManagementTargetConfiguration> getTargets() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private static AgentConfiguration parseYaml(final InputStream stream){
        final Yaml yaml = new Yaml();
        final Map<String, Object> dom = (Map<String, Object>)yaml.load(stream);
        return new YamlAgentConfiguration(dom);
    }

    private static void saveYaml(final AgentConfiguration config, final OutputStream output) throws IOException {
        final Yaml yaml = new Yaml();
        final String result = yaml.dumpAsMap(config);
        output.write(result.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Parses the configuration
     * @param stream The stream that contains configuration of the agent in serialized form.
     * @return The parsed agent configuration.
     * @throws IllegalArgumentException stream is {@literal null}.
     * @throws UnsupportedOperationException Invalid configuration file format type.
     */
    @Override
    public final AgentConfiguration parse(final InputStream stream){
        if(stream == null) throw new IllegalArgumentException("stream is null.");
        switch (_formatName){
            case "yaml": return parseYaml(stream);
            default: throw new UnsupportedOperationException("Configuration format is not supported.");
        }
    }

    /**
     * Saves the configuration back to the stream.
     * @param configuration The configuration to save.
     * @param output The output stream.
     */
    @Override
    public final void save(final AgentConfiguration configuration, final OutputStream output) throws IOException {
        switch (_formatName){
            case "yaml": saveYaml(configuration, output); return;
            default: throw new UnsupportedOperationException("Unsupported configuration file format");
        }
    }

    /**
     * Returns the configuration file format parser from the format name.
     * @param format The configuration file format name.
     * @return An instance of the configuration file.
     */
	public static ConfigurationFileFormat parse(final String format) {
		switch (format){
            default:
            case "yaml": return YAML;
        }
	}

    /**
     * Loads the configuration from the specified file.
     * @param format The configuration file format name.
     * @param fileName The path to the configuration file.
     * @return The parsed configuration.
     */
    public static AgentConfiguration load(final String format, final String fileName) throws IOException {
        final ConfigurationParser parser = parse(format);
        try(final InputStream stream = new FileInputStream(fileName)){
            return parser.parse(stream);
        }
    }
}
