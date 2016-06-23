package com.bytex.snamp.adapters.snmp;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.io.IOUtils;
import com.google.common.base.Supplier;
import com.google.common.primitives.Shorts;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOColumn;
import org.snmp4j.agent.mo.MOTable;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.ArrayType;
import java.io.*;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.adapters.snmp.helpers.OctetStringHelper.SNMP_ENCODING;

/**
 * @author Roman Sakno
 */
final class SnmpHelpers {
    private static final String AUTO_PREFIX_PROPERTY = "com.bytex.snamp.adapters.snmp.oidPrefix";

    private static final TimeZone ZERO_TIME_ZONE = new SimpleTimeZone(0, "UTC");
    private static final AtomicInteger POSTFIX_COUNTER = new AtomicInteger(1);

    private SnmpHelpers(){

    }



    static byte toByte(final long value){
        if(value > Byte.MAX_VALUE)
            return Byte.MAX_VALUE;
        else if(value < Byte.MIN_VALUE)
            return Byte.MIN_VALUE;
        else return (byte)value;
    }

    static short toShort(final long value){
        return Shorts.saturatedCast(value);
    }

    static char toChar(final String value){
        return value == null || value.isEmpty() ? '\0' : value.charAt(0);
    }

    private static Calendar createCalendar() {
        return Calendar.getInstance(ZERO_TIME_ZONE, Locale.ROOT);
    }

    /**
     * Provides date/time formatting using the custom pattern.
     * This class cannot be inherited.
     */
    private static class CustomDateTimeFormatter extends SimpleDateFormat implements DateTimeFormatter{
        private static final String DEFAUT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
        public static final String FORMATTER_NAME = "default";
        private static final long serialVersionUID = -4381345715692133371L;

        public CustomDateTimeFormatter(){
            this(DEFAUT_FORMAT);
        }

        public CustomDateTimeFormatter(final String pattern){
            super(pattern);
        }

        @Override
        public final byte[] convert(final Date value) {
            return format(value).getBytes(IOUtils.DEFAULT_CHARSET);
        }

        private Date convert(final String value) throws ParseException{
            return parse(value);
        }

        @Override
        public final Date convert(final byte[] value) throws ParseException {
            return convert(new String(value, SNMP_ENCODING));
        }
    }

    private static final class Rfc1903BinaryDateTimeFormatter implements DateTimeFormatter{
        public static final String FORMATTER_NAME = "rfc1903";

        private static byte[] convert(final Calendar value){
            try(final ByteArrayOutputStream output = new ByteArrayOutputStream(); final DataOutputStream dataStream = new DataOutputStream(output)){
                dataStream.writeShort(value.get(Calendar.YEAR));
                dataStream.writeByte(value.get(Calendar.MONTH)+1);
                dataStream.writeByte(value.get(Calendar.DAY_OF_MONTH));
                dataStream.writeByte(value.get(Calendar.HOUR_OF_DAY));
                dataStream.writeByte(value.get(Calendar.MINUTE));
                dataStream.writeByte(value.get(Calendar.SECOND));
                dataStream.writeByte(value.get(Calendar.MILLISECOND) / 100);

                int offsetInMillis = value.getTimeZone().getRawOffset();
                char directionFromUTC = '+';
                if (offsetInMillis < 0)
                {
                    directionFromUTC = '-';
                    offsetInMillis = -offsetInMillis;
                }

                dataStream.writeByte(directionFromUTC);
                dataStream.writeByte(offsetInMillis / 3600000); // hours
                dataStream.writeByte((offsetInMillis % 3600000) / 60000); // minutes
                dataStream.flush();
                output.flush();
                return output.toByteArray();
            }
            catch (final IOException e){
                return emptyArray(byte[].class);
            }
        }

        @Override
        public byte[] convert(final Date value) {
            final Calendar cal = createCalendar();
            cal.setTime(value);
            return convert(cal);
        }

        @Override
        public Date convert(final byte[] value) throws ParseException {
            try(final DataInputStream input = new DataInputStream(new ByteArrayInputStream(value))){
                final CalendarBuilder builder = new CalendarBuilder();
                builder.setYear(input.readShort());
                builder.setMonth(input.readByte()-1);
                builder.setDayOfMonth(input.readByte());
                builder.setHourOfDay(input.readByte());
                builder.setMinute(input.readByte());
                builder.setSecond(input.readByte());
                builder.setDeciseconds(input.readByte());
                builder.setDirectionFromUTCPlus(input.readByte() == '+');
                builder.setOffsetInHours(input.readByte());
                builder.setOffsetInMinutes(input.readByte());
                return builder.build().getTime();
            }
            catch (final IOException e){
                throw new ParseException(e.getMessage(), 0);
            }
        }
    }

