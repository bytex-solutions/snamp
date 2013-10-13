package com.snamp.hosting;

import com.snamp.connectors.ManagementConnector;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Represents configuration file parser.
 */
enum ConfigurationFileFormat {

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

        /**
         * Initializes a new empty configuration.
         */
        public YamlAgentConfiguration(){
            super(10);
        }

        /**
         * Creates a new default configuration of the management target.
         *
         * @return A new default configuration of the management target.
         */
        @Override
        public ManagementTargetConfiguration newManagementTargetConfiguration() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * Returns the agent hosting configuration.
         *
         * @return The agent hosting configuration.
         */
        @Override
        public HostingConfiguration getAgentHostingConfig() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
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

        /**
         * Saves the current configuration into the specified stream.
         *
         * @param output
         * @throws UnsupportedOperationException Serialization is not supported.
         */
        @Override
        public void save(final OutputStream output) throws IOException {
            final Yaml yaml = new Yaml();
            final String result = yaml.dumpAsMap(this);
            output.write(result.getBytes(Charset.forName("UTF-8")));
        }

        /**
         * Reads the file and fills the current instance.
         *
         * @param input
         */
        @Override
        public void load(final InputStream input) {
            final Yaml yaml = new Yaml();
            final Map<String, Object> dom = (Map<String, Object>)yaml.load(input);
            clear();
            putAll(dom);
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
     * Creates a new empty configuration of the specified format.
     * @param format
     * @return
     */
    public static AgentConfiguration newAgentConfiguration(final String format){
        return parse(format).newAgentConfiguration();
    }

    /**
     * Creates a new empty configuration of this format.
     * @return
     */
    public final AgentConfiguration newAgentConfiguration(){
        switch (_formatName){
            case "yaml": return new YamlAgentConfiguration();
            default: return null;
        }
    }

    /**
     * Loads the configuration from the specified file.
     * @param format The configuration file format name.
     * @param fileName The path to the configuration file.
     * @return The parsed configuration.
     */
    public static AgentConfiguration load(final String format, final String fileName) throws IOException {
        final AgentConfiguration config = newAgentConfiguration(format);
        try(final InputStream stream = new FileInputStream(fileName)){
            config.load(stream);
        }
        return config;
    }
}
