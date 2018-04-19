package org.linguisto.tools.imp.core.dbformat.db;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.linguisto.tools.imp.core.BatchConfiguration;
import org.linguisto.tools.imp.core.base.Persistable;
import org.linguisto.tools.imp.core.dbformat.db.annotation.*;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Table;
import org.linguisto.tools.imp.core.BatchConfiguration;
import org.linguisto.tools.imp.core.Configureable;
import org.linguisto.tools.imp.core.base.Persistable;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Column;
import org.linguisto.tools.imp.core.dbformat.db.annotation.ColumnType;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Id;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Update;

public final class SQLHelperImpl implements SQLHelper, Configureable {

    private Map<Class<?>, SQLMetaInfo> metaInfos = new HashMap<Class<?>, SQLMetaInfo>();
    private DBHelper dbHelper;
    private BatchConfiguration config;

    public SQLHelperImpl(DBHelper dbHelper, BatchConfiguration config) {
        this.dbHelper = dbHelper;
        this.config = config;
    }

    public String createInsertStatement(Persistable persistable) throws Exception {
        SQLMetaInfo metaInfo = metaInfos.get(persistable.getClass());
        if (metaInfo!=null) return metaInfo.insert;
        metaInfo = createSQLMetaInfo(persistable);
        return metaInfo.insert;
    }

    public String createUpdateStatement(Persistable persistable) throws Exception {
        SQLMetaInfo metaInfo = metaInfos.get(persistable.getClass());
        if (metaInfo!=null) return metaInfo.update;
        metaInfo = createSQLMetaInfo(persistable);
        return metaInfo.update;
    }

    public int getInsertColumnSize(Persistable persistable) throws Exception {
        SQLMetaInfo metaInfo = metaInfos.get(persistable.getClass());
        if (metaInfo!=null) return metaInfo.insertColumns.size();
        return createSQLMetaInfo(persistable).insertColumns.size();
    }

    public int getUpdateColumnSize(Persistable persistable) throws Exception {
        SQLMetaInfo metaInfo = metaInfos.get(persistable.getClass());
        if (metaInfo!=null) return metaInfo.updateColumns.size();
        return createSQLMetaInfo(persistable).updateColumns.size();
    }

    public int getSQLType(Action action, int column, Persistable persistable) throws Exception {
        SQLMetaInfo metaInfo = metaInfos.get(persistable.getClass());
        if (metaInfo==null) metaInfo = createSQLMetaInfo(persistable);

        String columnName;
        Column annoColumn;
        Class<?> theClass;

        switch(action) {
            case INSERT:
                columnName = metaInfo.insertColumns.get(column);
                annoColumn = metaInfo.insertColumnMethods.get(columnName).getAnnotation(Column.class);
                theClass = metaInfo.insertColumnMethods.get(columnName).getReturnType();
                break;
            case UPDATE:
                columnName = metaInfo.updateColumns.get(column);
                annoColumn = metaInfo.updateColumnMethods.get(columnName).getAnnotation(Column.class);
                theClass = metaInfo.updateColumnMethods.get(columnName).getReturnType();
                break;
            default:
                // May not happen
                throw new RuntimeException();
        }

        if (annoColumn.columnType().equals(ColumnType.AUTOMATIC)) {
            if (theClass.equals(String.class)) {
                return Types.VARCHAR;
            } else if (theClass.equals(Integer.TYPE) || theClass.equals(Integer.class)) {
                return Types.INTEGER;
            } else if (theClass.equals(Long.TYPE) || theClass.equals(Long.class)) {
                return Types.BIGINT;
            } else if (theClass.equals(Date.class)) {
                return Types.DATE;
            }
        } else {
            if (annoColumn.columnType().equals(ColumnType.DATE)) {
                return Types.DATE;
            } else if (annoColumn.columnType().equals(ColumnType.TIME)) {
                return Types.TIME;
            } else if (annoColumn.columnType().equals(ColumnType.TIMESTAMP)) {
                return Types.TIMESTAMP;
            } else if (annoColumn.columnType().equals(ColumnType.BIGINT)) {
                return Types.BIGINT;
            } else if (annoColumn.columnType().equals(ColumnType.INTEGER)) {
                return Types.INTEGER;
            } else if (annoColumn.columnType().equals(ColumnType.VARCHAR)) {
                return Types.VARCHAR;
            }
        }
        Object value = getSQLValue(action, column, persistable);
        if (value == null) return Types.NULL;
        throw new Exception("No SQL Type for column "+column+" found!");
    }

