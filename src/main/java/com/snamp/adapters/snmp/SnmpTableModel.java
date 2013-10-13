package com.snamp.adapters.snmp;

import org.snmp4j.agent.mo.*;
import org.snmp4j.smi.OID;

import java.util.Iterator;

/**
 * Represents SNMP table model.
 * @author roman
 */
final class SnmpTableModel implements MOTableModel {


    /**
     * Returns the number of columns currently in this table model.
     *
     * @return the number of columns.
     */
    @Override
    public int getColumnCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns the number of rows currently in this table model.
     *
     * @return the number of rows.
     */
    @Override
    public int getRowCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Checks whether this table model contains a row with the specified index.
     *
     * @param index the index OID of the row to search.
     * @return <code>true</code> if this model has a row of with index
     *         <code>index</code> or <code>false</code> otherwise.
     */
    @Override
    public boolean containsRow(final OID index) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Gets the row with the specified index.
     *
     * @param index the row index.
     * @return the <code>MOTableRow</code> with the specified index and
     *         <code>null</code> if no such row exists.
     */
    @Override
    public MOTableRow getRow(final OID index) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns an iterator over the rows in this table model.
     *
     * @return an <code>Iterator</code> returning <code>MOTableRow</code> instances.
     */
    @Override
    public Iterator iterator() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns an iterator on a view of the rows of this table model
     * whose index values are greater or equal <code>lowerBound</code>.
     *
     * @param lowerBound the lower bound index (inclusive). If <code>lowerBound</code> is
     *                   <code>null</code> the returned iterator is the same as returned by
     *                   {@link #iterator()}.
     * @return an <code>Iterator</code> over the
     */
    @Override
    public Iterator tailIterator(final OID lowerBound) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns the last row index in this model.
     *
     * @return the last index OID of this model.
     */
    @Override
    public OID lastIndex() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns the first row index in this model.
     *
     * @return the first index OID of this model.
     */
    @Override
    public OID firstIndex() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns the first row contained in this model.
     *
     * @return the <code>MOTableRow</code> with the smallest index or <code>null</code>
     *         if the model is empty.
     */
    @Override
    public MOTableRow firstRow() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns the last row contained in this model.
     *
     * @return the <code>MOTableRow</code> with the greatest index or <code>null</code>
     *         if the model is empty.
     */
    @Override
    public MOTableRow lastRow() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
