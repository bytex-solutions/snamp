package com.itworks.jcommands.impl;

import com.itworks.snamp.internal.semantics.Internal;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.ArrayUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.*;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Represents parser for command-line output result.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@XmlType(name = "CommandLineToolOutputParser", namespace = XmlConstants.NAMESPACE)
@XmlAccessorType(XmlAccessType.PROPERTY)
public class XmlCommandLineToolOutputParser {
    private static interface Converter<T> extends Transformer<String, T>{

    }

    /**
     * Represents parsing rule. This class cannot be inherited directly from your code.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @XmlTransient
    public abstract static class ParsingRule{
        private EnumSet<XmlCommandLineToolReturnType> related;

        private ParsingRule(final XmlCommandLineToolReturnType first,
                            final XmlCommandLineToolReturnType... other){
            this.related = EnumSet.of(first, other);
        }

        /**
         * Determines whether this parsing rule can be used to describe parsing
         * of the specified type.
         * @param type The type to check.
         * @return {@literal true}, if this parsing rule can be used with the specified
         * return type; otherwise, {@literal false}.
         */
        public final boolean compatibleWith(final XmlCommandLineToolReturnType type){
            return related.contains(type);
        }
    }

    /**
     * Represents parsing rule that detects line termination for recursive data structures,
     * such as dictionaries and tables. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @XmlRootElement(name = "line-terminator", namespace = XmlConstants.NAMESPACE)
    @XmlType(name = "LineTerminationParsingRule", namespace = XmlConstants.NAMESPACE)
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static final class LineTerminationParsingRule extends ParsingRule{
        private String terminationRule;

        public LineTerminationParsingRule(){
            super(XmlCommandLineToolReturnType.ARRAY, XmlCommandLineToolReturnType.TABLE);
            terminationRule = "";
        }

        @XmlValue
        public String getTerminationRule(){
            return terminationRule;
        }

        public void setTerminationRule(final String value){
            terminationRule = value != null ? value : null;
        }
    }

    @XmlRootElement(name = "column", namespace = XmlConstants.NAMESPACE)
    @XmlType(name = "TableColumnParsingRule")
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static final class TableColumnParsingRule extends ParsingRule{
        private String columnName;
        private String columnValueParsingRule;
        private XmlCommandLineToolReturnType columnType;

        public TableColumnParsingRule(){
            super(XmlCommandLineToolReturnType.TABLE);
            columnName = columnValueParsingRule = "";
            columnType = XmlCommandLineToolReturnType.STRING;
        }

        @XmlAttribute(name = "name", namespace = XmlConstants.NAMESPACE)
        public String getColumnName(){
            return columnName;
        }

        public void setColumnName(final String value){
            columnName = value != null ? value : "";
        }

        @XmlValue
        public String getColumnValueParsingRule(){
            return columnValueParsingRule;
        }

        public void setColumnValueParsingRule(final String value){
            columnValueParsingRule = value != null ? value : "";
        }

        @XmlAttribute(name = "type", namespace = XmlConstants.NAMESPACE)
        public XmlCommandLineToolReturnType getColumnType(){
            return columnType;
        }

        public void setColumnType(final XmlCommandLineToolReturnType value){
            if(value == null || !value.isScalar)
                throw new IllegalArgumentException(String.format("Expecting scalar type but found %s", value));
            else columnType = value;
        }
    }

    @XmlRootElement(name = "item", namespace = XmlConstants.NAMESPACE)
    @XmlType(name = "ArrayItemParsingRule", namespace = XmlConstants.NAMESPACE)
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static final class ArrayItemParsingRule extends ParsingRule{
        private String itemParsingRule;

        public ArrayItemParsingRule(){
            super(XmlCommandLineToolReturnType.ARRAY);
            itemParsingRule = "";
        }

        @XmlValue
        public String getItemParsingRule(){
            return itemParsingRule;
        }

        public void setItemParsingRule(final String value){
            itemParsingRule = value != null ? value : "";
        }
    }

    /**
     * Describes parsing of the key/value pair for the dictionary.
     * This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @XmlType(name = "DictionaryEntryParsingRule", namespace = XmlConstants.NAMESPACE)
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlRootElement(name = "entry", namespace = XmlConstants.NAMESPACE)
    public final static class DictionaryEntryParsingRule extends ParsingRule{
        private String key;
        private String value;
        private XmlCommandLineToolReturnType valueType;

        /**
         * Initializes a new dictionary entry with default settings.
         */
        public DictionaryEntryParsingRule(){
            super(XmlCommandLineToolReturnType.DICTIONARY);
            key = value = "";
            valueType = XmlCommandLineToolReturnType.STRING;
        }

        /**
         * Returns name of the dictionary key.
         * @return The name of the dictionary key.
         */
        @XmlAttribute(name = "key", namespace = XmlConstants.NAMESPACE)
        public String getKeyName() {
            return key;
        }

        /**
         * Sets the name of the dictionary key.
         * @param value The name of the dictionary key.
         */
        public void setKeyName(final String value){
            this.key = value != null ? value : "";
        }

        /**
         * Gets parsing rule for the value of the dictionary entry.
         * @return The parsing rule.
         */
        @XmlValue
        public String getValueParsingRule() {
            return value;
        }

        /**
         * Sets parsing rule for the value of the dictionary entry.
         * @param value The parsing rule.
         */
        public void setValueParsingRule(final String value){
            this.value = value != null ? value : "";
        }

        @XmlAttribute(name = "type", namespace = XmlConstants.NAMESPACE)
        public XmlCommandLineToolReturnType getValueType(){
            return valueType;
        }

        /**
         * Sets the type of the entry value.
         * @param value The type of the entry value. Only scalar types are allowed.
         */
        public void setValueType(final XmlCommandLineToolReturnType value){
            if(value == null || !value.isScalar)
                throw new IllegalArgumentException(String.format("Expecting scalar type but %s found", value));
            else this.valueType = value;
        }
    }

    /**
     * Represents parsing stream for the script. This class cannot be inherited or
     * directly instantiated from your code.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @Internal
    public static final class ParsingStream extends StreamTokenizer{

        private ParsingStream(final StringReader reader){
            super(reader);
        }


    }

    /**
     * Represents Regexp as parsing language for output stream.
     */
    public static final String REGEXP_LANG = "regexp";
    /**
     * Represents JavaScript as parsing language for output stream.
     */
    public static final String JAVASCRIPT_LANG = "JavaScript";

    private String language;
    private XmlCommandLineToolReturnType returnType;
    private final List parsingTemplate;

    /**
     * Initializes a new parser without settings.
     */
    public XmlCommandLineToolOutputParser(){
        language = REGEXP_LANG;
        returnType = XmlCommandLineToolReturnType.STRING;
        parsingTemplate = new ArrayList(10);
    }

    @XmlAttribute(name = "language", namespace = XmlConstants.NAMESPACE)
    public final String getParsingLanguage(){
        return language;
    }

    public final void setParsingLanguage(final String value){
        this.language = value != null ? value : "";
    }

    @XmlAttribute(name = "type", namespace = XmlConstants.NAMESPACE)
    public final XmlCommandLineToolReturnType getReturnType(){
        return returnType;
    }

    public final void setReturnType(final XmlCommandLineToolReturnType value){
        this.returnType = value;
    }

    @XmlMixed
    @XmlElementRefs({
            @XmlElementRef(type = DictionaryEntryParsingRule.class),
            @XmlElementRef(type = LineTerminationParsingRule.class),
            @XmlElementRef(type = TableColumnParsingRule.class),
            @XmlElementRef(type = ArrayItemParsingRule.class)
    })
    private List getParsingTemplate(){
        return parsingTemplate;
    }

    public final void removeParsingRules(){
        parsingTemplate.clear();
    }

    @SuppressWarnings("unchecked")
    public final void addParsingRule(final String placeholder){
        parsingTemplate.add(Objects.requireNonNull(placeholder, "placeholder is null."));
    }

    @SuppressWarnings("unchecked")
    public final void addParsingRule(final ParsingRule rule){
        if(rule == null) throw new NullPointerException("rule is null.");
        else if(!rule.compatibleWith(returnType)) throw new IllegalArgumentException(String.format("Incompatible rule with underlying type %s", returnType));
        else parsingTemplate.add(rule);
    }

    public final void addDictionaryEntryRule(final String key, final String valueRule, final XmlCommandLineToolReturnType valueType){
        final DictionaryEntryParsingRule rule = new DictionaryEntryParsingRule();
        rule.setKeyName(key);
        rule.setValueParsingRule(valueRule);
        rule.setValueType(valueType);
        addParsingRule(rule);
    }

    public final void addLineTermination(final String terminationRule){
        final LineTerminationParsingRule rule = new LineTerminationParsingRule();
        rule.setTerminationRule(terminationRule);
        addParsingRule(rule);
    }

    public final void addTableColumn(final String columnName, final String columnParsingRule, final XmlCommandLineToolReturnType columnType){
        final TableColumnParsingRule rule = new TableColumnParsingRule();
        rule.setColumnName(columnName);
        rule.setColumnType(columnType);
        rule.setColumnValueParsingRule(columnParsingRule);
        addParsingRule(rule);
    }

    public final void addArrayItem(final String itemParsingRule){
        final ArrayItemParsingRule rule = new ArrayItemParsingRule();
        rule.setItemParsingRule(itemParsingRule);
        addParsingRule(rule);
    }

    /**
     * Gets a line termination parsing rule if it is defined in this parser.
     * @return The line termination parsing rule; or {@literal null}, if this rule
     * is not defined.
     */
    public LineTerminationParsingRule getLineTerminationParsingRule(){
        for(final Object rule: getParsingTemplate())
            if(rule instanceof LineTerminationParsingRule)
                return (LineTerminationParsingRule)rule;
        return null;
    }

    /**
     * Parses the input string using this parser.
     * @param input The input string to parse.
     * @param scriptManager The script manager used to execute parsing script.
     * @return Parsed SNAMP-compliant value.
     * @throws java.lang.IllegalArgumentException Input string is {@literal null} or empty.
     * @throws java.lang.NullPointerException scriptManager is {@literal null}.
     * @throws java.lang.IllegalStateException Script engine with {@link #getParsingLanguage()} name not found.
     * @throws javax.script.ScriptException Some problems occurred inside of the script.
     */
    public final Object parse(final String input, final ScriptEngineManager scriptManager) throws ScriptException{
        if(input == null || input.isEmpty())
            throw new IllegalArgumentException("Invalid input string to parse.");
        else if(scriptManager == null)
            throw new NullPointerException("scriptManager is null.");
        else {
            //setup global scope and bindings
            final ScriptEngine scriptEngine = scriptManager.getEngineByName(getParsingLanguage());
            if(scriptEngine == null) throw new IllegalStateException(String.format("Script engine %s not found", getParsingLanguage()));
            else return parse(input, scriptEngine);
        }
    }

    private static <T> T parseScalar(final List parsingTemplate,
                                     final ScriptEngine engine,
                                     final Converter<T> converter,
                                     final T defaultValue) throws ScriptException {
        if (parsingTemplate.isEmpty())
            return defaultValue;
        final Object script = parsingTemplate.get(0);
        if (script instanceof String){
            final Object result = engine.eval(script.toString());
            return result != null ? converter.transform(result.toString()) : defaultValue;
        }
        else throw new ScriptException(String.format("Expecting script but found %s", script));
    }

    private static Short parseShort(final List parsingTemplate,
                                    final ScriptEngine engine,
                                    final boolean hexFormat) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Short>() {
                    @Override
                    public Short transform(final String input) {
                        return Short.parseShort(input, hexFormat ? 16 : 10);
                    }
                },
                (short) 0);
    }

    private static Byte parseByte(final List parsingTemplate,
                                    final ScriptEngine engine,
                                    final boolean hexFormat) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Byte>() {
                    @Override
                    public Byte transform(final String input) {
                        return Byte.parseByte(input, hexFormat ? 16 : 10);
                    }
                },
                (byte) 0);
    }

    private static Integer parseInteger(final List parsingTemplate,
                                  final ScriptEngine engine,
                                  final boolean hexFormat) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Integer>() {
                    @Override
                    public Integer transform(final String input) {
                        return Integer.parseInt(input, hexFormat ? 16 : 10);
                    }
                },
                0);
    }

    private static Long parseLong(final List parsingTemplate,
                                        final ScriptEngine engine,
                                        final boolean hexFormat) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Long>() {
                    @Override
                    public Long transform(final String input) {
                        return Long.parseLong(input, hexFormat ? 16 : 10);
                    }
                },
                0L);
    }

    private static BigInteger parseBigInt(final List parsingTemplate,
                                  final ScriptEngine engine,
                                  final boolean hexFormat) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<BigInteger>() {
                    @Override
                    public BigInteger transform(final String input) {
                        return new BigInteger(input, hexFormat ? 16 : 10);
                    }
                },
                BigInteger.ZERO);
    }

    private static BigDecimal parseDecimal(final List parsingTemplate,
                                          final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<BigDecimal>() {
                    @Override
                    public BigDecimal transform(final String input) {
                        return new BigDecimal(input);
                    }
                },
                BigDecimal.ZERO);
    }

    private static String parseString(final List parsingTemplate,
                                           final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<String>() {
                    @Override
                    public String transform(final String input) {
                        return input;
                    }
                },
                "");
    }

    private static Boolean parseBoolean(final List parsingTemplate,
                                           final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Boolean>() {
                    @Override
                    public Boolean transform(final String input) {
                        switch (input.toLowerCase()) {
                            case "1":
                            case "true":
                            case "yes":
                            case "ok":
                                return true;
                            default:
                                return false;
                        }
                    }
                },
                Boolean.FALSE);
    }

    private static Byte[] parseBlob(final List parsingTemplate,
                                           final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Byte[]>() {
                    @Override
                    public Byte[] transform(final String input) {
                        return ArrayUtils.toObject(DatatypeConverter.parseHexBinary(input));
                    }
                },
                new Byte[0]);
    }

    private static Byte[] parseBase64(final List parsingTemplate,
                                    final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Byte[]>() {
                    @Override
                    public Byte[] transform(final String input) {
                        return ArrayUtils.toObject(DatatypeConverter.parseBase64Binary(input));
                    }
                },
                new Byte[0]);
    }

    private Object parse(final String input, final ScriptEngine engine) throws ScriptException{
        try(final StringReader reader = new StringReader(input)){
            engine.put("stream", new ParsingStream(reader));
            switch (getReturnType()){
                case BYTE: return parseByte(parsingTemplate, engine, false);
                case HEX_BYTE: return parseByte(parsingTemplate, engine, true);
                case SHORT: return parseShort(parsingTemplate, engine, false);
                case HEX_SHORT: return parseShort(parsingTemplate, engine, true);
                case INTEGER: return parseInteger(parsingTemplate, engine, false);
                case HEX_INT: return parseInteger(parsingTemplate, engine, true);
                case LONG: return parseLong(parsingTemplate, engine, false);
                case HEX_LONG: return parseLong(parsingTemplate, engine, true);
                case BIG_INTEGER: return parseBigInt(parsingTemplate, engine, false);
                case HEX_BIGINT: return parseBigInt(parsingTemplate, engine, true);
                case BIG_DECIMAL: return parseDecimal(parsingTemplate, engine);
                case STRING: return parseString(parsingTemplate, engine);
                case BOOLEAN: return parseBoolean(parsingTemplate, engine);
                case HEX_BLOB: return parseBlob(parsingTemplate, engine);
                case BASE64_BLOB: return parseBase64(parsingTemplate, engine);
                default: throw new IllegalStateException(String.format("Invalid return type %s", getReturnType()));
            }
        }
    }
}
