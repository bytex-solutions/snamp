package com.itworks.snamp;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Iterators;
import com.itworks.snamp.internal.annotations.MethodStub;

import java.io.CharArrayWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Iterator;

/**
 * Represents advanced version of {@link java.lang.StringBuilder} class.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class StringAppender extends CharArrayWriter implements Appendable, CharSequence, Serializable {
    private static final long serialVersionUID = 5383532773187026410L;
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];

    public StringAppender(){
        this(32);
    }

    public StringAppender(final int capacity){
        super(capacity);
    }

    /**
     * Drains the content of this appender to the destination stream
     * and flushes it immediately.
     * @param output The content acceptor.
     * @param <T> Type of the destination stream.
     * @throws IOException I/O error occurs.
     */
    public final <T extends Appendable & Flushable> void flush(final T output) throws IOException{
        drainTo(output);
        output.flush();
    }

    /**
     * Drains the content of this appender to the destination stream.
     * @param output The content acceptor.
     * @throws IOException I/O error occurs.
     */
    public final void drainTo(final Appendable output) throws IOException {
        output.append(new String(buf, 0, count));
    }

    /**
     * Appends a new line.
     * @return This appender.
     */
    public final StringAppender newLine(){
        return append(System.lineSeparator());
    }

    /**
     * Appends a new string.
     * @param formatter The string template formatter. May be {@literal null}.
     * @param template The string template to format.
     * @param args The formatting arguments.
     * @return This appender.
     */
    public final StringAppender append(final Formatter formatter,
                                 final String template,
                                 final Object... args) {
        return formatter == null ?
                append(new Formatter(), template, args) :
                append(formatter.format(template, args).toString());
    }

    /**
     * Appends a new line.
     * @param formatter The string template formatter. May be {@literal null}.
     * @param template The string template to format.
     * @param args The formatting arguments.
     * @return This appender.
     */
    public final StringAppender appendln(final Formatter formatter,
                                   final String template,
                                   final Object... args){
        return append(formatter, template, args).newLine();
    }

    /**
     * Appends a new line.
     * @param format The string template to format.
     * @param args The formatting arguments.
     * @return This appender.
     */
    public final StringAppender appendln(final String format, final Object... args) {
        return appendln(null, format, args);
    }

    /**
     * Appends the specified character sequence to this <tt>Appendable</tt>.
     * <p/>
     * <p> Depending on which class implements the character sequence
     * <tt>csq</tt>, the entire sequence may not be appended.  For
     * instance, if <tt>csq</tt> is a {@link java.nio.CharBuffer} then
     * the subsequence to append is defined by the buffer's position and limit.
     *
     * @param csq The character sequence to append.  If <tt>csq</tt> is
     *            <tt>null</tt>, then the four characters <tt>"null"</tt> are
     *            appended to this Appendable.
     * @return A reference to this <tt>Appendable</tt>
     */
    @Override
    public final StringAppender append(final CharSequence csq) {
        super.append(csq);
        return this;
    }

    /**
     * Appends a subsequence of the specified character sequence to this
     * <tt>Appendable</tt>.
     * <p/>
     * <p> An invocation of this method of the form <tt>out.append(csq, start,
     * end)</tt> when <tt>csq</tt> is not <tt>null</tt>, behaves in
     * exactly the same way as the invocation
     * <p/>
     * <pre>
     *     out.append(csq.subSequence(start, end)) </pre>
     *
     * @param csq   The character sequence from which a subsequence will be
     *              appended.  If <tt>csq</tt> is <tt>null</tt>, then characters
     *              will be appended as if <tt>csq</tt> contained the four
     *              characters <tt>"null"</tt>.
     * @param start The index of the first character in the subsequence
     * @param end   The index of the character following the last character in the
     *              subsequence
     * @return A reference to this <tt>Appendable</tt>
     * @throws IndexOutOfBoundsException If <tt>start</tt> or <tt>end</tt> are negative, <tt>start</tt>
     *                                   is greater than <tt>end</tt>, or <tt>end</tt> is greater than
     *                                   <tt>csq.length()</tt>
     */
    @Override
    public final StringAppender append(final CharSequence csq, final int start, final int end) {
        super.append(csq, start, end);
        return this;
    }

    /**
     * Appends the specified character to this <tt>Appendable</tt>.
     *
     * @param c The character to append
     * @return A reference to this <tt>Appendable</tt>
     */
    @Override
    public final StringAppender append(final char c) {
        write(c);
        return this;
    }

    public final StringAppender append(final long value){
        return append(Long.toString(value));
    }

    public final StringAppender append(final byte b){
        return append((long)b);
    }

    public final StringAppender append(final short s){
        return append((long)s);
    }

    public final StringAppender append(final int i){
        return append((long)i);
    }

    public final StringAppender append(final Number value, final DecimalFormat format){
        return append(format.format(value));
    }

    private StringAppender join(final Iterator<? extends CharSequence> elements,
                                final CharSequence separator) {
        if (elements.hasNext()) {
            final CharSequence first = elements.next();
            append(first);
            while (elements.hasNext()) {
                append(separator);
                append(elements.next());
            }
        }
        return this;
    }

    public final <I> StringAppender join(final Iterable<I> elements,
                                   final Function<? super I, ? extends CharSequence> iteration,
                                   final CharSequence separator) {
        return join(Iterators.transform(elements.iterator(), iteration), separator);
    }

    public final <I> StringAppender appendln(final Iterable<? extends I> elements,
                                       final Function<? super I, ? extends CharSequence> iteration) {
        return join(elements, iteration, System.lineSeparator());
    }

    public final StringAppender appendln(final Iterable<?> elements) {
        return appendln(elements, Functions.toStringFunction());
    }

    /**
     * Returns the length of this character sequence.  The length is the number
     * of 16-bit <code>char</code>s in the sequence.</p>
     *
     * @return the number of <code>char</code>s in this sequence
     */
    @Override
    public final int length() {
        return count;
    }

    /**
     * Returns the <code>char</code> value at the specified index.  An index ranges from zero
     * to <tt>length() - 1</tt>.  The first <code>char</code> value of the sequence is at
     * index zero, the next at index one, and so on, as for array
     * indexing. </p>
     * <p/>
     * <p>If the <code>char</code> value specified by the index is a
     * <a href="{@docRoot}/java/lang/Character.html#unicode">surrogate</a>, the surrogate
     * value is returned.
     *
     * @param index the index of the <code>char</code> value to be returned
     * @return the specified <code>char</code> value
     * @throws IndexOutOfBoundsException if the <tt>index</tt> argument is negative or not less than
     *                                   <tt>length()</tt>
     */
    @Override
    public final char charAt(final int index) {
        if(index < 0 || index >= count)
            throw new StringIndexOutOfBoundsException();
        else return buf[index];
    }

    /**
     * Returns a new <code>CharSequence</code> that is a subsequence of this sequence.
     * The subsequence starts with the <code>char</code> value at the specified index and
     * ends with the <code>char</code> value at index <tt>end - 1</tt>.  The length
     * (in <code>char</code>s) of the
     * returned sequence is <tt>end - start</tt>, so if <tt>start == end</tt>
     * then an empty sequence is returned. </p>
     *
     * @param start the start index, inclusive
     * @param end   the end index, exclusive
     * @return the specified subsequence
     * @throws IndexOutOfBoundsException if <tt>start</tt> or <tt>end</tt> are negative,
     *                                   if <tt>end</tt> is greater than <tt>length()</tt>,
     *                                   or if <tt>start</tt> is greater than <tt>end</tt>
     */
    @Override
    public final String subSequence(final int start, final int end) {
        final int newLength = end - start;
        if(newLength < 0) throw new IndexOutOfBoundsException();
        else if(newLength == 0) return "";
        else {
            final char[] newBuffer = new char[newLength];
            System.arraycopy(buf, start, newBuffer, 0, newLength);
            return new String(newBuffer);
        }
    }

    /**
     * Nothing to do, but you can override this behavior.
     */
    @Override
    @MethodStub
    public void flush() {

    }

    /**
     * Destroys underlying buffer.
     */
    @Override
    public void close() {
        buf = EMPTY_CHAR_ARRAY;
        count = 0;
    }
}
