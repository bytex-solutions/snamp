package com.bytex.snamp.connector.rshell;

import com.bytex.jcommands.impl.XmlParserDefinition;
import com.bytex.jcommands.impl.XmlParsingResultType;
import com.bytex.snamp.EntryReader;
import com.bytex.snamp.connector.FeatureDescriptor;
import com.bytex.snamp.jmx.CompositeTypeBuilder;
import com.bytex.snamp.jmx.TabularTypeBuilder;

import javax.management.openmbean.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class OpenTypeHelpers {
    private static final String INDEX_COLUMN = "index";

    private OpenTypeHelpers(){
        throw new InstantiationError();
    }

    static <D extends FeatureDescriptor<?>> TabularType createTabularType(final String featureName,
                                                                          final D descriptor,
                                                                          final XmlParserDefinition definition,
                                                                          final Function<? super D, String> descriptionProvider) throws OpenDataException {
        final TabularTypeBuilder builder = new TabularTypeBuilder();
        builder.addColumn(INDEX_COLUMN, "The index of the row", SimpleType.INTEGER, true);
        definition.exportTableOrDictionaryType(EntryReader.<String, XmlParsingResultType>fromConsumer((index, value) -> builder.addColumn(index, index, value.getOpenType(), false)));
        final String typeName = descriptor.getAlternativeName().orElse(featureName);
        return builder.setTypeName(String.format("%sTabularType", typeName), true)
                .setDescription(descriptionProvider.apply(descriptor), true)
                .build();
    }

    static <D extends FeatureDescriptor<?>> CompositeType createCompositeType(final String featureName,
                                                  final D descriptor,
                                                  final XmlParserDefinition definition,
                                                                           final Function<? super D, String> descriptionProvider) throws OpenDataException {
        final CompositeTypeBuilder builder = new CompositeTypeBuilder();
        definition.exportTableOrDictionaryType(EntryReader.<String, XmlParsingResultType>fromConsumer((index, value) -> builder.addItem(index, index, value.getOpenType())));
        return builder.setTypeName(String.format("%sCompositeType", descriptor.getAlternativeName().orElse(featureName)))
                .setDescription(descriptionProvider.apply(descriptor))
                .build();
    }

    static TabularData toTabularData(final TabularType openType,
                                     final List<? extends Map<String, ?>> rows) throws OpenDataException{
        final TabularDataSupport result = new TabularDataSupport(openType);
        for(int index = 0; index < rows.size(); index++){
            final Map<String, Object> row = new HashMap<>(rows.get(index));
            row.put(INDEX_COLUMN, index);
            result.put(new CompositeDataSupport(result.getTabularType().getRowType(), row));
        }
        return result;
    }
}
