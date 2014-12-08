package com.itworks.snamp.testing;

import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.mapping.OrdinalRecordSet;
import com.itworks.snamp.mapping.RecordReader;
import com.itworks.snamp.mapping.RecordSet;
import com.itworks.snamp.mapping.RecordSetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class MatrixImpl<C> extends OrdinalRecordSet<Integer, RecordSet<Integer, C>> implements Matrix<C> {
    public static <C> MatrixImpl<C> create(final List<? extends Map<Integer, C>> rows){
        return new MatrixImpl<C>() {
            @Override
            protected Integer first() {
                return 0;
            }

            @Override
            protected Integer next(final Integer index) {
                return index < rows.size() - 1 ? index + 1 : null;
            }

            @Override
            protected RecordSet<Integer, C> getRecord(final Integer index) {
                return RecordSetUtils.fromMap(rows.get(index));
            }

            @Override
            public int size() {
                return rows.size();
            }
        };
    }

    public static <C> List<? extends Map<Integer, C>> toList(final Matrix<C> m){
        final List<Map<Integer, C>> result = new ArrayList<>(m.size());
        m.sequential().forEach(new RecordReader<Integer, RecordSet<Integer, C>, ExceptionPlaceholder>() {
            @Override
            public void read(final Integer index, final RecordSet<Integer, C> value) {
                result.add(RecordSetUtils.toMap(value));
            }
        });
        return result;
    }
}
