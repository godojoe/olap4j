/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package org.olap4j.driver.xmla;

import org.olap4j.OlapWrapper;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import javax.sql.rowset.RowSetMetaDataImpl;

/**
 * Implementation of {@link ResultSet} which returns 0 rows.
 *
 * <p>This class is used to implement {@link java.sql.DatabaseMetaData}
 * methods for querying object types where those object types never have
 * any instances for this particular driver.</p>
 *
 * <p>This class has sub-classes which implement JDBC 3.0 and JDBC 4.0 APIs;
 * it is instantiated using {@link Factory#newEmptyResultSet}.</p>
 *
 * @author jhyde
 * @since May 24, 2007
 */
abstract class EmptyResultSet implements ResultSet, OlapWrapper {
    final XmlaOlap4jConnection olap4jConnection;
    private final List<String> headerList;
    private final List<List<Object>> rowList;
    private int rowOrdinal = -1;
    private final RowSetMetaDataImpl metaData = new RowSetMetaDataImpl();

    /**
     * Creates an EmptyResultSet.
     *
     * @param olap4jConnection Connection
     * @param headerList Column names
     * @param rowList List of row values
     */
    EmptyResultSet(
        XmlaOlap4jConnection olap4jConnection,
        List<String> headerList,
        List<List<Object>> rowList)
    {
        this.olap4jConnection = olap4jConnection;
        this.headerList = headerList;
        this.rowList = rowList;
        try {
            metaData.setColumnCount(headerList.size());
            for (int i = 0; i < headerList.size(); i++) {
                metaData.setColumnName(i + 1, headerList.get(i));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the value of a given column
     * @param columnOrdinal 0-based ordinal
     * @return Value
     */
    private Object getColumn(int columnOrdinal) {
        return rowList.get(rowOrdinal).get(columnOrdinal);
    }

    private Object getColumn(String columnLabel) throws SQLException {
        int column = headerList.indexOf(columnLabel);
        if (column < 0) {
            throw new SQLException("Column not found: " + columnLabel);
        }
        return rowList.get(rowOrdinal).get(column);
    }

    // implement ResultSet

    public boolean next() throws SQLException {
        // note that if rowOrdinal == rowList.size - 1, we move but then return
        // false
        if (rowOrdinal < rowList.size()) {
            ++rowOrdinal;
        }
        return rowOrdinal < rowList.size();
    }

    public void close() throws SQLException {
    }

    public boolean wasNull() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public String getString(int columnIndex) throws SQLException {
        Object o = getColumn(columnIndex - 1);
        return o == null ? null : o.toString();
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        Object o = getColumn(columnIndex - 1);
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof String) {
            return Boolean.valueOf((String) o);
        } else {
            return !o.equals(0);
        }
    }

    private Number convertToNumber(Object o) {
        if (o instanceof Number) {
            return (Number)o;
        } else {
            return new BigDecimal(o.toString());
        }
    }

    public byte getByte(int columnIndex) throws SQLException {
        Object o = getColumn(columnIndex - 1);
        if (o == null) {
            return 0;
        }
        return convertToNumber(o).byteValue();
    }

    public short getShort(int columnIndex) throws SQLException {
        Object o = getColumn(columnIndex - 1);
        if (o == null) {
            return 0;
        }
        return convertToNumber(o).shortValue();
    }

    public int getInt(int columnIndex) throws SQLException {
        Object o = getColumn(columnIndex - 1);
        if (o == null) {
            return 0;
        }
        return convertToNumber(o).intValue();
    }

    public long getLong(int columnIndex) throws SQLException {
        Object o = getColumn(columnIndex - 1);
        if (o == null) {
            return 0;
        }
        return ((Number) o).longValue();
    }

    public float getFloat(int columnIndex) throws SQLException {
        Object o = getColumn(columnIndex - 1);
        if (o == null) {
            return 0;
        }
        return convertToNumber(o).floatValue();
    }

    public double getDouble(int columnIndex) throws SQLException {
        Object o = getColumn(columnIndex - 1);
        if (o == null) {
            return 0;
        }
        return convertToNumber(o).doubleValue();
    }

    public BigDecimal getBigDecimal(
        int columnIndex,
        int scale)
        throws SQLException
    {
        Object o = getColumn(columnIndex - 1);
        if (o == null) {
            return null;
        }
        BigDecimal bd;
        if (o instanceof BigDecimal) {
            bd = (BigDecimal)o;
        } else {
            bd = new BigDecimal(o.toString());
        }
        if (bd.scale() != scale) {
            bd = bd.setScale(scale);
        }
        return bd;
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        Object o = getColumn(columnIndex - 1);
        return (byte[]) o;
    }

    public Date getDate(int columnIndex) throws SQLException {
        Object o = getColumn(columnIndex - 1);
        return (Date) o;
    }

    public Time getTime(int columnIndex) throws SQLException {
        Object o = getColumn(columnIndex - 1);
        return (Time) o;
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        Object o = getColumn(columnIndex - 1);
        return (Timestamp) o;
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public String getString(String columnLabel) throws SQLException {
        Object o = getColumn(columnLabel);
        return o == null ? null : o.toString();
    }

    public boolean getBoolean(String columnLabel) throws SQLException {
        Object o = getColumn(columnLabel);
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof String) {
            return Boolean.valueOf((String) o);
        } else {
            return !o.equals(0);
        }
    }

    public byte getByte(String columnLabel) throws SQLException {
        Object o = getColumn(columnLabel);
        if (o == null) {
            return 0;
        }
        return convertToNumber(o).byteValue();
    }

    public short getShort(String columnLabel) throws SQLException {
        Object o = getColumn(columnLabel);
        if (o == null) {
            return 0;
        }
        return convertToNumber(o).shortValue();
    }

    public int getInt(String columnLabel) throws SQLException {
        Object o = getColumn(columnLabel);
        if (o == null) {
            return 0;
        }
        return convertToNumber(o).intValue();
    }

    public long getLong(String columnLabel) throws SQLException {
        Object o = getColumn(columnLabel);
        if (o == null) {
            return 0;
        }
        return convertToNumber(o).longValue();
    }

    public float getFloat(String columnLabel) throws SQLException {
        Object o = getColumn(columnLabel);
        if (o == null) {
            return 0;
        }
        return convertToNumber(o).floatValue();
    }

    public double getDouble(String columnLabel) throws SQLException {
        Object o = getColumn(columnLabel);
        if (o == null) {
            return 0;
        }
        return convertToNumber(o).doubleValue();
    }

    public BigDecimal getBigDecimal(
        String columnLabel,
        int scale)
        throws SQLException
    {
        Object o = getColumn(columnLabel);
        if (o == null) {
            return null;
        }
        BigDecimal bd;
        if (o instanceof BigDecimal) {
            bd = (BigDecimal)o;
        } else {
            bd = new BigDecimal(o.toString());
        }
        if (bd.scale() != scale) {
           bd = bd.setScale(scale);
        }
        return bd;
    }

    public byte[] getBytes(String columnLabel) throws SQLException {
        Object o = getColumn(columnLabel);
        return (byte[]) o;
    }

    public Date getDate(String columnLabel) throws SQLException {
        Object o = getColumn(columnLabel);
        return (Date) o;
    }

    public Time getTime(String columnLabel) throws SQLException {
        Object o = getColumn(columnLabel);
        return (Time) o;
    }

    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        Object o = getColumn(columnLabel);
        return (Timestamp) o;
    }

    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public InputStream getUnicodeStream(String columnLabel)
        throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public String getCursorName() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return metaData;
    }

