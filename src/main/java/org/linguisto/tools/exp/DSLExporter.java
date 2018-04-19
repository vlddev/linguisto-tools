package org.linguisto.tools.exp;

import org.linguisto.tools.db.Constants;
import org.linguisto.tools.db.DBUtil;
import org.linguisto.tools.db.ReaderDAO;
import org.linguisto.tools.obj.Inf;
import org.linguisto.tools.obj.Translation;
import org.linguisto.tools.db.Constants;
import org.linguisto.tools.db.DBUtil;
import org.linguisto.tools.db.ReaderDAO;
import org.linguisto.tools.obj.Inf;
import org.linguisto.tools.obj.Translation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Export jsfdict database to Lingvo DSL-Format (description of XDXF http://lingvo.helpmax.net/ru/%D0%B2%D0%BE%D0%BF%D1%80%D0%BE%D1%81%D1%8B-%D0%B8-%D0%B7%D0%B0%D1%82%D1%80%D1%83%D0%B4%D0%BD%D0%B5%D0%BD%D0%B8%D1%8F/dsl-compiler/%D1%81%D1%82%D1%80%D1%83%D0%BA%D1%82%D1%83%D1%80%D0%B0-%D1%81%D0%BB%D0%BE%D0%B2%D0%B0%D1%80%D1%8F-%D0%BD%D0%B0-%D1%8F%D0%B7%D1%8B%D0%BA%D0%B5-dsl/).
 * Call must looks like:
 * java ... DSLExporter -Djdbc.driver=com.mysql.jdbc.Driver -Djdbc.url=jdbc:mysql://localhost/dict?useUnicode=true&characterEncoding=UTF-8
 *                      -Djdbc.user=[user] -Djdbc.password=[pwd] [export file]
 */
public class DSLExporter {

	public static final Logger log = Logger.getLogger(DSLExporter.class.getName());

	public static void main(String[] args) throws Exception {
		//DictLogger.setup();
		String sExpFile = "vlad.dict.dsl";
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
		DSLExporter xmlExporter = new DSLExporter();
		xmlExporter.export(args[0], args[1], sExpFile);
	}

    private String getDSLLang(String lang) {
        String ret = "";
        if ("de".equalsIgnoreCase(lang)) {
            ret = "German";
        } else if ("en".equalsIgnoreCase(lang)) {
            ret = "English";
        } else if ("uk".equalsIgnoreCase(lang)) {
            ret = "Ukrainian";
        }
        return ret;
    }

    public void export(String langFrom, String langTo, String file) {
        String sNewLine = System.getProperty("line.separator");
        Connection con = null;
        ResultSet rs = null;
        PrintWriter pw = null;
        try {
            con = DBUtil.getConnection(System.getProperty(Constants.SYS_JDBC_USER),
                    System.getProperty(Constants.SYS_JDBC_PASSWORD),
                    System.getProperty(Constants.SYS_JDBC_URL),
                    System.getProperty(Constants.SYS_JDBC_DRIVER));

            ReaderDAO reader = new ReaderDAO(con);

            rs = reader.getXDXFDictExportRS(langFrom, langTo);

            Locale localeLangFrom = new Locale(langFrom);
            Locale localeLangTo = new Locale(langTo);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String version = "001";

            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-16LE"),true);
            pw.println("#NAME \"(DSL) Vlad Deutsch-Ukrainisches Wörterbuch\"");
            pw.println("#INDEX_LANGUAGE \""+getDSLLang(langFrom)+"\"");
            pw.println("#CONTENTS_LANGUAGE \""+getDSLLang(langTo)+"\"");
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
        ret.append(word.getInf()).append(ln);
        //TODO: add grammatical info. Convert word type to human readable, add wordforms etc.
        ret.append(prefix).append("[m][b]").append(word.getType()).append("[/b]");
        if (word.getTranscription() != null && word.getTranscription().length() > 0) {
            ret.append(" [c]").append(word.getTranscription().replace("[","").replace("]","")).append("[/c]");
        }
        if (word.getFrequency() != null) {
            ret.append(prefix).append(" [com][i]freq = ").append(word.getFrequency()).append("[/i][/com]");
        }
        ret.append("[/m]").append(ln);

        if (word.getTrList() != null) {
            boolean bExsTagAdded = false;
            for (Translation tr : word.getTrList()) {
                ret.append(prefix).append("");
                //TODO: replace links like [[]] with <kref>
                ret.append(prefix).append(prefix).append("[m1][trn]").append(tr.getTranslation()==null?"":tr.getTranslation()).append("[/trn][/m]").append(ln);
                if (tr.getExample() != null && tr.getExample().length() > 0) {
                    for (String ex : tr.getExamples()) {
                        //TODO: split into <ex_orig> and <ex_tran> if possible
                        ret.append(prefix).append(prefix).append(prefix).append("[m2][*][ex]").append(ex.trim()).append("[/ex][/*][/m]").append(ln);
                    }
                }
            }
        }
        return ret.toString();
    }
}
