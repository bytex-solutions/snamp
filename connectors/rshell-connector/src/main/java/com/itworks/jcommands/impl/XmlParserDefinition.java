package com.itworks.jcommands.impl;

import com.itworks.snamp.internal.annotations.Internal;
import org.apache.commons.collections4.ResettableListIterator;
import org.apache.commons.collections4.iterators.ListIteratorWrapper;
import org.apache.commons.collections4.map.LRUMap;

import javax.script.*;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Represents parser for command-line output result.
 * <p>
 *     The parser accepts unstructured text from command-line stdout
 *     and converts it into the typed object, such as {@link java.lang.String},
 *     {@link java.lang.Integer}, {@link java.util.Map} and etc.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see XmlParsingResultType
 */
@XmlType(name = "CommandLineToolOutputParser", namespace = XmlConstants.NAMESPACE)
@XmlAccessorType(XmlAccessType.PROPERTY)
public class XmlParserDefinition {
    private static final class RegexScriptEngine extends AbstractScriptEngine{
        private final ScriptEngine javaScriptEngine;

        public RegexScriptEngine(final ScriptEngineManager manager){
            javaScriptEngine = manager.getEngineByName(JAVASCRIPT_LANG);
        }

        /**
         * Causes the immediate execution of the script whose source is the String
         * passed as the first argument.  The script may be reparsed or recompiled before
         * execution.  State left in the engine from previous executions, including
         * variable values and compiled procedures may be visible during this execution.
         *
         * @param script  The script to be executed by the script engine.
         * @param context A <code>ScriptContext</code> exposing sets of attributes in
         *                different scopes.  The meanings of the scopes <code>ScriptContext.GLOBAL_SCOPE</code>,
         *                and <code>ScriptContext.ENGINE_SCOPE</code> are defined in the specification.
         *                <br><br>
         *                The <code>ENGINE_SCOPE</code> <code>Bindings</code> of the <code>ScriptContext</code> contains the
         *                bindings of scripting variables to application objects to be used during this
         *                script execution.
         * @return The value returned from the execution of the script.
         * @throws javax.script.ScriptException if an error occurrs in script. ScriptEngines should create and throw
         *                                      <code>ScriptException</code> wrappers for checked Exceptions thrown by underlying scripting
         *                                      implementations.
         * @throws NullPointerException         if either argument is null.
         */
        @Override
        public Object eval(final String script, final ScriptContext context) throws ScriptException {
            //Wraps regexp into the scanner expression
            return javaScriptEngine.eval(String.format("scan.next('%s');", script), context);
        }

        /**
         * Same as <code>eval(String, ScriptContext)</code> where the source of the script
         * is read from a <code>Reader</code>.
         *
         * @param reader  The source of the script to be executed by the script engine.
         * @param context The <code>ScriptContext</code> passed to the script engine.
         * @return The value returned from the execution of the script.
         * @throws javax.script.ScriptException if an error occurrs in script.
         * @throws NullPointerException         if either argument is null.
         */
        @Override
        public Object eval(final Reader reader, final ScriptContext context) throws ScriptException {
            final StringBuilder script = new StringBuilder();
            final char[] buffer = new char[256];
            int count;
            try {
                while ((count = reader.read(buffer)) > 0)
                    script.append(buffer, 0, count);
            } catch (final IOException e) {
                throw new ScriptException(e);
            }
            return eval(script.toString(), context);
        }

        /**
         * Returns an uninitialized <code>Bindings</code>.
         *
         * @return A <code>Bindings</code> that can be used to replace the state of this <code>ScriptEngine</code>.
         */
        @Override
        public Bindings createBindings() {
            return javaScriptEngine.createBindings();
        }

        /**
         * Returns a <code>ScriptEngineFactory</code> for the class to which this <code>ScriptEngine</code> belongs.
         *
         * @return The <code>ScriptEngineFactory</code>
         */
        @Override
        public ScriptEngineFactory getFactory() {
            throw new UnsupportedOperationException();
        }
    }

    private static interface PatternBasedFormat extends Serializable, Cloneable{
        String toPattern();
    }