    public Object getObject(int columnIndex) throws SQLException {
        return getColumn(columnIndex - 1);
    }

    public Object getObject(String columnLabel) throws SQLException {
        return getColumn(columnLabel);
    }

    public int findColumn(String columnLabel) throws SQLException {
        int column = headerList.indexOf(columnLabel);
        if (column < 0) {
            throw new SQLException("Column not found: " + columnLabel);
        }
        return column;
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Reader getCharacterStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        Object o = getColumn(columnIndex - 1);
        if (o == null) {
            return null;
        }
        BigDecimal bd;
        if (o instanceof BigDecimal) {
            bd = (BigDecimal)o;
        } else {
            bd = new BigDecimal(o.toString());
        }
        return bd;
    }

    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        Object o = getColumn(columnLabel);
        if (o == null) {
            return null;
        }
        BigDecimal bd;
        if (o instanceof BigDecimal) {
            bd = (BigDecimal)o;
        } else {
            bd = new BigDecimal(o.toString());
        }
        return bd;
    }

    public boolean isBeforeFirst() throws SQLException {
        return rowOrdinal < 0;
    }

    public boolean isAfterLast() throws SQLException {
        return rowOrdinal >= rowList.size();
    }

    public boolean isFirst() throws SQLException {
        return rowOrdinal == 0;
    }

    public boolean isLast() throws SQLException {
        return rowOrdinal == rowList.size() - 1;
    }

    public void beforeFirst() throws SQLException {
        rowOrdinal = -1;
    }

    public void afterLast() throws SQLException {
        rowOrdinal = rowList.size();
    }

    public boolean first() throws SQLException {
        if (rowList.size() == 0) {
            return false;
        } else {
            rowOrdinal = 0;
            return true;
        }
    }

    public boolean last() throws SQLException {
        if (rowList.size() == 0) {
            return false;
        } else {
            rowOrdinal = rowList.size() - 1;
            return true;
        }
    }

    public int getRow() throws SQLException {
        return rowOrdinal + 1; // 1-based
    }

    public boolean absolute(int row) throws SQLException {
        int newRowOrdinal = row - 1;// convert to 0-based
        if (newRowOrdinal >= 0 && newRowOrdinal < rowList.size()) {
            rowOrdinal = newRowOrdinal;
            return true;
        } else {
            return false;
        }
    }

    public boolean relative(int rows) throws SQLException {
        int newRowOrdinal = rowOrdinal + (rows - 1);
        if (newRowOrdinal >= 0 && newRowOrdinal < rowList.size()) {
            rowOrdinal = newRowOrdinal;
            return true;
        } else {
            return false;
        }
    }

    public boolean previous() throws SQLException {
        // converse of next(); note that if rowOrdinal == 0, we decrement
        // but return false
        if (rowOrdinal >= 0) {
            --rowOrdinal;
        }
        return rowOrdinal >= 0;
    }

    public void setFetchDirection(int direction) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int getFetchDirection() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setFetchSize(int rows) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int getFetchSize() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int getType() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int getConcurrency() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean rowUpdated() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean rowInserted() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean rowDeleted() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNull(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBigDecimal(
        int columnIndex, BigDecimal x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public void updateString(int columnIndex, String x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBytes(int columnIndex, byte x[]) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateTimestamp(
        int columnIndex, Timestamp x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public void updateAsciiStream(
        int columnIndex, InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public void updateBinaryStream(
        int columnIndex, InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public void updateCharacterStream(
        int columnIndex, Reader x, int length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public void updateObject(
        int columnIndex, Object x, int scaleOrLength) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNull(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBoolean(
        String columnLabel, boolean x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public void updateByte(String columnLabel, byte x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateShort(String columnLabel, short x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateInt(String columnLabel, int x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateLong(String columnLabel, long x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateFloat(String columnLabel, float x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateDouble(String columnLabel, double x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBigDecimal(
        String columnLabel, BigDecimal x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public void updateString(String columnLabel, String x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBytes(String columnLabel, byte x[]) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateDate(String columnLabel, Date x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateTime(String columnLabel, Time x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateTimestamp(
        String columnLabel, Timestamp x) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public void updateAsciiStream(
        String columnLabel, InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public void updateBinaryStream(
        String columnLabel, InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public void updateCharacterStream(
        String columnLabel, Reader reader, int length) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public void updateObject(
        String columnLabel, Object x, int scaleOrLength) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public void updateObject(String columnLabel, Object x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void insertRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void deleteRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void refreshRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void cancelRowUpdates() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void moveToInsertRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void moveToCurrentRow() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Statement getStatement() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Object getObject(
        int columnIndex, Map<String, Class<?>> map) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public Ref getRef(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Blob getBlob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Clob getClob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Array getArray(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Object getObject(
        String columnLabel, Map<String, Class<?>> map) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public Ref getRef(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Blob getBlob(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Clob getClob(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Array getArray(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Timestamp getTimestamp(
        int columnIndex, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public Timestamp getTimestamp(
        String columnLabel, Calendar cal) throws SQLException
    {
        throw new UnsupportedOperationException();
    }

    public URL getURL(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public URL getURL(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateRef(String columnLabel, Ref x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateClob(String columnLabel, Clob x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateArray(String columnLabel, Array x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    // implement Wrapper

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw olap4jConnection.helper.createException("cannot cast");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }

    public RowId getRowId(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public RowId getRowId(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int getHoldability() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean isClosed() throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNString(String columnLabel, String nString) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public NClob getNClob(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public NClob getNClob(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public String getNString(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public String getNString(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw new UnsupportedOperationException();
    }
}

// End EmptyResultSet.java
