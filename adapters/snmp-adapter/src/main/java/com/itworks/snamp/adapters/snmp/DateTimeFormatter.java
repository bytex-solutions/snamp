package com.itworks.snamp.adapters.snmp;

import java.text.ParseException;
import java.util.Date;

/**
 * Represents date/time formatter.
 */
interface DateTimeFormatter {
    byte[] convert(final Date value);
    Date convert(final byte[] value) throws ParseException;
}
