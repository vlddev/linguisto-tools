package org.linguisto.tools.exp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.linguisto.tools.db.Constants;
import org.linguisto.tools.db.DBUtil;
import org.linguisto.tools.db.ReaderDAO;
import org.linguisto.tools.obj.Inf;
import org.linguisto.tools.obj.Translation;
import org.linguisto.tools.obj.WordForm;
import org.linguisto.tools.obj.Inf;
import org.linguisto.tools.obj.WordForm;
import org.linguisto.tools.obj.WordProperty;
import org.linguisto.tools.db.Constants;
import org.linguisto.tools.db.DBUtil;
import org.linguisto.tools.db.ReaderDAO;
import org.linguisto.tools.obj.Translation;

/**
 * Export jsfdict database to jsfdict-XML.
 * Call must looks like:
 * java ... XMLExporter -Djdbc.driver=com.mysql.jdbc.Driver -Djdbc.url=jdbc:mysql://localhost/dict?useUnicode=true&characterEncoding=UTF-8
 *                      -Djdbc.user=[user] -Djdbc.password=[pwd] [export file]
 */
public class XMLExporter {

	public static final Logger log = Logger.getLogger(XMLExporter.class.getName());

	public static void main(String[] args) throws Exception {
		//DictLogger.setup();
		String sExpFile = "org.linguisto.tools.xml";
		if (args.length > 2) {
			sExpFile = args[2];
		} else {
			throw new Exception("There must be at least three params: <langFrom> <langTo> <export file>");
		}
		//check if sExpFile already exist
		File expFile = new File(sExpFile);
		if (expFile.exists()) {
			throw new Exception("File '"+expFile.getAbsolutePath()+"' already exist.");
		}
		XMLExporter xmlExporter = new XMLExporter();
		xmlExporter.export(args[0], args[1], sExpFile);
	}

    public void export(String langFrom, String langTo, String file) {
        String sNewLine = System.getProperty("line.separator");
        Connection con = null;
        ResultSet rs = null;
        PrintWriter pw = null;
        boolean exportWf = false;
        try {
            con = DBUtil.getConnection(System.getProperty(Constants.SYS_JDBC_USER),
                    System.getProperty(Constants.SYS_JDBC_PASSWORD),
                    System.getProperty(Constants.SYS_JDBC_URL),
                    System.getProperty(Constants.SYS_JDBC_DRIVER));

            ReaderDAO reader = new ReaderDAO(con);

            //rs = reader.getDictExportRS(langFrom, langTo);
            rs = reader.getDictExportDoubletsRS(langFrom, langTo);

            Map<Integer, List<WordForm>> wpMap = new HashMap<Integer, List<WordForm>>();
            if (exportWf) {
                wpMap = reader.readWordForms(langFrom);
            }
            //Map<Long, List<WordProperty>> wpMap = new HashMap<Long, List<WordProperty>>();

            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"),true);
            //write XML-Header
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<vlad.vdict>");
            pw.println("  <header>");
            pw.println("    <lang.from>"+langFrom+"</lang.from>");
            pw.println("    <lang.to>"+langTo+"</lang.to>");
            pw.println("  </header>");
            Inf curWord = null;
            Translation curTranslation = null;
            while(rs.next()) {
                if (curWord == null || !curWord.getId().equals(rs.getInt("INF_ID"))) {
                    // write word to file
                    if (curWord != null) {
                        pw.print(getExportXml(curWord, "  ", sNewLine));
                    }
                    //read new word
                    curWord = reader.readInf(rs);
                    // read word_forms
                    List<WordForm> wfList = wpMap.get(curWord.getId());
                    if (wfList != null && wfList.size() > 0) {
                        curWord.setWfList(wfList);
                    }
                    curTranslation = null;
                }
                if (curTranslation == null || !curTranslation.getId().equals(rs.getInt("TR_ID"))) {
                    //read translation
                    curTranslation = reader.readTranslation(rs);
                    curWord.addTranslation(curTranslation);
                }
            }
            // write last word to file
            if (curWord != null) {
                pw.print(getExportXml(curWord, "  ", sNewLine));
            }
            //closing tag
            pw.println("</vlad.vdict>");
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            pw.flush();
            pw.close();
            DBUtil.closeResultSet(rs);
            DBUtil.closeConnection(con);
        }
    }
	
    private String getExportXml(Inf word, String prefix, String ln) {
        StringBuffer ret = new StringBuffer();
        ret.append("<word>").append(ln);
        ret.append(prefix).append("<inf>").append(word.getInf()).append("</inf>").append(ln);
        ret.append(prefix).append("<type>").append(word.getType()).append("</type>").append(ln);
        if (word.getFrequency() != null) {
            ret.append(prefix).append("<freq>").append(word.getFrequency()).append("</freq>").append(ln);
        }
        if (word.getTranscription() != null && word.getTranscription().length() > 0) {
            ret.append(prefix).append("<transcription>").append(word.getTranscription()).append("</transcription>").append(ln);
        }
		if (word.getWfList() != null) {
			for (WordForm property : word.getWfList()) {
				ret.append(prefix).append("<wf><fid>").append(property.getFid()==null?"":property.getFid()).append("</fid>")
                        .append("<fv>").append(property.getWf()).append("</fv></wf>").append(ln);
			}
		}
        if (word.getTrList() != null) {
            boolean bExsTagAdded = false;
            for (Translation tr : word.getTrList()) {
                ret.append(prefix).append("<tr>").append(ln);
                //ret.append(prefix).append(prefix).append("<nr>").append(tr.getOrderNr()).append("</nr>").append(ln);
                ret.append(prefix).append(prefix).append("<txt>").append(tr.getTranslation()==null?"":tr.getTranslation()).append("</txt>").append(ln);
                if (tr.getExample() != null && tr.getExample().length() > 0) {
                    //ret.append(prefix).append(prefix).append("<exs>").append(ln);
                    for (String ex : tr.getExamples()) {
                        ret.append(prefix).append(prefix).append(prefix).append("<ex>").append(ex.trim()).append("</ex>").append(ln);
                    }
                    //ret.append(prefix).append(prefix).append("</exs>").append(ln);
                }
                ret.append(prefix).append("</tr>").append(ln);
            }
        }
        ret.append("</word>").append(ln);
        return ret.toString();
    }
}