    private static interface DateParser extends PatternBasedFormat{
        Date parse(final String input) throws ParseException;
    }

    private static final class SimpleDateParser extends SimpleDateFormat implements DateParser {
        public SimpleDateParser(final String pattern) {
            super(pattern);
        }
    }

    private static interface NumberParser extends PatternBasedFormat{
        Number parse(final String input) throws ParseException;
        byte parseAsByte(final String input) throws ParseException;
        short parseAsShort(final String input) throws ParseException;
        int parseAsInt(final String input) throws ParseException;
        long parseAsLong(final String input) throws ParseException;
        BigInteger parseAsBigInteger(final String input) throws ParseException;
        BigDecimal parseAsBigDecimal(final String input) throws ParseException;
        float parseAsFloat(final String input) throws ParseException;
        double parseAsDouble(final String input) throws ParseException;
    }

    private static final class DecimalNumberParser extends DecimalFormat implements NumberParser{
        public DecimalNumberParser(final String pattern){
            super(pattern);
        }

        public DecimalNumberParser(){

        }

        @Override
        public byte parseAsByte(final String input) throws ParseException {
            return parse(input).byteValue();
        }

        @Override
        public short parseAsShort(final String input) throws ParseException {
            return parse(input).shortValue();
        }

        @Override
        public int parseAsInt(final String input) throws ParseException {
            return parse(input).intValue();
        }

        @Override
        public long parseAsLong(final String input) throws ParseException {
            return parse(input).longValue();
        }

        @Override
        public BigInteger parseAsBigInteger(final String input) throws ParseException {
            final Number n = parse(input);
            return n instanceof BigInteger ? (BigInteger)n : BigInteger.valueOf(n.longValue());
        }

        @Override
        public BigDecimal parseAsBigDecimal(final String input) throws ParseException {
            final DecimalFormat format = (DecimalFormat)clone();
            format.setParseBigDecimal(true);
            return (BigDecimal)format.parse(input);
        }

        @Override
        public float parseAsFloat(final String input) throws ParseException {
            return parse(input).floatValue();
        }

        @Override
        public double parseAsDouble(final String input) throws ParseException {
            return parse(input).doubleValue();
        }
    }

    private static final class HexadecimalNumberParser implements NumberParser {
        private static final String PATTERN_STUB = "hex";

        @Override
        public Number parse(final String input) throws ParseException {
            try {
                return Long.parseLong(input, 16);
            } catch (final NumberFormatException e) {
                throw new ParseException(e.getMessage(), 0);
            }
        }

        @Override
        public byte parseAsByte(final String input) throws ParseException {
            try {
                return Byte.parseByte(input, 16);
            }
            catch (final NumberFormatException e){
                throw new ParseException(e.getMessage(), 0);
            }
        }

        @Override
        public short parseAsShort(final String input) throws ParseException {
            return Short.parseShort(input, 16);
        }

        @Override
        public int parseAsInt(final String input) throws ParseException {
            try {
                return Integer.parseInt(input, 16);
            }
            catch (final NumberFormatException e){
                throw new ParseException(e.getMessage(), 0);
            }
        }

        @Override
        public long parseAsLong(final String input) throws ParseException {
            try {
                return Long.parseLong(input, 16);
            }
            catch (final NumberFormatException e){
                throw new ParseException(e.getMessage(), 0);
            }
        }

        @Override
        public BigInteger parseAsBigInteger(final String input) throws ParseException {
            try {
                return new BigInteger(DatatypeConverter.parseHexBinary(input));
            }
            catch (final IllegalArgumentException e){
                throw new ParseException(e.getMessage(), 0);
            }
        }

        @Override
        public BigDecimal parseAsBigDecimal(final String input) throws ParseException {
            throw new ParseException("HEX format is not supported for BigDecimal.", 0);
        }

        @Override
        public float parseAsFloat(final String input) throws ParseException {
            try {
                return Float.parseFloat(input);
            }
            catch (final NumberFormatException e){
                throw new ParseException(e.getMessage(), 0);
            }
        }

