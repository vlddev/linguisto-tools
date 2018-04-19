package org.linguisto.tools.exp;

import org.linguisto.tools.db.Constants;
import org.linguisto.tools.db.DBUtil;
import org.linguisto.tools.db.ReaderDAO;
import org.linguisto.tools.db.Constants;
import org.linguisto.tools.db.DBUtil;
import org.linguisto.tools.db.ReaderDAO;
import org.linguisto.tools.obj.Inf;
import org.linguisto.tools.obj.Translation;
import org.linguisto.tools.obj.WordForm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Export jsfdict database to jsfdict-XML.
 * Call must looks like:
 * java ... XMLExporter -Djdbc.driver=com.mysql.jdbc.Driver -Djdbc.url=jdbc:mysql://localhost/dict?useUnicode=true&characterEncoding=UTF-8
 *                      -Djdbc.user=[user] -Djdbc.password=[pwd] [export file]
 */
public class StatsReporter {

	public static final Logger log = Logger.getLogger(StatsReporter.class.getName());

    public static final SimpleDateFormat sdfMmYyyy = new SimpleDateFormat("MM.yyyy");
    public static final SimpleDateFormat sdfMmYyyyMinus = new SimpleDateFormat("MM-yyyy");

    public static void main(String[] args) throws Exception {
		//DictLogger.setup();
		Date reportDate = new Date();
        String reportFile = "/home/vlad/Dokumente/my_dev/open-shift/db/jsfDictStats";
		if (args.length > 0) {
            reportDate = sdfMmYyyy.parse(args[0]);
            log.info("Report date: "+sdfMmYyyy.format(reportDate));
            reportFile = reportFile+"_"+sdfMmYyyyMinus.format(reportDate)+".txt";
		} else {
			throw new Exception("There must be at least one param: <date mm.yyyy>");
		}
		//check if sExpFile already exist
		File expFile = new File(reportFile);

		StatsReporter xmlExporter = new StatsReporter();
		xmlExporter.report(reportDate, reportFile);
	}

