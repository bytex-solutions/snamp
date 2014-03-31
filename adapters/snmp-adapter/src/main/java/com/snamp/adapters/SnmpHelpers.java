package com.snamp.adapters;

import com.snamp.connectors.AttributeMetadata;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.*;
import org.snmp4j.smi.Variable;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.*;

/**
 * @author Roman Sakno
 */
final class SnmpHelpers {
    private SnmpHelpers(){

    }

    public static final Logger getLogger(){
        return Logger.getLogger("snamp.snmp.log");
    }

    /**
     * Represents date/time formatter.
     */
    public static interface DateTimeFormatter{
        byte[] convert(final Date value);
        Date convert(final byte[] value) throws ParseException;
    }

    /**
     * Provides date/time formatting using the custom pattern.
     * This class cannot be inherited.
     */
    private static class CustomDateTimeFormatter extends SimpleDateFormat implements DateTimeFormatter{
        private static final String DEFAUT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
        public static final String FORMATTER_NAME = "default";

        public CustomDateTimeFormatter(){
            this(DEFAUT_FORMAT);
        }

        public CustomDateTimeFormatter(final String pattern){
            super(pattern);
        }

        @Override
        public final byte[] convert(final Date value) {
            return format(value).getBytes();
        }

        private Date convert(final String value) throws ParseException{
            return parse(value);
        }

        @Override
        public final Date convert(final byte[] value) throws ParseException {
            return convert(new String(value));
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
                int second = value.get(Calendar.SECOND);
                dataStream.writeByte(second == 0 ? 60 : second);
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
                return new byte[0];
            }
        }