    public Object getSQLValue(Action action, int column, Persistable persistable) throws Exception {
        SQLMetaInfo metaInfo = metaInfos.get(persistable.getClass());
        if (metaInfo==null) metaInfo = createSQLMetaInfo(persistable);

        Method method;

        switch(action) {
            case INSERT:
                method = metaInfo.insertColumnMethods.get(metaInfo.insertColumns.get(column));
                break;
            case UPDATE:
                method = metaInfo.updateColumnMethods.get(metaInfo.updateColumns.get(column));
                break;
            default:
                // may not happen
                throw new RuntimeException();
        }

        Object value = method.invoke(persistable);
        if (value instanceof Date) {
            if (getSQLType(action, column, persistable) == Types.DATE) {
                return new java.sql.Date(((Date)value).getTime());
            }
            if (getSQLType(action, column, persistable) == Types.TIME) {
                return new java.sql.Time(((Date)value).getTime());
            }
            if (getSQLType(action, column, persistable) == Types.TIMESTAMP) {
                return new Timestamp(((Date)value).getTime());
            }
        }
        return value;
    }

    public void retrieveId(DataSource dataSource, Persistable persistable) throws Exception {
        Table table = persistable.getClass().getAnnotation(Table.class);
        long id = dbHelper.getNextValue(dataSource, table.seq());
        persistable.setId((int)id);
    }

    private SQLMetaInfo createSQLMetaInfo(Persistable persistable) throws Exception {
        Class<?> theClass = persistable.getClass();
        StringBuffer insertStatement = new StringBuffer("INSERT INTO ");
        Table table = theClass.getAnnotation(Table.class);
        if (table != null) {
            SQLMetaInfo newMetaInfo = new SQLMetaInfo();

            if (!config.get(TABLE_PREFIX).trim().equals(""))
            	insertStatement.append(config.get(TABLE_PREFIX));
            insertStatement.append(table.name());
            if (!config.get(TABLE_POSTFIX).trim().equals(""))
            	insertStatement.append(config.get(TABLE_POSTFIX));

            insertStatement.append(" (");
            Method[] methods = theClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                Column column = method.getAnnotation(Column.class);
                if (column != null) {
                    newMetaInfo.insertColumns.add(column.name());
                    newMetaInfo.insertColumnMethods.put(column.name(), method);
                }
            }
            Collections.sort(newMetaInfo.insertColumns);
            StringBuffer keys = new StringBuffer();
            StringBuffer values = new StringBuffer();
            for (int i = 0; i < newMetaInfo.insertColumns.size(); i++) {
                String s = newMetaInfo.insertColumns.get(i);
                keys.append(s);
                values.append("?");
                if (i<newMetaInfo.insertColumns.size()-1) {
                    keys.append(",");
                    values.append(",");
                }
            }
            insertStatement.append(keys).append(") VALUES (").append(values).append(")");
            newMetaInfo.insert = insertStatement.toString();

            StringBuffer updateStatement = new StringBuffer("UPDATE ");

            if (!config.get(TABLE_PREFIX).trim().equals(""))
            	updateStatement.append(config.get(TABLE_PREFIX));
            updateStatement.append(table.name());
            if (!config.get(TABLE_POSTFIX).trim().equals(""))
            	updateStatement.append(config.get(TABLE_POSTFIX));

            updateStatement.append(" set ");

            String id = null;
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getAnnotation(Update.class)!= null) {
                    String column = method.getAnnotation(Column.class).name();
                    newMetaInfo.updateColumns.add(column);
                    newMetaInfo.updateColumnMethods.put(column, method);
                }
                if (method.getAnnotation(Id.class) != null) {
                    id = method.getAnnotation(Column.class).name();
                }
            }
            Collections.sort(newMetaInfo.updateColumns);
            for (int i = 0; i < newMetaInfo.updateColumns.size(); i++) {
                String s = newMetaInfo.updateColumns.get(i);
                updateStatement.append(s).append(" = ?");
                if (i<newMetaInfo.updateColumns.size()-1) {
                    updateStatement.append(",");
                }
            }
            updateStatement.append(" where ").append(id).append(" = ?");
            newMetaInfo.update = updateStatement.toString();

            metaInfos.put(persistable.getClass(), newMetaInfo);
            return newMetaInfo;
        } else {
            throw new Exception("Object "+persistable+" was not entity");
        }
    }

    private class SQLMetaInfo {
        String insert = null;
        String update = null;
        List<String> insertColumns = new ArrayList<String>();
        Map<String, Method> insertColumnMethods = new HashMap<String, Method>();
        List<String> updateColumns = new ArrayList<String>();
        Map<String, Method> updateColumnMethods = new HashMap<String, Method>();
    }
}