package org.linguisto.tools.exp;

import org.linguisto.tools.db.Constants;
import org.linguisto.tools.db.DBUtil;
import org.linguisto.tools.db.ReaderDAO;
import org.linguisto.tools.obj.Inf;
import org.linguisto.tools.obj.Translation;
import org.linguisto.tools.db.Constants;
import org.linguisto.tools.db.DBUtil;
import org.linguisto.tools.db.ReaderDAO;
import org.linguisto.tools.exp.parser.Parser;
import org.linguisto.tools.exp.parser.SChunk;
import org.linguisto.tools.obj.Inf;
import org.linguisto.tools.obj.Translation;
import org.linguisto.tools.obj.WordForm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Export jsfdict database to XDXF-Format (description of XDXF https://github.com/soshial/xdxf_makedict/blob/master/format_standard/xdxf_description.md).
 * Call must looks like:
 * java ... XDXFExporter -Djdbc.driver=com.mysql.jdbc.Driver -Djdbc.url=jdbc:mysql://localhost/dict?useUnicode=true&characterEncoding=UTF-8
 *                      -Djdbc.user=[user] -Djdbc.password=[pwd] [export file]
 */
public class XDXFExporter {

	public static final Logger log = Logger.getLogger(XDXFExporter.class.getName());

    Map<Integer,String> wtMap = new HashMap<Integer, String>();

	public static void main(String[] args) throws Exception {
		//DictLogger.setup();
		String sExpFile = "vlad.dict.xdxf";
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
		XDXFExporter xmlExporter = new XDXFExporter();
		xmlExporter.export(args[0], args[1], sExpFile);
	}

    private String getXDXFLang(String lang) {
        String ret = "";
        if ("de".equalsIgnoreCase(lang)) {
            ret = "GER";
        } else if ("en".equalsIgnoreCase(lang)) {
            ret = "ENG";
        } else if ("uk".equalsIgnoreCase(lang)) {
            ret = "UKR";
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

            rs = reader.getXDXFDictExportRS2(langFrom, langTo);

            wtMap = reader.readWordTypeCommentMap(langFrom);

            Locale localeLangFrom = new Locale(langFrom);
            Locale localeLangTo = new Locale(langTo);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String version = "001";
            int wordCount = 0;

            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"),true);
            //write XML-Header
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<xdxf lang_from=\""+getXDXFLang(langFrom)+"\" lang_to=\""+getXDXFLang(langTo)+"\" format=\"logical\" revision=\""+version+"\">");
            pw.println("  <full_name>Німецько-український словник (укладач Володимир Влад)</full_name>");
            pw.println("  <meta_info>");
            pw.println("    <title>Німецько-український словник (укладач Володимир Влад)</title>");
            pw.println("    <full_title>Deutsch-Ukrainisches Wörterbuch von V.Vlad / Німецько-український словник (укладач Володимир Влад)</full_title>");
            pw.println("    <description>Єдиний німецько-український електронний словник. Актуальна онлайн-версія доступна на http://linguisto.eu/</description>");
            pw.println("    <file_ver>" + version+"</file_ver>");
            pw.println("    <creation_date>"+ sdf.format(new Date())+"</creation_date>");
            pw.println("  </meta_info>");
            pw.println("  <lexicon>");
            Inf curWord = null;
            Translation curTranslation = null;
            while(rs.next()) {
                if (curWord == null || !curWord.getId().equals(rs.getInt("INF_ID"))) {
                    // write word to file
                    if (curWord != null) {
                        wordCount++;
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
                wordCount++;
                pw.print(getExportXml(curWord, "  ", sNewLine));
            }
            //closing tag
            pw.println("</lexicon></xdxf>");
            log.info("Exported word count: "+wordCount);
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
        ret.append("<ar>").append(ln);
        ret.append(prefix).append("<k>").append(word.getInf()).append("</k>").append(ln);
        if (word.getFrequency() != null) {
            ret.append(prefix).append("<def freq=\"").append(word.getFrequency()).append("\">").append(ln);
        } else {
            ret.append(prefix).append("<def>").append(ln);
        }
        if (word.getTranscription() != null && word.getTranscription().length() > 0) {
            //ret.append(prefix).append(prefix).append("<tr>").append(word.getTranscription().replace("[","").replace("]","")).append("</tr>");
            ret.append(prefix).append(prefix).append("<tr>").append(word.getTranscription()).append("</tr>");
        }
        //TODO: add grammatical info, wordforms etc.
        ret.append(ln).append(prefix);
        ret.append("<gr><c c=\"#006600\">").append(wtMap.get(word.getType())).append("</c>");
        if (word.getFrequency() != null) {
            ret.append("<br/><c c=\"#3300FF\">Ранг: ").append(word.getFrequency()).append("</c>");
        }
        ret.append("</gr>").append(ln);

        if (word.getTrList() != null) {
            boolean bExsTagAdded = false;
            for (Translation tr : word.getTrList()) {
                ret.append(prefix).append(prefix).append("<def>");
                String strTr = tr.getTranslation();
                if (strTr != null) {
                    // replace links like [[]] with <kref>
                    // parse comments in round brackets
                    strTr = Parser.getParsedTranslation(strTr);
                } else {
                    strTr = "";
                }
                ret.append("<dtrn>").append(strTr).append("</dtrn>").append(ln);
                if (tr.getExample() != null && tr.getExample().length() > 0) {
                    for (String ex : tr.getExamples()) {
                        //split into <ex_orig> and <ex_tran> if possible
                        String strEx = ex.trim();
                        ret.append(prefix).append(prefix).append(prefix).append("<ex>");
                        //TODO use Parser.parseExamples
                        int pos = strEx.indexOf("→");
                        if (pos > 0) {
                            ret.append("<ex_orig>").append(Parser.getParsedExample(strEx.substring(0, pos))).append("</ex_orig>");
                            ret.append("<ex_tran><c c=\"#3300FF\">").append(strEx.substring(pos+1)).append("</c></ex_tran>");
                        } else {
                            ret.append(Parser.getParsedExample(ex.trim()));
                        }
                        ret.append("</ex>").append(ln);
                    }
                }
                ret.append(prefix).append(prefix).append("</def>").append(ln);
            }
        }
        ret.append(prefix).append("</def>").append(ln);
        ret.append("</ar>").append(ln);
        return ret.toString();
    }
}
