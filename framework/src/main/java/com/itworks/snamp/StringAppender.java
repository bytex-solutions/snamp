package com.itworks.snamp;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.Transformer;

import java.io.Flushable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Objects;

/**
 * Represents advanced version of {@link java.lang.StringBuilder} class.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class StringAppender implements Appendable, CharSequence, Serializable {
    private final StringBuilder builder;

    private StringAppender(final StringBuilder builder) {
        this.builder = Objects.requireNonNull(builder, "builder is null");
    }

    /**
     * Creates a new empty string appender.
     * @return A new empty string appender.
     */
    public static StringAppender init(){
        return new StringAppender(new StringBuilder());
    }

    /**
     * Creates a new empty string appender with predefined capacity.
     * @param capacity The initial capacity of the appender.
     * @return A new empty string appender.
     */
    public static StringAppender init(final int capacity){
        return new StringAppender(new StringBuilder(capacity));
    }

    /**
     * Drains the content of this appender to the destination stream
     * and flushes it immediately.
     * @param output The content acceptor.
     * @param <T> Type of the destination stream.
     * @throws IOException I/O error occurs.
     */
    public <T extends Appendable & Flushable> void flush(final T output) throws IOException{
        drainTo(output);
        output.flush();
    }

    /**
     * Drains the content of this appender to the destination stream.
     * @param output The content acceptor.
     * @throws IOException I/O error occurs.
     */
    public void drainTo(final Appendable output) throws IOException {
        output.append(builder);
    }

    /**
     * Appends a new line.
     * @return This appender.
     */
    public StringAppender newLine(){
        return append(System.lineSeparator());
    }

    /**
     * Appends a new string.
     * @param formatter The string template formatter. May be {@literal null}.
     * @param template The string template to format.
     * @param args The formatting arguments.
     * @return This appender.
     */
    public StringAppender append(final Formatter formatter,
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
    public StringAppender appendln(final Formatter formatter,
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
    public StringAppender appendln(final String format, final Object... args) {
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
    public StringAppender append(final CharSequence csq) {
        builder.append(csq);
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
    public StringAppender append(final CharSequence csq, final int start, final int end) {
        builder.append(csq, start, end);
        return this;
    }

    /**
     * Appends the specified character to this <tt>Appendable</tt>.
     *
     * @param c The character to append
     * @return A reference to this <tt>Appendable</tt>
     */
    @Override
    public StringAppender append(final char c) {
        builder.append(c);
        return this;
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

    public <I> StringAppender join(final Iterable<? extends I> elements,
                               final Transformer<? super I, CharSequence> iteration,
                               final CharSequence separator){
        return join(IteratorUtils.transformedIterator(elements.iterator(), iteration), separator);
    }

    public <I> StringAppender appendln(final Iterable<? extends I> elements,
                                       final Transformer<? super I, CharSequence> iteration) {
        return join(elements, iteration, System.lineSeparator());
    }

    public StringAppender appendln(final Iterable<?> elements) {
        return appendln(elements, toStringTransformer());
    }

    /**
     * Returns the length of this character sequence.  The length is the number
     * of 16-bit <code>char</code>s in the sequence.</p>
     *
     * @return the number of <code>char</code>s in this sequence
     */
    @Override
    public int length() {
        return builder.length();
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
    public char charAt(final int index) {
        return builder.charAt(index);
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
    public CharSequence subSequence(final int start, final int end) {
        return builder.subSequence(start, end);
    }

    /**
     * Builds the whole string from fragments saved in this appender.
     * @return The whole string concatenated from fragments saved in this appender.
     */
    @Override
    public String toString() {
        return builder.toString();
    }

    public static <I> Transformer<I, CharSequence> toStringTransformer() {
        return new Transformer<I, CharSequence>() {
            @Override
            public String transform(final I input) {
                return Objects.toString(input, "");
            }
        };
    }
}
