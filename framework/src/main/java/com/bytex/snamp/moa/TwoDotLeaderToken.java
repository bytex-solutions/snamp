package com.bytex.snamp.moa;

import com.bytex.snamp.parser.PunctuationToken;
import com.bytex.snamp.parser.SingleCharacterToken;

import javax.annotation.concurrent.Immutable;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Immutable
final class TwoDotLeaderToken extends PunctuationToken implements SingleCharacterToken {
    private static final long serialVersionUID = 1143234177279410559L;
    public static final char VALUE = '\u2025';
    public static final int TYPE = VALUE;
    public static final TwoDotLeaderToken INSTANCE = new TwoDotLeaderToken();

    private TwoDotLeaderToken() {
        super(TYPE, VALUE);
    }

    /**
     * Gets character wrapped by this token.
     *
     * @return Wrapped character.
     */
    @Override
    public char getValue() {
        return VALUE;
    }
}