    private static final class CalendarBuilder{
        private int year;
        private int month;
        private int dayOfMonth;
        private int hourOfDay;
        private int deciseconds;
        private int minute;
        private int second;
        private int offsetInHours;
        private int offsetInMinutes;
        private boolean directionFromUTCPlus;

        public void setYear(final int year) {
            this.year = year;
        }

        public void setMonth(final int month) {
            this.month = month;
        }

        public void setDayOfMonth(final int dayOfMonth) {
            this.dayOfMonth = dayOfMonth;
        }

        public void setHourOfDay(final int hourOfDay) {
            this.hourOfDay = hourOfDay;
        }

        public void setDeciseconds(final int deciseconds) {
            this.deciseconds = deciseconds;
        }

        public void setMinute(final int minute) {
            this.minute = minute;
        }

        public void setSecond(final int second) {
            this.second = second;
        }

        public void setOffsetInHours(final int offsetInHours) {
            this.offsetInHours = offsetInHours;
        }

        public void setOffsetInMinutes(final int offsetInMinutes) {
            this.offsetInMinutes = offsetInMinutes;
        }

        public void setDirectionFromUTCPlus(final boolean directionFromUTCPlus) {
            this.directionFromUTCPlus = directionFromUTCPlus;
        }

        public Calendar build(){
            final Calendar cal = createCalendar();
            int offsetMills = offsetInHours * 3600000 + offsetInMinutes * 60000;
            if(!directionFromUTCPlus) offsetMills = -offsetMills;
            cal.setTimeZone(new SimpleTimeZone(offsetMills, "UTC"));

            cal.set(year, month, dayOfMonth, hourOfDay, minute, second == 60 ? 0 : second);
            cal.set(Calendar.MILLISECOND, deciseconds*100);
            return cal;
        }
    }

    private static final class Rfc1903HumanReadableDateTimeFormatter implements DateTimeFormatter{
        public static final String FORMATTER_NAME = "rfc1903-human-readable";

        private final Pattern pattern;

