package org.linguisto.tools.imp.xml;

import org.linguisto.tools.db.DBUtil;
import org.linguisto.tools.db.DbObjectRef;
import org.linguisto.tools.obj.Inf;
import org.linguisto.tools.obj.Translation;
import org.linguisto.tools.db.DBUtil;
import org.linguisto.tools.db.DbObjectRef;
import org.linguisto.tools.obj.Inf;
import org.linguisto.tools.obj.Translation;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLDBImporter {

	public static final Logger log = Logger.getLogger(SQLDBImporter.class.getName());
	
	private Connection con;

    private PreparedStatement psInsertInf = null;
    private PreparedStatement psFindInfByInfAndType = null;

    private PreparedStatement psFindTranslation = null;
    private PreparedStatement psInsertTranslation = null;
    private PreparedStatement psUpdateTranslation = null;
    private PreparedStatement psDeleteTranslation = null;


	private PreparedStatement psDeleteDeWordProperties = null;
	private PreparedStatement psInsertDeWordProperty = null;
	
    private String langFrom;
    private String langTo;

    private Map<String, Long> statisticMap = new HashMap<String, Long>();

    public SQLDBImporter(Connection con) throws SQLException {
		this.con = con;
	}

    public String getLangFrom() {
        return langFrom;
    }

    public void setLangFrom(String langFrom) {
        this.langFrom = langFrom;
    }

    public String getLangTo() {
        return langTo;
    }

    public void setLangTo(String langTo) {
        this.langTo = langTo;
    }

    public Map<String, Long> getStatisticMap() {
        return statisticMap;
    }

    private void addStats(String key) {
        Long cnt = statisticMap.get(key);
        if (cnt != null) {
            cnt++;
        } else {
            cnt = 1L;
        }
        statisticMap.put(key, cnt);
    }

    private void addStats(String key, int count) {
        Long cnt = statisticMap.get(key);
        if (cnt != null) {
            cnt += count;
        } else {
            cnt = (long)count;
        }
        statisticMap.put(key, cnt);
    }

    public void rollback() {
		DBUtil.rollback(con);
	}

	public void commit() {
		try {
			con.commit();
		} catch (SQLException e) {
            log.log(Level.SEVERE, "DaoDB: Exception commiting transaction: " + e.getMessage(), e);
		}
	}
	
	public void closeAll() {
        DBUtil.closeStatement(psInsertInf);
        DBUtil.closeStatement(psFindInfByInfAndType);

        DBUtil.closeStatement(psFindTranslation);
        DBUtil.closeStatement(psInsertTranslation);
        DBUtil.closeStatement(psUpdateTranslation);
        DBUtil.closeStatement(psDeleteTranslation);


        DBUtil.closeStatement(psDeleteDeWordProperties);
		DBUtil.closeStatement(psInsertDeWordProperty);

		DBUtil.closeConnection(con);
	}
	
	public void init() throws SQLException {
        if (con == null) throw new IllegalStateException("SQLDBImporter was not connected to DB");
        if (langFrom == null) throw new IllegalStateException("langFrom was not set");
        if (langTo == null) throw new IllegalStateException("langTo was not set");

        psInsertInf = con.prepareStatement("INSERT INTO "+getLangFrom()+"_inf(inf, type) VALUES (?,?)",
                Statement.RETURN_GENERATED_KEYS);
        psFindInfByInfAndType = con.prepareStatement("SELECT id FROM "+getLangFrom()+"_inf WHERE inf = ? and type = ?");

        psFindTranslation = con.prepareStatement("SELECT * FROM tr_"+langFrom+"_"+getLangTo()+" WHERE fk_inf = ? and translation = ?");
        psUpdateTranslation = con.prepareStatement("UPDATE tr_"+langFrom+"_"+langTo+" SET example = ? WHERE id = ?");
        psInsertTranslation = con.prepareStatement("INSERT INTO tr_"+langFrom+"_"+langTo+" (order_nr, fk_inf, translation, example) " +
                        " VALUES(?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
        psDeleteTranslation = con.prepareStatement("DELETE FROM tr_"+getLangFrom()+"_"+getLangTo()+" WHERE fk_inf = ?");


		psDeleteDeWordProperties = con.prepareStatement("DELETE FROM de_word_property WHERE de_word_id = ?");
		psInsertDeWordProperty = con.prepareStatement("INSERT INTO de_word_property(de_word_id, type, pname, pvalue) " +
				" VALUES (?,?,?,?)",
				Statement.RETURN_GENERATED_KEYS);
		
	}

	private int getGeneratedId(PreparedStatement ps) throws SQLException {
		int ret = -1;
        ResultSet generatedKeys = null;
        try {
			generatedKeys = ps.getGeneratedKeys();
			if (generatedKeys.next()) {
				ret = generatedKeys.getInt(1);
			} else {
			    throw new SQLException("No generated key obtained.");
			}
		} finally {
			DBUtil.closeResultSet(generatedKeys);
		}
		return ret;
	}
	
	public DbObjectRef getOrCreateInf(Inf inf) throws SQLException {
		DbObjectRef ret;
        //get existing DeWord
        DbObjectRef infId = getInf(inf.getInf(), inf.getType());
        if (DbObjectRef.OBJ_NOT_FOUND == infId.getState()) {
            //insert word
            ret = insertInf(inf);
            addStats("Inf.new");
        } else {
            ret = infId;
            log.warning("Word '"+inf.getInf()+"', type = "+inf.getType()+" exist.");
            addStats("Inf.exist");
        }

		return ret;
	}
	
	public DbObjectRef insertInf(Inf inf) throws SQLException {
		DbObjectRef ret = new DbObjectRef();
        psInsertInf.setString(1, inf.getInf());
        psInsertInf.setInt(2, inf.getType());
		if (inf.getTranscription() != null) {
            //TODO insert or update transcription
		}
		int affectedRows = psInsertInf.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating "+langFrom+"_inf ["+inf.getInf() + ", "+ inf.getType() +"] failed, no rows affected.");
        }

		ret.setId(getGeneratedId(psInsertInf));
		ret.setState(DbObjectRef.OBJ_CREATED);
		return ret;
	}
	
	public DbObjectRef getInf(String inf, Integer type) throws SQLException {
		DbObjectRef ret = new DbObjectRef();
		psFindInfByInfAndType.setString(1, inf);
        psFindInfByInfAndType.setInt(2, type);
		int id = DBUtil.exeIntPreparedStatement(psFindInfByInfAndType);
		if (id != -1) {
			ret.setId(id);
			ret.setState(DbObjectRef.OBJ_FOUND);
		} else {
			ret.setId(id);
			ret.setState(DbObjectRef.OBJ_NOT_FOUND);
		}
		return ret;
	}

    public DbObjectRef getOrCreateTranslation(Translation translation, Integer infId) throws SQLException {
        DbObjectRef ret;
        //check translation
        DbObjectRef translationId = getTranslation(translation, infId);
        if (DbObjectRef.OBJ_NOT_FOUND == translationId.getState()) {
            //insert translation
            ret = insertTranslation(translation, infId);
            addStats("Translation.new");
        } else {
            //check for update
            Translation dbTran = (Translation)translationId.getObj();
            if (dbTran != null) {
                if (dbTran.getExample() != null && dbTran.getExample().length() > 0) {
                    //DB has example
                    if (translation.getExample() != null && translation.getExample().length() > 0) {
                        if (!dbTran.getExample().contains(translation.getExample())) {
                            //merge examples
                            translation.setExample(dbTran.getExample()+" | "+translation.getExample());
                            //update with example from file
                            updateTranslation(translation, translationId.getId());
                            log.warning("Update example in translation [id = " + translationId.getId() + ", tr = '" + dbTran.getTranslation() + "']\n"
                                + "Old example '" + dbTran.getExample() + "'\n"
                                + "New example '" + translation.getExample() + "'");
                            addStats("Translation.updated");
                        } else {
                            // example is already in DB
                            addStats("Translation.ignored");
                        }
                    } else {
                        // example is empty
                        addStats("Translation.ignored");
                    }
                } else {
                    //no example in DB
                    if (translation.getExample() != null && translation.getExample().length() > 0) {
                        //update with example from file
                        updateTranslation(translation, translationId.getId());
                        addStats("Translation.updated");
                    } else {
                        // example is empty
                        addStats("Translation.ignored");
                    }
                }
            } else {
                throw new SQLException("Translation found but DB-Object is null for id = "+translationId.getId());
            }
            ret = translationId;
        }

        return ret;
    }

    public DbObjectRef getTranslation(Translation translation, Integer infId) throws SQLException {
        DbObjectRef ret = new DbObjectRef();
        psFindTranslation.setInt(1, infId);
        psFindTranslation.setString(2, translation.getTranslation());
        ResultSet rs = null;
        try {
            rs = psFindTranslation.executeQuery();

            if (rs.next()) {
                Translation obj = new Translation();
                obj.setId(rs.getInt("id"));
                obj.setOrderNr(rs.getInt("order_nr"));
                obj.setTranslation(translation.getTranslation());
                obj.setExample(rs.getString("example"));

                ret.setId(obj.getId());
                ret.setState(DbObjectRef.OBJ_FOUND);
                ret.setObj(obj);
            } else {
                ret.setId(-1);
                ret.setState(DbObjectRef.OBJ_NOT_FOUND);
            }
        } finally {
            DBUtil.closeResultSet(rs);
        }
        return ret;
    }

    public void updateTranslation(Translation translation, long trId) throws SQLException {
        psUpdateTranslation.setString(1, translation.getExample());
        psUpdateTranslation.setLong(2, trId);
        int affectedRows = psUpdateTranslation.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Updating tr_"+langFrom+"_"+langTo+" [ id = "+ trId +"] failed, no rows affected.");
        }
    }

    public DbObjectRef insertTranslation(Translation translation, Integer infId) throws SQLException {
        DbObjectRef ret = new DbObjectRef();
        psInsertTranslation.setInt(1, translation.getOrderNr());
        psInsertTranslation.setInt(2, infId);
        psInsertTranslation.setString(3, translation.getTranslation());
        psInsertTranslation.setString(4, translation.getExample());
        int affectedRows = psInsertTranslation.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating tr_"+langFrom+"_"+langTo+" [ fk_inf = "+infId +", translation = "+translation.getTranslation()+"] failed, no rows affected.");
        }

        ret.setId(getGeneratedId(psInsertTranslation));
        ret.setState(DbObjectRef.OBJ_CREATED);
        return ret;
    }

    public void deleteTranslations(Integer infId) throws SQLException {
        psDeleteTranslation.setInt(1, infId);
        int affectedRows = psDeleteTranslation.executeUpdate();
        if (affectedRows > 0) {
            log.warning(""+affectedRows+" translation(s) deleted.");
            addStats("Translation.deleted", affectedRows);
        }
    }

    public void storeTranslations(Inf inf, Integer infId) throws SQLException {
        for (Translation translation : inf.getTrList()) {
            //store Translation
            getOrCreateTranslation(translation,infId);
        }
    }


