package dev.yekllurt.mutesystem.core.database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class SQLResult {

    private final List<Map<String, Object>> result = new ArrayList<>();
    private int row;

    public SQLResult(ResultSet resultSet) {
        copyResultSet(resultSet);
    }

    private void copyResultSet(ResultSet resultSet) {
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            this.result.clear();
            while (resultSet.next()) {
                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                    rowData.put(resultSetMetaData.getColumnLabel(i), resultSet.getObject(i));
                }
                this.result.add(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isEmpty() {
        return this.result.isEmpty();
    }

    public boolean hasNext() {
        return this.row + 1 < this.result.size();
    }

    public boolean next() {
        if (hasNext()) {
            this.row++;
            return true;
        } else {
            return false;
        }
    }

    public int rowCount() {
        return this.result.size();
    }

    public int columnCount() {
        return this.result.get(row).size();
    }

    public Object getObject(String column) {
        return this.result.get(this.row).get(column);
    }

    public Object getObject(int column) {
        int index = 0;
        for (Map.Entry<String, Object> entry : this.result.get(this.row).entrySet()) {
            if (index == column) {
                return entry.getValue();
            }
            index++;
        }
        throw new NullPointerException("The column '" + column + "' does not exist");
    }

    public byte getByte(String column) {
        return (byte) getObject(column);
    }

    public byte getByte(int column) {
        return (byte) getObject(column);
    }

    public short getShort(String column) {
        return (short) getObject(column);
    }

    public short getShort(int column) {
        return (short) getObject(column);
    }

    public int getInt(String column) {
        return (int) getObject(column);
    }

    public int getInt(int column) {
        return (int) getObject(column);
    }

    public long getLong(String column) {
        return (long) getObject(column);
    }

    public long getLong(int column) {
        return (long) getObject(column);
    }

    public float getFloat(String column) {
        return (float) getObject(column);
    }

    public float getFloat(int column) {
        return (float) getObject(column);
    }

    public double getDouble(String column) {
        return (double) getObject(column);
    }

    public double getDouble(int column) {
        return (double) getObject(column);
    }

    public boolean getBoolean(String column) {
        return (boolean) getObject(column);
    }

    public boolean getBoolean(int column) {
        return (boolean) getObject(column);
    }

    public char getChar(String column) {
        return (char) getObject(column);
    }

    public char getChar(int column) {
        return (char) getObject(column);
    }

    public String getString(String column) {
        return (String) getObject(column);
    }

    public String getString(int column) {
        return (String) getObject(column);
    }

    public Date getDate(String column) {
        return (Date) getObject(column);
    }

    public Date getDate(int column) {
        return (Date) getObject(column);
    }

}
