package com.itworks.snamp.adapters;

import java.io.IOException;
import java.math.*;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

import com.google.gson.*;
import com.itworks.snamp.SimpleTable;
import com.itworks.snamp.Table;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.ManagementEntityTabularType;
import com.itworks.snamp.connectors.ManagementEntityType;
import com.itworks.snamp.connectors.util.AttributeValue;
import com.itworks.snamp.connectors.util.AttributesRegistryReader;
import com.itworks.snamp.core.AbstractPlatformService;
import com.itworks.snamp.internal.MethodThreadSafety;
import com.itworks.snamp.internal.ThreadSafety;
import com.sun.jersey.spi.resource.Singleton;
import static com.itworks.snamp.connectors.ManagementEntityTypeBuilder.AbstractManagementEntityArrayType.*;
import static com.itworks.snamp.connectors.WellKnownTypeSystem.*;

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

    RestAdapterService(final String dateFormat, final AttributesRegistryReader registeredAttributes, final Logger logger){
        super(logger);
        this.attributes = registeredAttributes;
        final GsonBuilder builder = new GsonBuilder();
        if(dateFormat == null || dateFormat.isEmpty())
            builder.setDateFormat(DateFormat.FULL);
        else builder.setDateFormat(dateFormat);
        builder.serializeNulls();
        jsonFormatter = builder.create();
        jsonParser = new JsonParser();
        this.logger = logger;
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    private final JsonElement toJsonTable(final AttributeValue<ManagementEntityTabularType> table){
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

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    private final JsonArray toJsonArray(final AttributeValue<ManagementEntityTabularType> array){
        final JsonArray result = new JsonArray();
        //invoke all elements and converts each of them to JSON
        for(final Object rawValue: array.convertTo(Object[].class)){
            result.add(toJson(new AttributeValue<>(rawValue, array.type.getColumnType(VALUE_COLUMN_NAME))));
        }
        return result;
    }

    private JsonObject toJsonMap(final AttributeValue<ManagementEntityTabularType> map){
        final JsonObject result = new JsonObject();
        final Map<String, Object> value = map.convertTo(Map.class);
        for(final String column: value.keySet())
            result.add(column, toJson(new AttributeValue<>(value.get(column), map.type.getColumnType(column))));
        return result;
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    private final JsonElement toJson(final AttributeValue<? extends ManagementEntityType> value){
        if(value == null || value.rawValue == null)
            return JsonNull.INSTANCE;
        else if(supportsString(value.type))
            return new JsonPrimitive(value.convertTo(String.class));
        else if(supportsBoolean(value.type))
            return new JsonPrimitive(value.convertTo(Boolean.class));
        else if(supportsUnixTime(value.type))
            return jsonFormatter.toJsonTree(value.convertTo(Date.class));
        else if(supportsInt8(value.type))
            return new JsonPrimitive(value.convertTo(Byte.class));
        else if(supportsInt16(value.type))
            return new JsonPrimitive(value.convertTo(Short.class));
        else if(supportsInt32(value.type))
            return new JsonPrimitive(value.convertTo(Integer.class));
        else if(supportsInt64(value.type))
            return new JsonPrimitive(value.convertTo(Long.class));
        else if(supportsInteger(value.type))
            return new JsonPrimitive(value.convertTo(BigInteger.class));
        else if(supportsDecimal(value.type))
            return new JsonPrimitive(value.convertTo(BigDecimal.class));
        else if(supportsFloat(value.type))
            return new JsonPrimitive(value.convertTo(Float.class));
        else if(supportsDouble(value.type))
            return new JsonPrimitive(value.convertTo(Double.class));
        else if(isArray(value.type))
            return toJsonArray(value.cast(ManagementEntityTabularType.class));
        else if(isMap(value.type))
            return toJsonMap(value.cast(ManagementEntityTabularType.class));
        else if(value.isTypeOf(ManagementEntityTabularType.class))
            return toJsonTable(value.cast(ManagementEntityTabularType.class));
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

    private Object fromArrayJson(final JsonArray attributeValue, final ManagementEntityType elementType){
        final Object[] result = new Object[attributeValue.size()];
        for(int i = 0; i < attributeValue.size(); i++){
            result[i] = fromJson(jsonFormatter.toJson(attributeValue.get(i)), elementType);
        }
        return result;
    }

    private Object fromArrayJson(final JsonElement attributeValue, final ManagementEntityType elementType){
        if(attributeValue instanceof JsonArray)
            return fromArrayJson((JsonArray) attributeValue, elementType);
        else {
            throwAndLog(Level.WARNING, new JsonSyntaxException(String.format("Expected JSON array, but actually found %s", jsonFormatter.toJson(attributeValue))));
            return null;
        }
    }

    private Object fromArrayJson(final String attributeValue, final ManagementEntityTabularType attributeType){
        return fromArrayJson(jsonParser.parse(attributeValue), attributeType.getColumnType(VALUE_COLUMN_NAME));
    }

    private void insertRow(final Table<String> table, final JsonObject row, final ManagementEntityTabularType attributeType){
        final Map<String, Object> insertedRow = new HashMap<>(10);
        //iterates through each column
        for(final Map.Entry<String, JsonElement> column: row.entrySet()){
            insertedRow.put(column.getKey(),
                    fromJson(jsonFormatter.toJson(row.get(column.getKey())), attributeType.getColumnType(column.getKey())));
        }
        table.addRow(insertedRow);
    }

    private Table<String> fromTableJson(final JsonArray attributeValue, final ManagementEntityTabularType attributeType){
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

    private Table<String> fromTableJson(final JsonElement attributeValue, final ManagementEntityTabularType attributeType){
        if(attributeValue instanceof JsonArray)
            return fromTableJson((JsonArray) attributeValue, attributeType);
        else if(attributeValue instanceof JsonObject){
            final JsonArray array = new JsonArray();
            array.add(attributeValue);
            return fromTableJson(array, attributeType);
        }
        else {
            throwAndLog(Level.WARNING, new JsonSyntaxException(String.format("Expected JSON array, but actually found %s", jsonFormatter.toJson(attributeValue))));
            return null;
        }
    }

    private Table<String> fromTableJson(final String attributeValue, final ManagementEntityTabularType attributeType){
        return fromTableJson(jsonParser.parse(attributeValue), attributeType);
    }

    private Map<String, Object> fromMapJson(final JsonObject attributeValue, final ManagementEntityTabularType attributeType){
        final Map<String, Object> result = new HashMap<>(10);
        for(final String column: attributeType.getColumns())
            if(attributeValue.has(column))
                result.put(column, fromJson(jsonFormatter.toJson(attributeValue.get(column)), attributeType.getColumnType(column)));
            else throwAndLog(Level.WARNING, new JsonSyntaxException(String.format("JSON key %s not found.", column)));
        return result;
    }

    private Map<String, Object> fromMapJson(final JsonElement attributeValue, final ManagementEntityTabularType attributeType){
        if(attributeValue instanceof JsonObject)
            return fromMapJson((JsonObject)attributeValue, attributeType);
        else {
            throwAndLog(Level.WARNING, new JsonSyntaxException(String.format("Expected JSON object, but actually found %s", jsonFormatter.toJson(attributeValue))));
            return null;
        }
    }

    private Map<String, Object> fromMapJson(final String attributeValue, final ManagementEntityTabularType attributeType){
        return fromMapJson(jsonParser.parse(attributeValue), attributeType);
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    private Object fromJson(final String attributeValue, final ManagementEntityType attributeType){
        if(supportsBoolean(attributeType))
            return jsonFormatter.fromJson(attributeValue, Boolean.class);
        else if(supportsString(attributeType))
            return jsonFormatter.fromJson(attributeValue, String.class);
        else if(supportsInt8(attributeType))
            return jsonFormatter.fromJson(attributeValue, Byte.class);
        else if(supportsInt16(attributeType))
            return jsonFormatter.fromJson(attributeValue, Short.class);
        else if(supportsInt32(attributeType))
            return jsonFormatter.fromJson(attributeValue, Integer.class);
        else if(supportsInt64(attributeType))
            return jsonFormatter.fromJson(attributeValue, Long.class);
        else if(supportsInteger(attributeType))
            return jsonFormatter.fromJson(attributeValue, BigInteger.class);
        else if(supportsDecimal(attributeType))
            return jsonFormatter.fromJson(attributeValue, BigDecimal.class);
        else if(supportsDouble(attributeType))
            return jsonFormatter.fromJson(attributeValue, Double.class);
        else if(supportsFloat(attributeType))
            return jsonFormatter.fromJson(attributeValue, Float.class);
        else if(supportsUnixTime(attributeType))
            return jsonFormatter.fromJson(attributeValue, Date.class);
        else if(isArray(attributeType))
            return fromArrayJson(attributeValue, (ManagementEntityTabularType) attributeType);
        else if(isMap(attributeType))
            return fromMapJson(attributeValue, (ManagementEntityTabularType)attributeType);
        else if(isTable(attributeType))
            return fromTableJson(attributeValue, (ManagementEntityTabularType) attributeType);
        else {
            throwAndLog(Level.WARNING, new UnsupportedOperationException(String.format("Unsupported conversion from %s value", attributeValue)));
            return null;
        }
    }

    /**
     * Returns a list of available attributes.
     * @return JSON list of available attributes.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/attributes")
    public final String getAttributes(){
        final JsonArray result = new JsonArray();
        for(final String prefix: attributes.getNamespaces())
            for(final String postfix: attributes.getRegisteredAttributes(prefix))
            result.add(new JsonPrimitive(RestAdapterHelpers.makeAttributeID(prefix, postfix)));
        return jsonFormatter.toJson(result);
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    @POST
    @Path("/attribute/{" + namespaceParam + "}/{" + attributeIdParam + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public final String setAttribute(@PathParam(namespaceParam)final String namespace,
                                   @PathParam(attributeIdParam)final String attributeId,
                                   final String attributeValue,
                                   @Context HttpServletResponse response) throws IOException {
        final ManagementEntityType attributeType = attributes.getAttributeType(namespace, attributeId);
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