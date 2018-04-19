package org.linguisto.tools.db;

import org.linguisto.tools.obj.Inf;
import org.linguisto.tools.obj.Translation;
import org.linguisto.tools.obj.WordForm;
import org.linguisto.tools.obj.WordProperty;
import org.linguisto.tools.obj.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ReaderDAO {
	
	private Connection con;
	
	public ReaderDAO(Connection con) {
		this.con = con;
	}

	public ResultSet getDictExportRS(String langFrom, String langTo) throws SQLException {
        String sql =  "SELECT inf.id INF_ID, inf.inf INF, inf.type TYPE," +
                "   fr.rank RANK, trans.transcription TRANSCRIPTION," +
                "   tr.id TR_ID, tr.order_nr TR_ORDER, tr.translation TRANSLATION, tr.example TR_EX " +
                " FROM "+langFrom+"_inf inf " +
                "   LEFT JOIN tr_"+langFrom+"_uk tr ON inf.id = tr.fk_inf" +
                "   LEFT JOIN (SELECT word, min(rank) rank FROM "+langFrom+"_frequency group by word) fr ON fr.word = inf.inf " +
                "   LEFT JOIN "+langFrom+"_transcription trans ON trans.word = inf.inf " +
                "ORDER BY COALESCE(fr.rank,999999), inf.inf, inf.type, tr.order_nr";
		PreparedStatement ps = con.prepareStatement(sql);
		return ps.executeQuery();
	}

    public ResultSet getXDXFDictExportTestRS(String langFrom, String langTo) throws SQLException {
        String sql =  "SELECT inf.id INF_ID, inf.inf INF, inf.type TYPE," +
                "   fr.rank RANK, trans.transcription TRANSCRIPTION," +
                "   tr.id TR_ID, tr.order_nr TR_ORDER, tr.translation TRANSLATION, tr.example TR_EX " +
                " FROM "+langFrom+"_inf inf " +
                "   LEFT JOIN tr_"+langFrom+"_uk tr ON inf.id = tr.fk_inf" +
                "   LEFT JOIN (SELECT word, min(rank) rank FROM "+langFrom+"_frequency WHERE rank < 200 GROUP BY word) fr ON fr.word = inf.inf " +
                "   LEFT JOIN "+langFrom+"_transcription trans ON trans.word = inf.inf " +
                "  WHERE tr.id is not null AND fr.rank is not null AND fr.rank < 200 " +
                "ORDER BY inf.inf, inf.type, tr.order_nr LIMIT 0,300 ";
        PreparedStatement ps = con.prepareStatement(sql);
        return ps.executeQuery();
    }

    public ResultSet getXDXFDictExportRS(String langFrom, String langTo) throws SQLException {
        String sql =  "SELECT inf.id INF_ID, inf.inf INF, inf.type TYPE," +
                "   fr.rank RANK, trans.transcription TRANSCRIPTION," +
                "   tr.id TR_ID, tr.order_nr TR_ORDER, tr.translation TRANSLATION, tr.example TR_EX " +
                " FROM "+langFrom+"_inf inf " +
                "   LEFT JOIN tr_"+langFrom+"_uk tr ON inf.id = tr.fk_inf" +
                "   LEFT JOIN (SELECT word, min(rank) rank FROM "+langFrom+"_frequency GROUP BY word) fr ON fr.word = inf.inf " +
                "   LEFT JOIN "+langFrom+"_transcription trans ON trans.word = inf.inf " +
                "  WHERE tr.id is not null " +
                "ORDER BY inf.inf, inf.type, tr.order_nr ";
        PreparedStatement ps = con.prepareStatement(sql);
        return ps.executeQuery();
    }

    public ResultSet getXDXFDictExportRS2(String langFrom, String langTo) throws SQLException {
        String sql =  "SELECT inf.id INF_ID, inf.inf INF, inf.type TYPE," +
                "   inf.rank RANK, inf.transcription TRANSCRIPTION," +
                "   tr.id TR_ID, tr.order_nr TR_ORDER, tr.translation TRANSLATION, tr.example TR_EX " +
                " FROM inf " +
                "   LEFT JOIN tr ON inf.id = tr.fk_inf" +
                "  WHERE inf.lang = '"+langFrom+"' AND tr.id is not null " +
                "ORDER BY inf.inf, inf.type, tr.order_nr ";
        PreparedStatement ps = con.prepareStatement(sql);
        return ps.executeQuery();
    }

    public ResultSet getDictExportByInfIdRS(String langFrom, String langTo) throws SQLException {
        String sql =  "SELECT inf.id INF_ID, inf.inf INF, inf.type TYPE," +
                "   fr.rank RANK, trans.transcription TRANSCRIPTION," +
                "   tr.id TR_ID, tr.order_nr TR_ORDER, tr.translation TRANSLATION, tr.example TR_EX " +
                " FROM "+langFrom+"_inf inf " +
                "   LEFT JOIN tr_"+langFrom+"_uk tr ON inf.id = tr.fk_inf" +
                "   LEFT JOIN (SELECT word, min(rank) rank FROM "+langFrom+"_frequency group by word) fr ON fr.word = inf.inf " +
                "   LEFT JOIN "+langFrom+"_transcription trans ON trans.word = inf.inf " +
                " WHERE inf.id in (9463,13035,14830,15730)" +
                "ORDER BY COALESCE(fr.rank,999999), inf.inf, inf.type, tr.order_nr";
        PreparedStatement ps = con.prepareStatement(sql);
        return ps.executeQuery();
    }

    public ResultSet getDictExportDoubletsRS(String langFrom, String langTo) throws SQLException {
        String sql =  "SELECT inf.id INF_ID, inf.inf INF, inf.type TYPE," +
                "   1 RANK, '' TRANSCRIPTION," +
                "   tr.id TR_ID, tr.order_nr TR_ORDER, tr.translation TRANSLATION, tr.example TR_EX " +
                " FROM "+langFrom+"_inf inf " +
                "   LEFT JOIN tr_"+langFrom+"_uk tr ON inf.id = tr.fk_inf" +
                " WHERE (inf.inf, inf.type) in (select inf, type from "+langFrom+"_inf group by inf, type having count(id) > 1)" +
                "ORDER BY inf.inf, inf.type, inf.id, tr.order_nr";
        PreparedStatement ps = con.prepareStatement(sql);
        return ps.executeQuery();
    }

    public ResultSet getDictNoTrExportRS(String langFrom, String langTo) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
                "SELECT inf.id INF_ID, inf.inf INF, inf.type TYPE," +
                        "   fr.rank RANK, null TRANSCRIPTION, " +
                        "   tr.id TR_ID, tr.order_nr TR_ORDER, tr.translation TRANSLATION, tr.example TR_EX " +
                        " FROM "+langFrom+"_inf inf " +
                        "   LEFT JOIN tr_"+langFrom+"_uk tr ON inf.id = tr.fk_inf" +
                        "   LEFT JOIN (SELECT word, min(rank) rank FROM "+langFrom+"_frequency group by word) fr ON fr.word = inf.inf " +
                        " WHERE tr.id is null " +
                        "ORDER BY COALESCE(fr.rank,999999), inf.id, inf.type, tr.order_nr");
        return ps.executeQuery();
    }

    public Inf readInf(ResultSet rs) throws SQLException {
        Inf ret = new Inf();
		ret.setId(rs.getInt("INF_ID"));
		ret.setInf(rs.getString("INF"));
        ret.setType(rs.getInt("TYPE"));
        int rank = rs.getInt("RANK");
        if (!rs.wasNull()) {
            ret.setFrequency(rank);
        }
		ret.setTranscription(rs.getString("TRANSCRIPTION"));

		return ret;
	}

	public Map<Integer, List<WordForm>> readWordForms(String langFrom) throws SQLException {
		Map<Integer, List<WordForm>> ret = new HashMap<Integer, List<WordForm>>();
		ResultSet rs = null;
		int lastWordId = -1;
		List<WordForm> wpList = new ArrayList<WordForm>();
		try {
			rs = con.createStatement().executeQuery(
                    "SELECT wf.fk_inf, wf.fid, wf.wf " +
                            "FROM "+langFrom+"_wf wf " +
                            "ORDER BY wf.fk_inf, wf.wf");
			
			while (rs.next()) {
				if (lastWordId != rs.getInt("fk_inf")) {
					if (lastWordId > -1) {
						ret.put(lastWordId, wpList);
					}
					lastWordId = rs.getInt("fk_inf");
					wpList = new ArrayList<WordForm>();
				}
                WordForm wf = new WordForm(rs.getString("fid"), rs.getString("wf"));
                wpList.add(wf);
			}
			if (lastWordId > -1) {
				ret.put(lastWordId, wpList);
			}
		} finally {
			DBUtil.closeResultSet(rs);
		}
		
		return ret;
	}

    public Map<Integer, String> readWordTypeDescMap(String lang) throws SQLException {
        Map<Integer, String> ret = new HashMap<Integer, String>();
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(
                    "SELECT wt.id, wt.desc " +
                            " FROM word_type wt " +
                            " WHERE lang = ?");
            ps.setString(1, lang);
            rs = ps.executeQuery();
            while (rs.next()) {
                ret.put(rs.getInt("id"), rs.getString("desc"));
            }
        } finally {
            DBUtil.closeStatement(ps);
            DBUtil.closeResultSet(rs);
        }

        return ret;
    }

    public Map<Integer, String> readWordTypeCommentMap(String lang) throws SQLException {
        Map<Integer, String> ret = new HashMap<Integer, String>();
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(
                    "SELECT wt.id, wt.comment " +
                            " FROM word_type wt " +
                            " WHERE lang = ?");
            ps.setString(1, lang);
            rs = ps.executeQuery();
            while (rs.next()) {
                ret.put(rs.getInt("id"), rs.getString("comment"));
            }
        } finally {
            DBUtil.closeStatement(ps);
            DBUtil.closeResultSet(rs);
        }

        return ret;
    }

    public List<WordProperty> readWordProperties(long wordId) throws SQLException {
        List<WordProperty> ret = new ArrayList<WordProperty>();
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(
                    "SELECT p.pname, p.pvalue " +
                            "FROM de_word_property p " +
                            "WHERE p.de_word_id = ? " +
                            "ORDER BY p.pname");
            ps.setLong(1, wordId);

            rs = ps.executeQuery();

            while (rs.next()) {
                ret.add(new WordProperty(rs.getString("pname"), rs.getString("pvalue")));
            }
        } finally {
            DBUtil.closeStatement(ps);
            DBUtil.closeResultSet(rs);
        }

        return ret;
    }

	public Translation readTranslation(ResultSet rs) throws SQLException {
		Translation ret = new Translation();
		ret.setId(rs.getInt("TR_ID"));
		ret.setOrderNr(rs.getInt("TR_ORDER"));
        ret.setTranslation(rs.getString("TRANSLATION"));
        ret.setExample(rs.getString("TR_EX"));
		return ret;
	}

}