        public Rfc1903HumanReadableDateTimeFormatter(){
            pattern = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2}),([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])(?:\\.([0-9])),(\\+)(\\d{1,2}):(\\d{1,2})?");
        }

        private static String addLeadingZeroes(final String value, final int requiredLength){
            if(value == null) return addLeadingZeroes("", requiredLength);
            else if(value.length() < requiredLength) return addLeadingZeroes("0".concat(value), requiredLength);
            else return value;
        }

        private static String convert(final Calendar value){
            final String RFC1903_FORMAT = "%s-%s-%s,%s:%s:%s.%s,%s%s:%s";
            //parse components of RFC1903
            final String year = Integer.toString(value.get(Calendar.YEAR));
            final String month = Integer.toString(value.get(Calendar.MONTH) + 1); //Month value is 0-based. e.g., 0 for January.
            final String dayOfMonth = Integer.toString(value.get(Calendar.DAY_OF_MONTH));
            final String hourOfDay = addLeadingZeroes(Integer.toString(value.get(Calendar.HOUR_OF_DAY)), 2);
            final String minute = addLeadingZeroes(Integer.toString(value.get(Calendar.MINUTE)), 2);
            final String second = addLeadingZeroes(Integer.toString(value.get(Calendar.SECOND)), 2);
            final String deciseconds = Integer.toString(value.get(Calendar.MILLISECOND) / 100);
            int offsetInMillis = value.getTimeZone().getRawOffset();
            char directionFromUTC = '+';
            if (offsetInMillis < 0)
            {
                directionFromUTC = '-';
                offsetInMillis = -offsetInMillis;
            }
            final String offsetInHours = Integer.toString(offsetInMillis / 3600000);
            final String offsetInMinutes = Integer.toString((offsetInMillis % 3600000) / 60000);
            return String.format(RFC1903_FORMAT, year, month, dayOfMonth,
                    hourOfDay, minute, second, deciseconds,
                    directionFromUTC, offsetInHours, offsetInMinutes);
        }

        @Override
        public byte[] convert(final Date value) {
            final Calendar cal = createCalendar();
            cal.setTime(value);
            return convert(cal).getBytes(IOUtils.DEFAULT_CHARSET);
        }

        private Date convert(final String value) throws ParseException{
            final Matcher matcher = pattern.matcher(value);
            if (matcher.matches())
            {
                final CalendarBuilder builder = new CalendarBuilder();
                builder.setYear(Integer.parseInt(matcher.group(1)));
                builder.setMonth(Integer.parseInt(matcher.group(2))-1);
                builder.setDayOfMonth(Integer.parseInt(matcher.group(3)));
                builder.setHourOfDay(Integer.parseInt(matcher.group(4)));
                builder.setMinute(Integer.parseInt(matcher.group(5)));
                builder.setSecond(Integer.parseInt(matcher.group(6)));
                builder.setDeciseconds(Integer.parseInt(matcher.group(7)));
                builder.setDirectionFromUTCPlus(matcher.group(8).equals("+"));
                builder.setOffsetInHours(Integer.parseInt(matcher.group(9)));
                builder.setOffsetInMinutes(Integer.parseInt(matcher.group(10)));
                return builder.build().getTime();
            }
            else
                throw new ParseException(String.format("Unable to parse value %s to rfc1903 format", value), 0);
        }

        @Override
        public Date convert(final byte[] value) throws ParseException {
            return convert(new String(value, SNMP_ENCODING));
        }
    }

    static DateTimeFormatter createDateTimeFormatter(final String formatterName){
        if(formatterName == null || formatterName.isEmpty()) return new Rfc1903BinaryDateTimeFormatter();
        else switch (formatterName){
            case CustomDateTimeFormatter.FORMATTER_NAME: return new CustomDateTimeFormatter();
            case Rfc1903BinaryDateTimeFormatter.FORMATTER_NAME: return new Rfc1903BinaryDateTimeFormatter();
            case Rfc1903HumanReadableDateTimeFormatter.FORMATTER_NAME: return new Rfc1903HumanReadableDateTimeFormatter();
            default: return new CustomDateTimeFormatter(formatterName);
        }
    }

    static MOAccess getAccessRestrictions(final MBeanAttributeInfo metadata, final boolean mayCreate){
        switch ((metadata.isWritable() ? 1 : 0) << 1 | (metadata.isReadable() ? 1 : 0)){
            //case 0: case 1:
            default: return MOAccessImpl.ACCESS_READ_ONLY;
            case 2: return mayCreate ? new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_WRITE | MOAccessImpl.ACCESSIBLE_FOR_CREATE) : MOAccessImpl.ACCESS_WRITE_ONLY;
            case 3: return mayCreate ? new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE | MOAccessImpl.ACCESSIBLE_FOR_CREATE) :  MOAccessImpl.ACCESS_READ_WRITE;
        }
    }

    static MOAccess getAccessRestrictions(final MBeanAttributeInfo metadata){
        return getAccessRestrictions(metadata, false);
    }

    static <COLUMN extends MOColumn<?>> COLUMN findColumn(final MOTable<?, ? extends MOColumn<?>, ?> table, final Class<COLUMN> columnType){
        for(final MOColumn<? extends Variable> column: table.getColumns())
            if(columnType.isInstance(column)) return columnType.cast(column);
        return null;
    }

    static int findColumnIndex(final MOTable<?, ? extends MOColumn<?>, ?> table, final Class<? extends MOColumn<?>> columnType){
        final MOColumn<? extends Variable> column = findColumn(table, columnType);
        return column != null ? column.getColumnID() : -1;
    }

    static Object toArray(final List<?> lst, final ArrayType<?> arrayType) {
        final Object result = ArrayUtils.newArray(arrayType, lst.size());
        assert result != null;
        for(int i = 0; i < lst.size(); i++)
            Array.set(result, i, lst.get(i));
        return result;
    }

    static Logger getLogger(){
        return SnmpResourceAdapter.getLoggerImpl();
    }

    private static void log(final Level lvl, final String message, final Object[] args, final Throwable e) {
        getLogger().log(lvl, String.format(message, args), e);
    }

    static void log(final Level lvl, final String message, final Throwable e){
        log(lvl, message, emptyArray(String[].class), e);
    }

    static void log(final Level lvl, final String message, final Object arg0, final Throwable e){
        log(lvl, message, new Object[]{arg0}, e);
    }

    static void log(final Level lvl, final String message, final Object arg0, final Object arg1, final Throwable e){
        log(lvl, message, new Object[]{arg0, arg1}, e);
    }

    static void log(final Level lvl, final String message, final Object arg0, final Object arg1, final Object arg2, final Throwable e){
        log(lvl, message, new Object[]{arg0, arg1, arg2}, e);
    }

    private static OID generateOID(final OID prefix){
        return new OID(prefix).append(POSTFIX_COUNTER.getAndIncrement()).append(0);
    }

    static final Supplier<OID> OID_GENERATOR = () -> {
        final OID prefix = new OID(new OID(System.getProperty(AUTO_PREFIX_PROPERTY, "1.1")));
        return generateOID(prefix);
    };
}
