package org.linguisto.tools.imp.core.dbformat.db;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;

/**
 */
public interface DBHelper {
    void closePreparedStatement(PreparedStatement prep);
    void closeResultSet(ResultSet resultSet);
    void closeConnection(Connection con);
    long getNextValue(DataSource dataSource, String sequence) throws Exception;
}
