package com.snamp.adapters;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.*;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

import com.google.gson.*;
import com.snamp.*;
import com.snamp.connectors.*;
import com.sun.jersey.spi.resource.Singleton;

import static com.snamp.connectors.AttributePrimitiveTypeBuilder.*;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
@Singleton
public final class RestAdapterService extends AbstractPlatformService {
    private static final String namespaceParam = "namespace";
    private static final String attributeIdParam = "attributeId";

    private final AttributesRegistryReader attributes;
    private final Gson jsonFormatter;
    private final JsonParser jsonParser;
    private final Logger logger;
    private static final TimeSpan READ_WRITE_TIMEOUT = new TimeSpan(60, TimeUnit.SECONDS);

    RestAdapterService(final AttributesRegistryReader registeredAttributes, final Logger logger){
        super(logger);
        this.attributes = registeredAttributes;
        jsonFormatter = new GsonBuilder()
                .serializeNulls()
                .setDateFormat(DateFormat.LONG).create();
        jsonParser = new JsonParser();
        this.logger = logger;
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    private final JsonElement toJsonTable(final AttributeValue<AttributeTabularType> table){
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
        return result.size() == 1 ? result.get(0) : result;
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    private final JsonArray toJsonArray(final AttributeValue<AttributeArrayType> array){
        final JsonArray result = new JsonArray();
        //read all elements and converts each of them to JSON
        for(final Object rawValue: array.convertTo(Object[].class)){
            result.add(toJson(new AttributeValue<>(rawValue, array.type.getColumnType(AttributeArrayType.VALUE_COLUMN_NAME))));
        }
        return result;
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    private final JsonElement toJson(final AttributeValue<? extends AttributeTypeInfo> value){
        if(value == null || value.rawValue == null)
            return JsonNull.INSTANCE;
        else if(isString(value.type))
            return new JsonPrimitive(value.convertTo(String.class));
        else if(isBoolean(value.type))
            return new JsonPrimitive(value.convertTo(Boolean.class));
        else if(isUnixTime(value.type))
            return jsonFormatter.toJsonTree(value.convertTo(Date.class));
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

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/attribute/{" + namespaceParam + "}/{" + attributeIdParam + "}")
    public final String getAttribute(@PathParam(namespaceParam) final String namespace,
                                     @PathParam(attributeIdParam) final String attributeId){

        return jsonFormatter.toJson(toJson(attributes.getAttribute(namespace, attributeId, READ_WRITE_TIMEOUT)));
    }

    private Object fromJson(final JsonArray attributeValue, final AttributeTypeInfo elementType){
        final Object[] result = new Object[attributeValue.size()];
        for(int i = 0; i < attributeValue.size(); i++){
            result[i] = fromJson(jsonFormatter.toJson(attributeValue.get(i)), elementType);
        }
        return result;
    }

    private Object fromJson(final JsonElement attributeValue, final AttributeTypeInfo elementType){
        if(attributeValue instanceof JsonArray)
            return fromJson((JsonArray)attributeValue, elementType);
        else {
            throwAndLog(Level.WARNING, new JsonSyntaxException(String.format("Expected JSON array, but actually found %s", jsonFormatter.toJson(attributeValue))));
            return null;
        }
    }

    private Object fromJson(final String attributeValue, final AttributeArrayType attributeType){
        return fromJson(jsonParser.parse(attributeValue), attributeType.getColumnType(AttributeArrayType.VALUE_COLUMN_NAME));
    }

    private void insertRow(final Table<String> table, final JsonObject row, final AttributeTabularType attributeType){
        final Map<String, Object> insertedRow = new HashMap<>(10);
        //iterates through each column
        for(final Map.Entry<String, JsonElement> column: row.entrySet()){
            insertedRow.put(column.getKey(),
                    fromJson(jsonFormatter.toJson(row.get(column.getKey())), attributeType.getColumnType(column.getKey())));
        }
        table.addRow(insertedRow);
    }

    private Table<String> fromJson(final JsonArray attributeValue, final AttributeTabularType attributeType){
        final Table<String> result = new SimpleTable<>(new HashMap<String, Class<?>>(){{
            for(final String columnName: attributeType.getColumns())
                put(columnName, Object.class);
        }});
        for(final JsonElement element: attributeValue)
            if(element instanceof JsonObject)
                insertRow(result, (JsonObject)element, attributeType);
            else throwAndLog(Level.WARNING, new JsonSyntaxException("The element of the JSON array must be a JSON dictionary"));
        return result;
    }

    private Table<String> fromJson(final JsonElement attributeValue, final AttributeTabularType attributeType){
        if(attributeValue instanceof JsonArray)
            return fromJson((JsonArray)attributeValue, attributeType);
        else if(attributeValue instanceof JsonObject){
            final JsonArray array = new JsonArray();
            array.add(attributeValue);
            return fromJson(array, attributeType);
        }
        else {
            throwAndLog(Level.WARNING, new JsonSyntaxException(String.format("Expected JSON array, but actually found %s", jsonFormatter.toJson(attributeValue))));
            return null;
        }
    }

    private Object fromJson(final String attributeValue, final AttributeTabularType attributeType){
        return fromJson(jsonParser.parse(attributeValue), attributeType);
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    private Object fromJson(final String attributeValue, final AttributeTypeInfo attributeType){
        if(isBoolean(attributeType))
            return jsonFormatter.fromJson(attributeValue, Boolean.class);
        else if(isString(attributeType))
            return jsonFormatter.fromJson(attributeValue, String.class);
        else if(isInt8(attributeType))
            return jsonFormatter.fromJson(attributeValue, Byte.class);
        else if(isInt16(attributeType))
            return jsonFormatter.fromJson(attributeValue, Short.class);
        else if(isInt32(attributeType))
            return jsonFormatter.fromJson(attributeValue, Integer.class);
        else if(isInt64(attributeType))
            return jsonFormatter.fromJson(attributeValue, Long.class);
        else if(isInteger(attributeType))
            return jsonFormatter.fromJson(attributeValue, BigInteger.class);
        else if(isDecimal(attributeType))
            return jsonFormatter.fromJson(attributeValue, BigDecimal.class);
        else if(isDouble(attributeType))
            return jsonFormatter.fromJson(attributeValue, Double.class);
        else if(isFloat(attributeType))
            return jsonFormatter.fromJson(attributeValue, Float.class);
        else if(isUnixTime(attributeType))
            return jsonFormatter.fromJson(attributeValue, Date.class);
        else if(attributeType.canConvertFrom(Object[].class) && attributeType instanceof AttributeArrayType)
            return fromJson(attributeValue, (AttributeArrayType)attributeType);
        else if(attributeType.canConvertFrom(Table.class) && attributeType instanceof AttributeTabularType)
            return fromJson(attributeValue, (AttributeTabularType) attributeType);
        else if(attributeType.canConvertFrom(Byte.class))
            return jsonFormatter.fromJson(attributeValue, Byte.class);
        else if(attributeType.canConvertFrom(Short.class))
            return jsonFormatter.fromJson(attributeValue, Short.class);
        else if(attributeType.canConvertFrom(Integer.class))
            return jsonFormatter.fromJson(attributeValue, Integer.class);
        else if(attributeType.canConvertFrom(Long.class))
            return jsonFormatter.fromJson(attributeValue, Long.class);
        else if(attributeType.canConvertFrom(BigInteger.class))
            return jsonFormatter.fromJson(attributeValue, BigInteger.class);
        else if(attributeType.canConvertFrom(BigDecimal.class))
            return jsonFormatter.fromJson(attributeValue, BigDecimal.class);
        else if(attributeType.canConvertFrom(Boolean.class))
            return jsonFormatter.fromJson(attributeValue, Boolean.class);
        else {
            throwAndLog(Level.WARNING, new UnsupportedOperationException(String.format("Unsupported conversion from %s value", attributeValue)));
            return null;
        }
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    @POST
    @Path("/attribute/{" + namespaceParam + "}/{" + attributeIdParam + "}")
    //@Path("/attribute/test/stringProperty")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public final String setAttribute(@PathParam(namespaceParam)final String namespace,
                                   @PathParam(attributeIdParam)final String attributeId,
                                   final String attributeValue,
                                   @Context HttpServletResponse response) throws IOException {
        final AttributeTypeInfo attributeType = attributes.getAttributeType(namespace, attributeId);
        if(attributeType == null) return jsonFormatter.toJson(new JsonPrimitive(false));
        try{
            attributes.setAttribute(namespace, attributeId, fromJson(attributeValue, attributeType), READ_WRITE_TIMEOUT);
        }
        catch (final JsonSyntaxException e){
            response.sendError(400, e.getMessage());
            return jsonFormatter.toJson(new JsonPrimitive(false));
        }
        return jsonFormatter.toJson(new JsonPrimitive(true));
    }
}