package com.snamp.adapters;

import java.util.*;

/**
 * @author roman
 */

import com.google.gson.*;
import com.snamp.Table;
import com.snamp.TimeSpan;
import com.snamp.connectors.*;

import static com.snamp.connectors.AttributePrimitiveTypeBuilder.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public final class RestAdapterService {
    private static final String namespaceParam = "namespace";
    private static final String attributeIdParam = "attributeId";

    private final AttributesRegistryReader attributes;
    private final Gson jsonFormatter;

    RestAdapterService(final AttributesRegistryReader registeredAttributes){
        this.attributes = registeredAttributes;
        jsonFormatter = new Gson();
    }

    private static final JsonElement toJsonTable(final AttributeValue<AttributeTabularType> table){
        final JsonArray result = new JsonArray();
        final Table<String> tableReader = table.convertTo(Table.class);
        //table representation in JSON: [{column: value}, {column: value}]
        //therefore, iterates through rows
        for(int rowIndex = 0; rowIndex < tableReader.getRowCount(); rowIndex++){
            final JsonObject row = new JsonObject();
            //iterates through columns
            for(final String columnName: table.type.getColumns())
                row.add(columnName, toJson(new AttributeValue<>(tableReader.getCell(columnName, rowIndex), table.type.getColumnType(columnName))));
            result.add(row);
        }
        return result;
    }

    private static final JsonArray toJsonArray(final AttributeValue<AttributeArrayType> array){
        final JsonArray result = new JsonArray();
        //read all elements and converts each of them to JSON
        for(final Object rawValue: array.convertTo(Object[].class)){
            result.add(toJson(new AttributeValue<>(rawValue, array.type.getColumnType(AttributeArrayType.VALUE_COLUMN_NAME))));
        }
        return result;
    }

    private static final JsonElement toJson(final AttributeValue<? extends AttributeTypeInfo> value){
        if(value == null || value.rawValue == null)
            return JsonNull.INSTANCE;
        else if(isString(value.type))
            return new JsonPrimitive(value.convertTo(String.class));
        else if(isBoolean(value.type))
            return new JsonPrimitive(value.convertTo(Boolean.class));
        else if(isUnixTime(value.type))
            return new JsonPrimitive(value.convertTo(Date.class).getTime());
        else if(isInt8(value.type) ||
                isInt16(value.type) ||
                isInt32(value.type) ||
                isInt64(value.type) ||
                isInteger(value.type) ||
                isDecimal(value.type) ||
                isFloat(value.type) ||
                isDouble(value.type) ||
                value.canConvertTo(Number.class))
            return new JsonPrimitive(value.convertTo(Number.class));
        else if(value.isTypeOf(AttributeArrayType.class))
            return toJsonArray(value.cast(AttributeArrayType.class));
        else if(value.isTypeOf(AttributeTabularType.class))
            return toJsonTable(value.cast(AttributeTabularType.class));
        else return new JsonPrimitive(value.convertTo(String.class));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/attribute/{" + namespaceParam + "}/{" + attributeIdParam + "}")
    public final String getAttribute(@PathParam(namespaceParam) final String namespace,
                                     @PathParam(attributeIdParam) final String attributeId){
        return jsonFormatter.toJson(toJson(attributes.getAttribute(namespace, attributeId, TimeSpan.INFINITE)));
    }
}