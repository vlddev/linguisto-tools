package org.linguisto.tools.imp.core.dbformat.db;

import javax.sql.DataSource;

import org.linguisto.tools.imp.core.base.Persistable;
import org.linguisto.tools.imp.core.base.Persistable;

/**
 */
public interface SQLHelper {

    enum Action {
        INSERT,
        UPDATE
    }

    void retrieveId(DataSource dataSource, Persistable persistable) throws Exception;
    String createInsertStatement(Persistable persistable) throws Exception;
    String createUpdateStatement(Persistable persistable) throws Exception;
    int getInsertColumnSize(Persistable persistable) throws Exception;
    int getUpdateColumnSize(Persistable persistable) throws Exception;
    Object getSQLValue(Action action, int column, Persistable persistable) throws Exception;
    int getSQLType(Action acton, int column, Persistable persistable) throws Exception;

}