        @Override
        public double parseAsDouble(final String input) throws ParseException {
            try{
                return Double.parseDouble(input);
            }
            catch (final NumberFormatException e){
                throw new ParseException(e.getMessage(), 0);
            }
        }

        @Override
        public String toPattern() {
            return PATTERN_STUB;
        }
    }

    private static final class ArrayBuilder extends ArrayList<Object> {
        private Class<?> elementType;

        public ArrayBuilder() {
            super(10);
            elementType = String.class;
        }

        public void setElementType(final Class<?> value) {
            this.elementType = value;
        }

        public void setElementType(final XmlParsingResultType value) {
            setElementType(value.underlyingType);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Object[] toArray() {
            final Object result = Array.newInstance(elementType, size());
            for (int i = 0; i < size(); i++)
                Array.set(result, i, get(i));
            return (Object[]) result;
        }
    }

    /**
     * Represents parser for basic data types. This class cannot be inherited
     * or instantiated directly from your code.
     * @author Roman Sakno
     * @since 1.0
     */
    @SuppressWarnings("UnusedDeclaration")
    @Internal
    public static final class DataParser{
        private final LRUMap<String, NumberParser> cachedNumberParsers;
        private final LRUMap<String, DateParser> cachedDateParsers;

        private DataParser(){
            cachedNumberParsers = new LRUMap<>(8);
            cachedDateParsers = new LRUMap<>(4);
        }

        private NumberParser getNumberParser(final String format){
            if(cachedNumberParsers.containsKey(format))
                return cachedNumberParsers.get(format);
            else {
                final NumberParser parser = createNumberParser(format);
                cachedNumberParsers.put(format, parser);
                return parser;
            }
        }

        private DateParser getDateParser(final String format) {
            if (cachedDateParsers.containsKey(format))
                return cachedDateParsers.get(format);
            else {
                final DateParser parser = new SimpleDateParser(format);
                cachedDateParsers.put(format, parser);
                return parser;
            }
        }

        public byte parseByte(final String value) throws NumberFormatException{
            return Byte.parseByte(value);
        }

        public byte parseByte(final String value, final String format) throws ParseException {
            return getNumberParser(format).parseAsByte(value);
        }

        public short parseShort(final String value) throws NumberFormatException{
            return Short.parseShort(value);
        }

        public short parseShort(final String value, final String format) throws ParseException{
            return getNumberParser(format).parseAsShort(value);
        }

        public int parseInt(final String value) throws NumberFormatException{
            return Integer.parseInt(value);
        }

        public int parseInt(final String value, final String format) throws ParseException{
            return getNumberParser(format).parseAsInt(value);
        }

        public long parseLong(final String value) throws NumberFormatException{
            return Long.parseLong(value);
        }

        public long parseLong(final String value, final String format) throws ParseException{
            return getNumberParser(format).parseAsLong(value);
        }

        public float parseFloat(final String value) throws NumberFormatException{
            return Float.parseFloat(value);
        }

        public float parseFloat(final String value, final String format) throws ParseException{
            return getNumberParser(format).parseAsFloat(value);
        }

        public double parseDouble(final String value) throws NumberFormatException{
            return Double.parseDouble(value);
        }

        public double parseDouble(final String value, final String format) throws ParseException{
            return getNumberParser(format).parseAsDouble(value);
        }

        public BigInteger parseBigInteger(final String value) throws NumberFormatException{
            return new BigInteger(value);
        }

        public BigInteger parseBigInteger(final String value, final String format) throws ParseException{
            return getNumberParser(format).parseAsBigInteger(value);
        }

        public BigDecimal parseBigDecimal(final String value) throws NumberFormatException{
            return new BigDecimal(value);
        }

        public BigDecimal parseBigDecimal(final String value, final String format) throws ParseException{
            return getNumberParser(format).parseAsBigDecimal(value);
        }

        public Date parseDate(final String value) throws ParseException {
            return DEFAULT_DATE_TIME_FORMAT.parse(value);
        }

