package org.linguisto.tools.imp.core.dbformat.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.linguisto.tools.imp.core.BatchConfiguration;
import org.linguisto.tools.imp.core.BatchConfiguration;
import org.linguisto.tools.imp.core.Configureable;

public final class DBHelperImpl implements DBHelper, Configureable {
	private BatchConfiguration config;

	public DBHelperImpl(BatchConfiguration config) {
		this.config = config;
	}

    public long getNextValue(DataSource datasource, String sequence) throws Exception {
        Connection con = null;
        PreparedStatement prep = null;
        ResultSet result = null;
        try {
            con = datasource.getConnection();
            StringBuffer sql = new StringBuffer("select ");

//            if (!config.get(SEQUENCE_PREFIX).trim().equals(""))
//            	sql.append(config.get(SEQUENCE_PREFIX));

            sql.append(sequence);

//            if (!config.get(SEQUENCE_POSTFIX).trim().equals(""))
//            	sql.append(config.get(SEQUENCE_POSTFIX));

            sql.append(" from dual");
            prep = con.prepareStatement(sql.toString());
            result = prep.executeQuery();
            if (result.next()) {
                return result.getLong(1);
            }
        } catch (Exception e) {
            throw new Exception("Could not query sequence "+sequence, e);
        } finally {
            closeResultSet(result);
            closePreparedStatement(prep);
            closeConnection(con);
        }
        throw new Exception("Could not query sequence "+sequence);
    }

    public void closeConnection(Connection con) {
        try {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException e) {
                    // do nothing
                }
                con.close();
            }
        } catch (SQLException e) {
            // do nothing
        }
    }

    public void closePreparedStatement(PreparedStatement prep) {
        try {
            if (prep != null) {
                prep.close();
            }
        } catch (SQLException e) {
            // do nothing
        }
    }

    public void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                // do nothing
            }
        }
    }
}