//	public void storeProperties(DeWord deWord, long deWordId) throws SQLException {
//		//delete existing
//		deleteProperties(deWordId);
//		//create new
//		if (deWord.getPropertyList() != null) {
//			for (WordProperty wp : deWord.getPropertyList()) {
//				insertProperty(wp, deWordId);
//			}
//		}
//	}
//
//	public void deleteProperties(long wordId) throws SQLException {
//		psDeleteDeWordProperties.setLong(1, wordId);
//		int affectedRows = psDeleteDeWordProperties.executeUpdate();
//	}
//
//	public DbObjectRef insertProperty(WordProperty wp, long wordId) throws SQLException {
//		DbObjectRef ret = new DbObjectRef();
//		psInsertDeWordProperty.setLong(1, wordId);
//		psInsertDeWordProperty.setString(2, wp.getType());
//		psInsertDeWordProperty.setString(3, wp.getPname());
//		psInsertDeWordProperty.setString(4, wp.getPvalue());
//		int affectedRows = psInsertDeWordProperty.executeUpdate();
//        if (affectedRows == 0) {
//            throw new SQLException("Creating de_word_property [ word_id = "+wordId +"] failed, no rows affected.");
//        }
//
//		ret.setId(getGeneratedId(psInsertDeWordProperty));
//		ret.setState(DbObjectRef.OBJ_CREATED);
//		return ret;
//	}
}
