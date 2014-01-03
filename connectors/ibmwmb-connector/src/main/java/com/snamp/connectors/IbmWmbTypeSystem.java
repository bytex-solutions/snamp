package com.snamp.connectors;

import com.snamp.SimpleTable;
import com.snamp.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * User: adonai
 * Date: 13.11.13
 * Time: 23:02
 */
class IbmWmbTypeSystem extends WellKnownTypeSystem {

    @Converter
    public static Table<String> convertToTable(final Properties propsMap) {
        final Map<String, Class<?>> columnsAndTypes = new HashMap<String, Class<?>>() {{ put("Key", String.class); put("Value", String.class); }};
        final SimpleTable<String> result = new SimpleTable<>(columnsAndTypes);
        for(final Map.Entry<Object, Object> property : propsMap.entrySet())
            result.addRow(new HashMap<String, Object>() {{ put("Key", property.getKey().toString()); put("Value", property.getValue().toString()); }});

        return result;
    }

    public ManagementEntityType createPropertiesMap() {
        return createEntityTabularType(new HashMap<String, ManagementEntityType>() {{
            put("Key", createStringType());
            put("Value", createStringType());
        }});
    }
}