    public void report(Date reportDate, String file) {
        String sNewLine = System.getProperty("line.separator");
        Connection con = null;
        PreparedStatement ps = null;
        PrintWriter pw = null;
        try {
            con = DBUtil.getConnection(System.getProperty(Constants.SYS_JDBC_USER),
                    System.getProperty(Constants.SYS_JDBC_PASSWORD),
                    System.getProperty(Constants.SYS_JDBC_URL),
                    System.getProperty(Constants.SYS_JDBC_DRIVER));

            ReaderDAO reader = new ReaderDAO(con);

            java.sql.Date sqlReportDate = new java.sql.Date(reportDate.getTime());
            String strWhereTime = "atime >= (LAST_DAY(?) + INTERVAL 1 DAY - INTERVAL 1 MONTH) " +
                    "   AND atime <  (LAST_DAY(?) + INTERVAL 1 DAY) ";

            ps = con.prepareStatement("SELECT LAST_DAY(?) + INTERVAL 1 DAY - INTERVAL 1 MONTH from dual");
            ps.setDate(1, sqlReportDate);
            log.log(Level.INFO, "Report from: " + DBUtil.exeStringPreparedStatement(ps));
            DBUtil.closeStatement(ps);

            ps = con.prepareStatement("SELECT LAST_DAY(?) + INTERVAL 1 DAY from dual");
            ps.setDate(1, sqlReportDate);
            log.log(Level.INFO, "Report to: " + DBUtil.exeStringPreparedStatement(ps));
            DBUtil.closeStatement(ps);

            log.log(Level.INFO, "Report file: " + file);
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"),true);
            //write XML-Header
            pw.println("Статистика сайту за "+sdfMmYyyy.format(reportDate));
            pw.println("");
            pw.println("Кількість зареєстрованих користувачів:\t\t" + DBUtil.exeLongQuery(con, "select count(*) from user where confirmed = 1"));

            ps = con.prepareStatement("SELECT count(*) FROM sessions WHERE "+strWhereTime + " AND action = ?");
            ps.setDate(1, sqlReportDate);
            ps.setDate(2, sqlReportDate);
            ps.setString(3, "create");

            pw.println("Кількість відвідувачів (загальна):\t\t" + DBUtil.exeLongPreparedStatement(ps));

            ps = con.prepareStatement("SELECT count(distinct ip) FROM sessions WHERE "+strWhereTime + " AND action = ?");
            ps.setDate(1, sqlReportDate);
            ps.setDate(2, sqlReportDate);
            ps.setString(3, "create");

            pw.println("Кількість відвідувачів з різних IP-адрес:\t" + DBUtil.exeLongPreparedStatement(ps));

            DBUtil.closeStatement(ps);
            ps = con.prepareStatement("SELECT count(*) FROM sessions WHERE "+strWhereTime + " AND action in ('emptysearch','revsearch','search','searchwf')");
            ps.setDate(1, sqlReportDate);
            ps.setDate(2, sqlReportDate);
            long searchCount = DBUtil.exeLongPreparedStatement(ps);

            DBUtil.closeStatement(ps);
            ps = con.prepareStatement("SELECT count(*) FROM sessions WHERE "+strWhereTime +  " AND action = ?");
            ps.setDate(1, sqlReportDate);
            ps.setDate(2, sqlReportDate);
            ps.setString(3, "emptysearch");
            long emptySearchCount = DBUtil.exeLongPreparedStatement(ps);

            pw.println("Кількість пошуків в словнику:\t\t\t" + searchCount);
            pw.println("\tз них успішних (щось знайдено):\t\t"+(searchCount - emptySearchCount));
            pw.println("\tнеуспішних (нічого не знайдено):\t" + emptySearchCount);
            pw.println("");

            DBUtil.closeStatement(ps);
            ps = con.prepareStatement("SELECT count(*) FROM sessions WHERE "+strWhereTime +  " AND action = ?");
            ps.setDate(1, sqlReportDate);
            ps.setDate(2, sqlReportDate);
            ps.setString(3, "freqsearch");
            searchCount = DBUtil.exeLongPreparedStatement(ps);
            pw.println("Кількість пошуків в частотному словнику:\t" + searchCount);
            pw.println("");

            DBUtil.closeStatement(ps);
            ps = con.prepareStatement("SELECT count(*) FROM sessions WHERE "+strWhereTime + " AND action = ?");
            ps.setDate(1, sqlReportDate);
            ps.setDate(2, sqlReportDate);
            ps.setString(3, "voctest");
            pw.println("Кількість тестів словникового запасу:\t\t" + DBUtil.exeLongPreparedStatement(ps));

            DBUtil.closeStatement(ps);
            ps = con.prepareStatement("SELECT count(*) FROM sessions WHERE "+strWhereTime + " AND action = ? AND def_lang = ?");
            ps.setDate(1, sqlReportDate);
            ps.setDate(2, sqlReportDate);
            ps.setString(3, "voctest");
            ps.setString(4, "de");
            pw.println("\t\t\t\tde:\t\t" + DBUtil.exeLongPreparedStatement(ps));

            ps.clearParameters();
            ps.setDate(1, sqlReportDate);
            ps.setDate(2, sqlReportDate);
            ps.setString(3, "voctest");
            ps.setString(4, "en");
            pw.println("\t\t\t\ten:\t\t" + DBUtil.exeLongPreparedStatement(ps));

            ps.clearParameters();
            ps.setDate(1, sqlReportDate);
            ps.setDate(2, sqlReportDate);
            ps.setString(3, "voctest");
            ps.setString(4, "uk");
            pw.println("\t\t\t\tuk:\t\t" + DBUtil.exeLongPreparedStatement(ps));
            DBUtil.closeStatement(ps);

            pw.println("");
            pw.println("Статистика німецько-українського словника");
            pw.println("Кількість статей:\t\t\t\t" + DBUtil.exeLongQuery(con, "SELECT count(id) cnt FROM inf WHERE lang = 'de'"));
            pw.println("Кількість перекладів:\t\t\t\t" + DBUtil.exeLongQuery(con, "SELECT count(tr.id) cnt FROM tr, inf WHERE tr.fk_inf = inf.id and inf.lang = 'de'"));
            pw.println("Кількість прикладів:\t\t\t\t"+
                    DBUtil.exeLongQuery(con, "select sum(LENGTH(tr.example) - LENGTH(REPLACE(tr.example, '|', ''))+1) cnt FROM tr, inf " +
                            " WHERE tr.fk_inf = inf.id AND inf.lang = 'de' AND tr.example is not null"));
            pw.println("");
            pw.println("Статистика англійсько-українського словника");
            pw.println("Кількість статей:\t\t\t\t"+DBUtil.exeLongQuery(con, "SELECT count(id) cnt FROM inf WHERE lang = 'en'"));
            pw.println("Кількість перекладів:\t\t\t\t"+DBUtil.exeLongQuery(con, "SELECT count(tr.id) cnt FROM tr, inf WHERE tr.fk_inf = inf.id and inf.lang = 'en'"));
            pw.println("Кількість прикладів:\t\t\t\t"+
                    DBUtil.exeLongQuery(con, "select sum(LENGTH(tr.example) - LENGTH(REPLACE(tr.example, '|', ''))+1) cnt FROM tr, inf " +
                            " WHERE tr.fk_inf = inf.id AND inf.lang = 'en' AND tr.example is not null"));
            pw.println("");
            pw.println("Статистика французько-українського словника");
            pw.println("Кількість статей:\t\t\t\t"+DBUtil.exeLongQuery(con, "SELECT count(id) cnt FROM inf WHERE lang = 'fr'"));
            pw.println("Кількість перекладів:\t\t\t\t"+DBUtil.exeLongQuery(con, "SELECT count(tr.id) cnt FROM tr, inf WHERE tr.fk_inf = inf.id and inf.lang = 'fr'"));
            pw.println("Кількість прикладів:\t\t\t\t"+
                    DBUtil.exeLongQuery(con, "select sum(LENGTH(tr.example) - LENGTH(REPLACE(tr.example, '|', ''))+1) cnt FROM tr, inf " +
                            " WHERE tr.fk_inf = inf.id AND inf.lang = 'fr' AND tr.example is not null"));
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            pw.flush();
            pw.close();
            DBUtil.closeStatement(ps);
            DBUtil.closeConnection(con);
        }
    }
}
