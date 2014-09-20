package com.itworks.jcommands.impl;

import com.itworks.snamp.internal.semantics.Internal;
import org.apache.commons.collections4.Transformer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private static final class HexadecimalParser implements NumberParser {
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
                return new BigInteger(input, 16);
            } catch (final NumberFormatException e) {
                throw new ParseException(e.getMessage(), 0);
            }
        }

        @Override
        public BigDecimal parseAsBigDecimal(final String input) throws ParseException {
            try {
                return new BigDecimal(input);
            }
            catch (final NumberFormatException e){
                throw new ParseException(e.getMessage(), 0);
            }
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

        @XmlAttribute(name = "name", namespace = XmlConstants.NAMESPACE, required = true)
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

        @XmlAttribute(name = "type", namespace = XmlConstants.NAMESPACE, required = true)
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
        @XmlAttribute(name = "key", namespace = XmlConstants.NAMESPACE, required = true)
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

        @XmlAttribute(name = "type", namespace = XmlConstants.NAMESPACE, required = true)
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
    public static final class ParsingStream extends StreamTokenizer {

        private ParsingStream(final StringReader reader) {
            super(reader);
            resetSyntax();
        }

        /**
         * Setup the default syntax for this tokenizer.
         */
        public final void setupDefaultSyntax() {
            wordChars('a', 'z');
            wordChars('A', 'Z');
            wordChars(128 + 32, 255);
            whitespaceChars(0, ' ');
            commentChar('/');
            quoteChar('"');
            quoteChar('\'');
            parseNumbers();
        }

        private Object lookup(final int tokenType) {
            switch (tokenType) {
                case TT_WORD:
                    return sval;
                case TT_NUMBER:
                    return nval;
                default:
                    return null;
            }
        }

        public final Object lookup() {
            return lookup(ttype);
        }

        /**
         * Parses the next token in the stream.
         *
         * @return The next parsed token in the stream (may be {@link java.lang.String} or {@link java.lang.Double}).
         * @throws IOException
         */
        public final Object parseNextToken() throws IOException {
            return lookup(nextToken());
        }

        private Byte parseByte(final Byte defval, final NumberParser format) throws IOException {
            switch (nextToken()) {
                case TT_NUMBER:
                    return (byte) nval;
                case TT_WORD:
                    try {
                        return format.parseAsByte(sval);
                    } catch (final ParseException ignored) {
                        return defval;
                    }
                default:
                    return null;
            }
        }

        /**
         * Parses the next token in the stream as {@link java.lang.Byte}.
         *
         * @return {@link java.lang.Byte} representation of the next token in the stream;
         * or {@literal null}, if the next token is not a number.
         * @throws IOException Some I/O problem occurs in the underlying stream.
         */
        public final Byte parseByte() throws IOException {
            return parseByte(null, DEFAULT_NUMBER_FORMAT);
        }


        /**
         * Parses the next token in the stream as {@link java.lang.Byte}.
         *
         * @param defval The value returned by the method if the next token is not a number.
         * @return {@link java.lang.Byte} representation of the next token in the stream;
         * or {@literal null}, if the next token is not a number.
         * @throws IOException Some I/O problem occurs in the underlying stream.
         */
        public final byte parseByte(final byte defval) throws IOException {
            return parseByte(defval, DEFAULT_NUMBER_FORMAT);
        }

        public final byte parseByte(final byte defval, final String pattern) throws IOException {
            return parseByte(defval, createNumberParser(pattern));
        }

        private Short parseShort(final Short defval, final NumberParser parser) throws IOException {
            switch (nextToken()) {
                case TT_NUMBER:
                    return (short) nval;
                case TT_WORD:
                    try {
                        return parser.parseAsShort(sval);
                    } catch (final ParseException ignored) {
                        return defval;
                    }
                default:
                    return defval;
            }
        }

        /**
         * Parses the next token in the stream as {@link java.lang.Short}.
         *
         * @return {@link java.lang.Short} representation of the next token in the stream;
         * or {@literal null}, if the next token is not a number.
         * @throws IOException Some I/O problem occurs in the underlying stream.
         */
        public final Short parseShort() throws IOException {
            return parseShort(null, DEFAULT_NUMBER_FORMAT);
        }

        /**
         * Parses the next token in the stream as {@link java.lang.Short}.
         *
         * @param defval The value returned by the method if the next token is not a number.
         * @return {@link java.lang.Short} representation of the next token in the stream;
         * or {@literal null}, if the next token is not a number.
         * @throws IOException Some I/O problem occurs in the underlying stream.
         */
        public final short parseShort(final short defval) throws IOException {
            return parseShort(defval, DEFAULT_NUMBER_FORMAT);
        }

        public final short parseShort(final short defval, final String pattern) throws IOException {
            return parseShort(defval, createNumberParser(pattern));
        }

        private Integer parseInt(final Integer defval, final NumberParser parser) throws IOException {
            switch (nextToken()) {
                case TT_NUMBER:
                    return (int) nval;
                case TT_WORD:
                    try {
                        return parser.parseAsInt(sval);
                    } catch (final ParseException ignored) {
                        return defval;
                    }
                default:
                    return defval;
            }
        }

        /**
         * Parses the next token in the stream as {@link java.lang.Integer}.
         *
         * @return {@link java.lang.Integer} representation of the next token in the stream;
         * or {@literal null}, if the next token is not a number.
         * @throws IOException Some I/O problem occurs in the underlying stream.
         */
        public final Integer parseInt() throws IOException {
            return parseInt(null, DEFAULT_NUMBER_FORMAT);
        }

        /**
         * Parses the next token in the stream as {@link java.lang.Integer}.
         *
         * @param defval The value returned by the method if the next token is not a number.
         * @return {@link java.lang.Integer} representation of the next token in the stream;
         * or {@literal null}, if the next token is not a number.
         * @throws IOException Some I/O problem occurs in the underlying stream.
         */
        public final int parseInt(final int defval) throws IOException {
            return parseInt(defval, DEFAULT_NUMBER_FORMAT);
        }

        private Long parseLong(final Long defval, final NumberParser parser) throws IOException {
            switch (nextToken()) {
                case TT_NUMBER:
                    return (long) nval;
                case TT_WORD:
                    try {
                        return parser.parseAsLong(sval);
                    } catch (final ParseException ignored) {
                        return defval;
                    }
                default:
                    return defval;
            }
        }

        /**
         * Parses the next token in the stream as {@link java.lang.Integer}.
         *
         * @return {@link java.lang.Integer} representation of the next token in the stream;
         * or {@literal null}, if the next token is not a number.
         * @throws IOException Some I/O problem occurs in the underlying stream.
         */
        public final Long parseLong() throws IOException {
            return parseLong(null, DEFAULT_NUMBER_FORMAT);
        }

        /**
         * Parses the next token in the stream as {@link java.lang.Long}.
         *
         * @param defval The value returned by the method if the next token is not a number.
         * @return {@link java.lang.Long} representation of the next token in the stream;
         * or {@literal null}, if the next token is not a number.
         * @throws IOException Some I/O problem occurs in the underlying stream.
         */
        public final long parseLong(final long defval) throws IOException {
            return parseLong(defval, DEFAULT_NUMBER_FORMAT);
        }

        public final long parseLong(final long defval, final String pattern) throws IOException {
            return parseLong(defval, createNumberParser(pattern));
        }

        private BigInteger parseBigInt(final BigInteger defval, final NumberParser parser) throws IOException {
            switch (nextToken()) {
                case TT_NUMBER:
                    return BigInteger.valueOf((long) nval);
                case TT_WORD:
                    try {
                        return parser.parseAsBigInteger(sval);
                    } catch (final ParseException ignored) {
                        return defval;
                    }
                default:
                    return defval;
            }
        }

        public final BigInteger parseBigInt() throws IOException {
            return parseBigInt(null, DEFAULT_NUMBER_FORMAT);
        }

        public final BigInteger parseBigInt(final BigInteger defval) throws IOException {
            return parseBigInt(defval, DEFAULT_NUMBER_FORMAT);
        }

        public final BigInteger parseBigInt(final BigInteger defval, final String pattern) throws IOException {
            return parseBigInt(defval, createNumberParser(pattern));
        }

        private BigDecimal parseDecimal(final BigDecimal defval, final NumberParser parser) throws IOException {
            switch (nextToken()) {
                case TT_NUMBER:
                    return BigDecimal.valueOf(nval);
                case TT_WORD:
                    try {
                        return parser.parseAsBigDecimal(sval);
                    } catch (final ParseException ignored) {
                        return defval;
                    }
                default:
                    return defval;
            }
        }

        public final BigDecimal parseDecimal() throws IOException {
            return parseDecimal(null, DEFAULT_NUMBER_FORMAT);
        }

        public final BigDecimal parseDecimal(final BigDecimal defval) throws IOException {
            return parseDecimal(defval, DEFAULT_NUMBER_FORMAT);
        }

        public final BigDecimal parseDecimal(final BigDecimal defval, final String format) throws IOException {
            return parseDecimal(defval, createNumberParser(format));
        }

        public final String parseWord() throws IOException {
            switch (nextToken()) {
                case TT_NUMBER:
                    return Double.toString(nval);
                case TT_WORD:
                    return sval;
                default:
                    return "";
            }
        }

        @SuppressWarnings("UnusedDeclaration")
        public final boolean parseBoolean() throws IOException {
            switch (nextToken()) {
                case TT_NUMBER:
                    return nval != 0.0;
                case TT_WORD:
                    switch (sval.toLowerCase()) {
                        case "true":
                        case "1":
                        case "yes":
                        case "ok":
                            return true;
                    }
                default:
                    return false;
            }
        }

        private Date parseDate(final Date defval, final DateParser parser) throws IOException {
            switch (nextToken()) {
                case TT_NUMBER:
                    return new Date((long) nval);
                case TT_WORD:
                    try {
                        return parser.parse(sval);
                    } catch (ParseException ignored) {
                        return defval;
                    }
                default:
                    return defval;
            }
        }

        @SuppressWarnings("UnusedDeclaration")
        public final Date parseDate() throws IOException {
            return parseDate(null, DEFAULT_DATE_TIME_FORMAT);
        }

        @SuppressWarnings("UnusedDeclaration")
        public final Date parseDate(final Date defval) throws IOException {
            return parseDate(defval, DEFAULT_DATE_TIME_FORMAT);
        }

        @SuppressWarnings("UnusedDeclaration")
        public final Date parseDate(final Date defval, final String format) throws IOException {
            return parseDate(defval, new SimpleDateParser(format));
        }

        private Float parseFloat(final Float defval, final NumberParser parser) throws IOException {
            switch (nextToken()) {
                case TT_NUMBER:
                    return (float) nval;
                case TT_WORD:
                    try {
                        return parser.parseAsFloat(sval);
                    } catch (final ParseException ignored) {
                        return defval;
                    }
                default:
                    return defval;
            }
        }

        @SuppressWarnings("UnusedDeclaration")
        public Float parseFloat() throws IOException {
            return parseFloat(null, DEFAULT_NUMBER_FORMAT);
        }

        @SuppressWarnings("UnusedDeclaration")
        public float parseFloat(final float defval) throws IOException {
            return parseFloat(defval, DEFAULT_NUMBER_FORMAT);
        }

        @SuppressWarnings("UnusedDeclaration")
        public float parseFloat(final float defval, final String format) throws IOException {
            return parseFloat(defval, createNumberParser(format));
        }

        private Double parseDouble(final Double defval, final NumberParser parser) throws IOException {
            switch (nextToken()) {
                case TT_NUMBER:
                    return nval;
                case TT_WORD:
                    try {
                        return parser.parseAsDouble(sval);
                    } catch (final ParseException ignored) {
                        return defval;
                    }
                default:
                    return defval;
            }
        }

        @SuppressWarnings("UnusedDeclaration")
        public final Double parseDouble() throws IOException {
            return parseDouble(null, DEFAULT_NUMBER_FORMAT);
        }

        @SuppressWarnings("UnusedDeclaration")
        public final double parseDouble(final double defval) throws IOException {
            return parseDouble(defval, DEFAULT_NUMBER_FORMAT);
        }

        @SuppressWarnings("UnusedDeclaration")
        public final double parseDouble(final double defval, final String format) throws IOException {
            return parseDouble(defval, createNumberParser(format));
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

    private String language;
    private XmlCommandLineToolReturnType returnType;
    private List parsingTemplate;
    private NumberParser numberFormatter;
    private DateParser dateFormatter;

    /**
     * Initializes a new parser without settings.
     */
    public XmlCommandLineToolOutputParser(){
        language = REGEXP_LANG;
        returnType = XmlCommandLineToolReturnType.STRING;
        parsingTemplate = null;
        numberFormatter = DEFAULT_NUMBER_FORMAT;
        dateFormatter = DEFAULT_DATE_TIME_FORMAT;
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
        else if(Objects.equals(HexadecimalParser.PATTERN_STUB, format))
            return new HexadecimalParser();
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

    public final void setParsingLanguage(final String value){
        this.language = value != null ? value : "";
    }

    @XmlAttribute(name = "type", namespace = XmlConstants.NAMESPACE, required = true)
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
        if(parsingTemplate == null) parsingTemplate = new ArrayList(10);
        return parsingTemplate;
    }

    public final void removeParsingRules() {
        if (parsingTemplate != null) parsingTemplate.clear();
        parsingTemplate = null;
    }

    @SuppressWarnings("unchecked")
    public final void addParsingRule(final String placeholder){
        getParsingTemplate().add(Objects.requireNonNull(placeholder, "placeholder is null."));
    }

    @SuppressWarnings("unchecked")
    public final void addParsingRule(final ParsingRule rule){
        if(rule == null) throw new NullPointerException("rule is null.");
        else if(!rule.compatibleWith(returnType)) throw new IllegalArgumentException(String.format("Incompatible rule with underlying type %s", returnType));
        else getParsingTemplate().add(rule);
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

    /**
     * Adds parsing rule for the array item.
     * @param itemParsingRule The parsing rule for the array item.
     */
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

    private Object parse(final String input, final ScriptEngine engine) throws ScriptException{
        try(final StringReader reader = new StringReader(input)){
            engine.put("stream", new ParsingStream(reader));
            switch (getReturnType()){
                case BYTE: return parseByte(getParsingTemplate(), numberFormatter, engine);
                case SHORT: return parseShort(getParsingTemplate(), numberFormatter, engine);
                case INTEGER: return parseInteger(getParsingTemplate(), numberFormatter, engine);
                case LONG: return parseLong(getParsingTemplate(), numberFormatter, engine);
                case FLOAT: return parseFloat(getParsingTemplate(), numberFormatter, engine);
                case DOUBLE: return parseDouble(getParsingTemplate(), numberFormatter, engine);
                case BIG_INTEGER: return parseBigInt(getParsingTemplate(), numberFormatter, engine);
                case BIG_DECIMAL: return parseDecimal(getParsingTemplate(), numberFormatter, engine);
                case STRING: return parseString(getParsingTemplate(), engine);
                case BOOLEAN: return parseBoolean(getParsingTemplate(), engine);
                case DATE_TIME: return parseDate(getParsingTemplate(), dateFormatter, engine);
                default: throw new IllegalStateException(String.format("Invalid return type %s", getReturnType()));
            }
        }
    }
}
