package org.linguisto.tools.imp.xml;

import org.junit.BeforeClass;
import org.junit.Test;
import org.linguisto.tools.db.Constants;
import org.linguisto.tools.db.DBUtil;
import org.linguisto.tools.db.DbObjectRef;
import org.linguisto.tools.obj.*;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLDBImporterTest extends junit.framework.Assert {
	
	private static SQLDBImporter dbImporter = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Connection con = DBUtil.getConnection(System.getProperty(Constants.SYS_JDBC_USER),
				System.getProperty(Constants.SYS_JDBC_PASSWORD),
				System.getProperty(Constants.SYS_JDBC_URL),
				System.getProperty(Constants.SYS_JDBC_DRIVER));
		con.setAutoCommit(false);
		dbImporter = new SQLDBImporter(con);
		testCreateTestSet();
	}

	@Test
	public void testGetDeWord() throws SQLException {
		assertEquals(1, dbImporter.getInf("Land", 4).getId());
		assertEquals(2, dbImporter.getInf("achtzig", 9).getId());
	}

	@Test
	public void testGetOrCreateDeWord() throws SQLException {
		Inf deWord = new Inf();
		deWord.setInf("Land");
		deWord.setType(4);
		DbObjectRef ret = dbImporter.getOrCreateInf(deWord);
	}

	//@Test
	public void testRollback() throws SQLException {
        Inf deWord = new Inf();
		deWord.setInf("Rollback");
		deWord.setType(4);
		DbObjectRef ret = dbImporter.getOrCreateInf(deWord);
		dbImporter.rollback();
	}
	
	//@Test
	public static void testCreateTestSet() throws SQLException {
		//create de_words 
        Inf deWordLand = new Inf();
		deWordLand.setInf("Land");
		deWordLand.setType(4);

		//deWordLand.addWordProperty(new WordProperty("1", "Land (Landes)"));
		//deWordLand.addWordProperty(new WordProperty("2", "Länder"));
		
		Translation tr = new Translation();
		tr.setTranslation("земля");
		tr.setExample("Mein Land");
		tr.setOrderNr(1);
		
		deWordLand.addTranslation(tr);
		
		DbObjectRef deLand = dbImporter.getOrCreateInf(deWordLand);
		
		//dbImporter.storeProperties(deWordLand, deLand.getId());
		dbImporter.storeTranslations(deWordLand, deLand.getId());

	}
}
