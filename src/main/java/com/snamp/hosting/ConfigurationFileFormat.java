package com.snamp.hosting;

import com.snamp.connectors.ManagementConnector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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

    private static AgentConfiguration parseYaml(final InputStream stream){
        //TODO: Should be completed
        return null;
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