        public Date parseDate(final String value, final String format) throws ParseException{
            return getDateParser(format).parse(value);
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

    private static final DateParser DEFAULT_DATE_TIME_FORMAT = new SimpleDateParser("EEE MMM d HH:mm:ss zzz yyyy");

    private static final NumberParser DEFAULT_NUMBER_FORMAT = new DecimalNumberParser();

    private static final String SCAN_BINDING = "scan";
    private static final String PARSER_BINDING = "parser";

    private String language;
    private XmlParsingResultType returnType;
    private List parsingTemplate;
    private NumberParser numberFormatter;
    private DateParser dateFormatter;
    private BLOBFormat blobFormatter;

    /**
     * Initializes a new parser without settings.
     */
    public XmlParserDefinition(){
        language = JAVASCRIPT_LANG;
        returnType = XmlParsingResultType.STRING;
        parsingTemplate = null;
        numberFormatter = DEFAULT_NUMBER_FORMAT;
        dateFormatter = DEFAULT_DATE_TIME_FORMAT;
        blobFormatter = BLOBFormat.HEX;
    }

    @XmlAttribute(name = "blobFormat", namespace = XmlConstants.NAMESPACE, required = false)
    public final void setBlobParsingFormat(final BLOBFormat value){
        blobFormatter = value;
    }

    public final BLOBFormat getBlobParsingFormat(){
        return blobFormatter;
    }

    @XmlAttribute(name = "dateTimeFormat", namespace = XmlConstants.NAMESPACE, required = false)
    public final void setDateTimeParsingFormat(final String value){
        if(value == null || value.isEmpty())
            dateFormatter = DEFAULT_DATE_TIME_FORMAT;
        else dateFormatter = new SimpleDateParser(value);
    }

    public final String getDateTimeParsingFormat(){
        return dateFormatter.toPattern();
    }

    private static NumberParser createNumberParser(final String format){
        if(format == null || format.isEmpty())
            return DEFAULT_NUMBER_FORMAT;
        else if(Objects.equals(HexadecimalNumberParser.PATTERN_STUB, format))
            return new HexadecimalNumberParser();
        else return new DecimalNumberParser(format);
    }

    @XmlAttribute(name = "numberFormat", namespace = XmlConstants.NAMESPACE, required = false)
    public final void setNumberParsingFormat(final String value){
        numberFormatter = createNumberParser(value);
    }

    public final String getNumberParsingFormat(){
        return numberFormatter.toPattern();
    }

    @XmlAttribute(name = "language", namespace = XmlConstants.NAMESPACE, required = true)
    public final String getParsingLanguage(){
        return language;
    }

    public final void setParsingLanguage(final String value) {
        this.language = value != null ? value : "";
    }

    @XmlAttribute(name = "type", namespace = XmlConstants.NAMESPACE, required = true)
    public final XmlParsingResultType getParsingResultType(){
        return returnType;
    }

    public final void setParsingResultType(final XmlParsingResultType value){
        this.returnType = value;
    }

    @XmlMixed
    @XmlElementRefs({
            @XmlElementRef(type = DictionaryEntryParsingRule.class),
            @XmlElementRef(type = LineTerminationParsingRule.class),
            @XmlElementRef(type = TableColumnParsingRule.class),
            @XmlElementRef(type = ArrayItemParsingRule.class),
            @XmlElementRef(type = PlaceholderParsingRule.class),
            @XmlElementRef(type = ConstantParsingRule.class)
    })
    private List getParsingTemplate(){
        if(parsingTemplate == null) parsingTemplate = new ArrayList(10);
        return parsingTemplate;
    }

    public final void removeParsingRules() {
        if (parsingTemplate != null) parsingTemplate.clear();
        parsingTemplate = null;
    }

    @SuppressWarnings("unchecked")
    public final void addParsingRule(final String expression){
        getParsingTemplate().add(Objects.requireNonNull(expression, "expression is null."));
    }

    public final void addPlaceholder(final String placeholder) {
        final PlaceholderParsingRule rule = new PlaceholderParsingRule();
        rule.setRule(placeholder);
        addParsingRule(rule);
    }

    public final void addConstantDef(final Object value){
        final ConstantParsingRule def = new ConstantParsingRule();
        def.setValue(value.toString());
        addParsingRule(def);
    }

    @SuppressWarnings("unchecked")
    public final void addParsingRule(final ParsingRule rule){
        if(rule == null) throw new NullPointerException("rule is null.");
        else if(!rule.compatibleWith(returnType)) throw new IllegalArgumentException(String.format("Incompatible rule with underlying type %s", returnType));
        else getParsingTemplate().add(rule);
    }

    public final void addDictionaryEntryRule(final String key, final String valueRule, final XmlParsingResultType valueType){
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

    public final void addTableColumn(final String columnName, final String columnParsingRule, final XmlParsingResultType columnType){
        final TableColumnParsingRule rule = new TableColumnParsingRule();
        rule.setColumnName(columnName);
        rule.setColumnType(columnType);
        rule.setColumnValueParsingRule(columnParsingRule);
        addParsingRule(rule);
    }

    /**
     * Adds parsing rule for the array item.
     * @param itemParsingRule The parsing rule for the array item.
     * @param elementType Type of the array elements.
     */
    public final void addArrayItem(final String itemParsingRule, final XmlParsingResultType elementType){
        final ArrayItemParsingRule rule = new ArrayItemParsingRule();
        rule.setItemParsingRule(itemParsingRule);
        rule.setElementType(elementType);
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
            final ScriptEngine scriptEngine = createScriptEngine(scriptManager);
            if(scriptEngine == null) throw new IllegalStateException(String.format("Script engine %s not found", getParsingLanguage()));
            else return parse(input, scriptEngine);
        }
    }

    private ScriptEngine createScriptEngine(final ScriptEngineManager manager){
        final String language;
        switch (language = getParsingLanguage()){
            case REGEXP_LANG: return new RegexScriptEngine(manager);
            default: return manager.getEngineByName(language);
        }
    }

    private static <T> T parseScalar(final List parsingTemplate,
                                     final ScriptEngine engine,
                                     final Converter<T> converter,
                                     final T defaultValue) throws ScriptException {
        if (parsingTemplate.isEmpty())
            return defaultValue;
        for (final Object templateFragment : parsingTemplate)
            if (templateFragment instanceof String) {
                final Object result = engine.eval(templateFragment.toString());
                return result != null ? converter.transform(result.toString()) : defaultValue;
            }
            else if (templateFragment instanceof PlaceholderParsingRule)
                runPlaceholder(((PlaceholderParsingRule) templateFragment).getRule(), engine);
            else if(templateFragment instanceof ConstantParsingRule)
                return converter.transform(((ConstantParsingRule)templateFragment).getValue());
        throw new ScriptException("Parsing rule doesn't contain parser for scalar value.");
    }

    private static Short parseShort(final List parsingTemplate,
                                    final NumberParser format,
                                    final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Short>() {
                    @Override
                    public Short transform(final String input) {
                        try {
                            return format.parseAsShort(input);
                        } catch (final ParseException e) {
                            throw new NumberFormatException(e.getMessage());
                        }
                    }
                },
                (short) 0);
    }

