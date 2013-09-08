package com.snamp.hosting;

enum ConfigurationFileFormat {
	/**
	 * Формат файл Java Properties
	 */
	PROPERTIES("props"),
	
	/**
	 * Формат файла YAML
	 */
	YAML("yaml");
	
	private final String _formatName;
	
	private ConfigurationFileFormat(String formatName)
	{
		_formatName = formatName;
	}
	
	/**
	 * Получить название формата файла.
	 */
	@Override
	public String toString()
	{
		return _formatName;
	}
	
	/**
	 * Преобразовать название формата в его типизированную форму.
	 * @param format Имя формата: yaml, props
	 * @return
	 */
	public static ConfigurationFileFormat parse(final String format)
	{
		return "yaml".equals(format) ? YAML : PROPERTIES;
	}
}