        @Override
        public byte[] convert(final Date value) {
            final Calendar cal = new GregorianCalendar();
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
                return null;
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

        public final void setYear(final int year) {
            this.year = year;
        }

        public final void setMonth(final int month) {
            this.month = month;
        }

        public final void setDayOfMonth(final int dayOfMonth) {
            this.dayOfMonth = dayOfMonth;
        }

        public final void setHourOfDay(final int hourOfDay) {
            this.hourOfDay = hourOfDay;
        }

        public final void setDeciseconds(final int deciseconds) {
            this.deciseconds = deciseconds;
        }

        public final void setMinute(final int minute) {
            this.minute = minute;
        }

        public final void setSecond(final int second) {
            this.second = second;
        }

        public final void setOffsetInHours(final int offsetInHours) {
            this.offsetInHours = offsetInHours;
        }

        public final void setOffsetInMinutes(final int offsetInMinutes) {
            this.offsetInMinutes = offsetInMinutes;
        }

        public final void setDirectionFromUTCPlus(final boolean directionFromUTCPlus) {
            this.directionFromUTCPlus = directionFromUTCPlus;
        }

        public final Calendar build(){
            final Calendar cal = Calendar.getInstance();
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
            pattern = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2}),([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])(?:\\.([0-9]{1})),(\\+)(\\d{1,2}):(\\d{1,2})?");
        }

        private static String addLeadingZeroes(final String value, final int requiredLength){
            if(value == null) return addLeadingZeroes("", requiredLength);
            else if(value.length() < requiredLength) return addLeadingZeroes("0" + value, requiredLength);
            else return value;
        }

        private static <T extends Comparable<T>> T replace(final T actual, final T check, final T replacement){
            return actual.compareTo(check) == 0 ? replacement : actual;
        }

        private static String convert(final Calendar value){
            final String RFC1903_FORMAT = "%s-%s-%s,%s:%s:%s.%s,%s%s:%s";
            //parse components of RFC1903
            final String year = Integer.toString(value.get(Calendar.YEAR));
            final String month = Integer.toString(value.get(Calendar.MONTH) + 1); //Month value is 0-based. e.g., 0 for January.
            final String dayOfMonth = Integer.toString(value.get(Calendar.DAY_OF_MONTH));
            final String hourOfDay = addLeadingZeroes(Integer.toString(value.get(Calendar.HOUR_OF_DAY)), 2);
            final String minute = addLeadingZeroes(Integer.toString(value.get(Calendar.MINUTE)), 2);
            final String second = addLeadingZeroes(Integer.toString(replace(value.get(Calendar.SECOND), 0, 60)), 2);
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
            final Calendar cal = new GregorianCalendar();
            cal.setTime(value);
            return convert(cal).getBytes();
        }

        private Date convert(final String value) throws ParseException{
            final Matcher matcher = pattern.matcher(value);
            if (matcher.matches())
            {
                final CalendarBuilder builder = new CalendarBuilder();
                builder.setYear(Integer.valueOf(matcher.group(1)));
                builder.setMonth(Integer.valueOf(matcher.group(2))-1);
                builder.setDayOfMonth(Integer.valueOf(matcher.group(3)));
                builder.setHourOfDay(Integer.valueOf(matcher.group(4)));
                builder.setMinute(Integer.valueOf(matcher.group(5)));
                builder.setSecond(Integer.valueOf(matcher.group(6)));
                builder.setDeciseconds(Integer.valueOf(matcher.group(7)));
                builder.setDirectionFromUTCPlus(matcher.group(8).equals("+"));
                builder.setOffsetInHours(Integer.valueOf(matcher.group(9)));
                builder.setOffsetInMinutes(Integer.valueOf(matcher.group(10)));
                return builder.build().getTime();
            }
            else
                throw new ParseException(String.format("Unable to parse value %s to rfc1903 format", value), 0);
        }

        @Override
        public Date convert(final byte[] value) throws ParseException {
            return convert(new String(value));
        }
    }

    public static DateTimeFormatter createDateTimeFormatter(final String formatterName){
        if(formatterName == null || formatterName.isEmpty()) return new Rfc1903BinaryDateTimeFormatter();
        else switch (formatterName){
            case CustomDateTimeFormatter.FORMATTER_NAME: return new CustomDateTimeFormatter();
            case Rfc1903BinaryDateTimeFormatter.FORMATTER_NAME: return new Rfc1903BinaryDateTimeFormatter();
            case Rfc1903HumanReadableDateTimeFormatter.FORMATTER_NAME: return new Rfc1903HumanReadableDateTimeFormatter();
            default: return new CustomDateTimeFormatter(formatterName);
        }
    }

    public static MOAccess getAccessRestrictions(final AttributeMetadata metadata, final boolean mayCreate){
        switch ((metadata.canWrite() ? 1 : 0) << 1 | (metadata.canRead() ? 1 : 0)){
            //case 0: case 1:
            default: return MOAccessImpl.ACCESS_READ_ONLY;
            case 2: return mayCreate ? new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_WRITE | MOAccessImpl.ACCESSIBLE_FOR_CREATE) : MOAccessImpl.ACCESS_WRITE_ONLY;
            case 3: return mayCreate ? new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE | MOAccessImpl.ACCESSIBLE_FOR_CREATE) :  MOAccessImpl.ACCESS_READ_WRITE;
        }
    }

    public static MOAccess getAccessRestrictions(final AttributeMetadata metadata){
        return getAccessRestrictions(metadata, false);
    }

    public static <COLUMN extends MOColumn<? extends Variable>> COLUMN findColumn(final MOTable<?, ? extends MOColumn<? extends Variable>, ?> table, final Class<COLUMN> columnType){
        for(final MOColumn<? extends Variable> column: table.getColumns())
            if(columnType.isInstance(column)) return columnType.cast(column);
        return null;
    }

    public static int findColumnIndex(final MOTable<?, ? extends MOColumn<? extends Variable>, ?> table, final Class<? extends MOColumn<? extends Variable>> columnType){
        final MOColumn<? extends Variable> column = findColumn(table, columnType);
        return column != null ? column.getColumnID() : -1;
    }
}