    private static Byte parseByte(final List parsingTemplate,
                                  final NumberParser format,
                                    final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Byte>() {
                    @Override
                    public Byte transform(final String input) {
                        try {
                            return format.parseAsByte(input);
                        } catch (final ParseException e) {
                            throw new NumberFormatException(e.getMessage());
                        }
                    }
                },
                (byte) 0);
    }

    private static Integer parseInteger(final List parsingTemplate,
                                        final NumberParser format,
                                  final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Integer>() {
                    @Override
                    public Integer transform(final String input) {
                        try {
                            return format.parseAsInt(input);
                        } catch (final ParseException e) {
                            throw new NumberFormatException(e.getMessage());
                        }
                    }
                },
                0);
    }

    private static Long parseLong(final List parsingTemplate,
                                  final NumberParser format,
                                        final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Long>() {
                    @Override
                    public Long transform(final String input) {
                        try {
                            return format.parseAsLong(input);
                        } catch (final ParseException e) {
                            throw new NumberFormatException(e.getMessage());
                        }
                    }
                },
                0L);
    }

    private static BigInteger parseBigInt(final List parsingTemplate,
                                  final NumberParser format,
                                  final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<BigInteger>() {
                    @Override
                    public BigInteger transform(final String input) {
                        try {
                            return format.parseAsBigInteger(input);
                        } catch (final ParseException e) {
                            throw new NumberFormatException(e.getMessage());
                        }
                    }
                },
                BigInteger.ZERO);
    }

    private static BigDecimal parseDecimal(final List parsingTemplate,
                                           final NumberParser parser,
                                          final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<BigDecimal>() {
                    @Override
                    public BigDecimal transform(final String input) {
                        try {
                            return parser.parseAsBigDecimal(input);
                        } catch (final ParseException e) {
                            throw new NumberFormatException(e.getMessage());
                        }
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

    private static Float parseFloat(final List parsingTemplate,
                                    final NumberParser parser,
                                    final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Float>() {
                    @Override
                    public Float transform(final String input) {
                        try {
                            return parser.parseAsFloat(input);
                        } catch (final ParseException e) {
                            throw new NumberFormatException(e.getMessage());
                        }
                    }
                }, 0F);
    }

    private static Double parseDouble(final List parsingTemplate,
                                    final NumberParser parser,
                                    final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Double>() {
                    @Override
                    public Double transform(final String input) {
                        try {
                            return parser.parseAsDouble(input);
                        } catch (final ParseException e) {
                            throw new NumberFormatException(e.getMessage());
                        }
                    }
                }, 0.0);
    }

    private static Date parseDate(final List parsingTemplate,
                                  final DateParser dateTimeFormat,
                                  final ScriptEngine engine) throws ScriptException{
        return parseScalar(parsingTemplate,
                engine,
                new Converter<Date>() {
                    @Override
                    public Date transform(final String input) {
                        try {
                            return dateTimeFormat.parse(input);
                        } catch (final ParseException ignored) {
                            return new Date(0L);
                        }
                    }
                }, new Date(0L));
    }

    private static Byte[] parseBLOB(final List parsingTemplate,
                                    final BLOBFormat format,
                                    final ScriptEngine engine) throws ScriptException {
        return parseScalar(parsingTemplate,
                engine,
                format, new Byte[0]);
    }

    private static void runPlaceholder(final String fragment, final ScriptEngine engine) throws ScriptException {
        engine.eval(fragment);
    }

    private Object[] parseArray(final ResettableListIterator parsingTemplateIter,
                                     final ScriptEngine engine) throws ScriptException {
        final ArrayBuilder builder = new ArrayBuilder();
        final Scanner stream = (Scanner)engine.get(SCAN_BINDING);
        while (stream.hasNext() && parsingTemplateIter.hasNext()) {
            final Object templateFragment = parsingTemplateIter.next();
            if (templateFragment instanceof String)  //pass through placeholder
                runPlaceholder((String) templateFragment, engine);
            else if (templateFragment instanceof ArrayItemParsingRule) {
                final ArrayItemParsingRule rule = (ArrayItemParsingRule) templateFragment;
                final Object element = parse(rule.getElementType(), Arrays.asList(rule.getItemParsingRule()), engine);
                builder.add(element);
                builder.setElementType(rule.getElementType());
                //...just continue parsing
            } else if (templateFragment instanceof LineTerminationParsingRule) {
                //pass through the line terminator
                runPlaceholder(((LineTerminationParsingRule) templateFragment).getTerminationRule(), engine);
                //...and set parsing template iterator to the initial state
                parsingTemplateIter.reset();
            } else if (templateFragment instanceof PlaceholderParsingRule)
                runPlaceholder(((PlaceholderParsingRule) templateFragment).getRule(), engine);
        }
        return builder.toArray();
    }

    private Map<String, Object> parseDictionary(final ResettableListIterator parsingTemplateIter,
                                                final ScriptEngine engine) throws ScriptException {
        final Map<String, Object> result = new HashMap<>(20);
        final Scanner stream = (Scanner)engine.get(SCAN_BINDING);
        while (stream.hasNext() && parsingTemplateIter.hasNext()){
            final Object templateFragment = parsingTemplateIter.next();
            if(templateFragment instanceof String)
                runPlaceholder((String)templateFragment, engine);
            else if(templateFragment instanceof DictionaryEntryParsingRule){
                final DictionaryEntryParsingRule rule = (DictionaryEntryParsingRule)templateFragment;
                result.put(rule.getKeyName(), parse(rule.getValueType(), Arrays.asList(rule.getValueParsingRule()), engine));
            }
            else if(templateFragment instanceof PlaceholderParsingRule)
                runPlaceholder(((PlaceholderParsingRule)templateFragment).getRule(), engine);
        }
        return result;
    }

    private Collection<Map<String, Object>> parseTable(final ResettableListIterator parsingTemplateIter,
                                                       final ScriptEngine engine) throws ScriptException{
        final List<Map<String, Object>> table = new LinkedList<>();
        Map<String, Object> row = new HashMap<>(20);
        final Scanner stream = (Scanner)engine.get(SCAN_BINDING);
        while (stream.hasNext() && parsingTemplateIter.hasNext()){
            final Object templateFragment = parsingTemplateIter.next();
            if(templateFragment instanceof String)
                runPlaceholder((String)templateFragment, engine);
            else if(templateFragment instanceof TableColumnParsingRule){
                final TableColumnParsingRule rule = (TableColumnParsingRule)templateFragment;
                row.put(rule.getColumnName(), parse(rule.getColumnType(), Arrays.asList(rule.getColumnValueParsingRule()), engine));
            }
            else if(templateFragment instanceof LineTerminationParsingRule){
                table.add(row);
                row = new HashMap<>(20);
                runPlaceholder(((LineTerminationParsingRule)templateFragment).getTerminationRule(), engine);
                parsingTemplateIter.reset();
            }
            else if(templateFragment instanceof PlaceholderParsingRule)
                runPlaceholder(((PlaceholderParsingRule)templateFragment).getRule(), engine);
        }
        return table;
    }

    @SuppressWarnings("unchecked")
    private Object parse(final XmlParsingResultType retType,
                         final List parsingTemplate,
                         final ScriptEngine engine) throws ScriptException {
        switch (retType) {
            case BYTE:
                return parseByte(parsingTemplate, numberFormatter, engine);
            case SHORT:
                return parseShort(parsingTemplate, numberFormatter, engine);
            case INTEGER:
                return parseInteger(parsingTemplate, numberFormatter, engine);
            case LONG:
                return parseLong(parsingTemplate, numberFormatter, engine);
            case FLOAT:
                return parseFloat(parsingTemplate, numberFormatter, engine);
            case DOUBLE:
                return parseDouble(parsingTemplate, numberFormatter, engine);
            case BIG_INTEGER:
                return parseBigInt(parsingTemplate, numberFormatter, engine);
            case BIG_DECIMAL:
                return parseDecimal(parsingTemplate, numberFormatter, engine);
            case STRING:
                return parseString(parsingTemplate, engine);
            case BOOLEAN:
                return parseBoolean(parsingTemplate, engine);
            case DATE_TIME:
                return parseDate(parsingTemplate, dateFormatter, engine);
            case BLOB:
                return parseBLOB(parsingTemplate, blobFormatter, engine);
            case ARRAY:
                return parseArray(new ListIteratorWrapper(parsingTemplate.iterator()),
                        engine);
            case DICTIONARY:
                return parseDictionary(new ListIteratorWrapper(parsingTemplate.iterator()),
                        engine);
            case TABLE:
                return parseTable(new ListIteratorWrapper(parsingTemplate.iterator()),
                        engine);
            default:
                throw new IllegalStateException(String.format("Invalid return type %s", getParsingResultType()));
        }
    }

    private Object parse(final String input, final ScriptEngine engine) throws ScriptException{
        try(final Scanner reader = new Scanner(input)){
            engine.put(SCAN_BINDING, reader);
            engine.put(PARSER_BINDING, new DataParser());
            return parse(getParsingResultType(), getParsingTemplate(), engine);
        }
    }
}
