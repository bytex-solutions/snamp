package com.bytex.snamp.testing;

import org.snmp4j.smi.Variable;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface SnmpTable {

    /**
     * Gets number of rows in this table.
     * @return The number of rows in this table.
     */
    int getRowCount();

    /**
     * Gets number of columns in this table.
     * @return The number of columns in this table.
     */
    int getColumnCount();

    /**
     * Gets table cell.
     * @param columnIndex The column index.
     * @param rowIndex The row index.
     * @return The cell value.
     */
    Variable getRawCell(final int columnIndex, final int rowIndex);

    Object getCell(final int columndIndex, final int rowIndex);
}
