package org.linguisto.tools.imp;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.linguisto.tools.db.Constants;
import org.linguisto.tools.db.DBUtil;
import org.linguisto.tools.imp.core.FormatFieldMapperImpl;
import org.linguisto.tools.imp.core.FormatObjectFactoryImpl;
import org.linguisto.tools.imp.core.base.ObjectFactoryImpl;
import org.linguisto.tools.imp.core.base.ProcessingEngine;
import org.linguisto.tools.imp.core.converter.FieldsConverterImpl;
import org.linguisto.tools.db.Constants;
import org.linguisto.tools.db.DBUtil;
import org.linguisto.tools.imp.core.AnnotationCacheImpl;
import org.linguisto.tools.imp.core.FormatFieldMapperImpl;
import org.linguisto.tools.imp.core.FormatObjectFactoryImpl;
import org.linguisto.tools.imp.core.FormatReader;
import org.linguisto.tools.imp.core.base.ObjectFactoryImpl;
import org.linguisto.tools.imp.core.base.ProcessingEngine;
import org.linguisto.tools.imp.core.converter.FieldsConverterImpl;
import org.linguisto.tools.imp.xml.SQLDBImporter;
import org.linguisto.tools.imp.xml.SimpleVDictToDBConvertor;

import javax.xml.transform.TransformerException;

/**
 * Import jsfdict-XML to jsfdict database.
 * Call must looks like:
 * java ... XMLImporter -Djdbc.driver=com.mysql.jdbc.Driver -Djdbc.url=jdbc:mysql://localhost/vdict?useUnicode=true&characterEncoding=UTF-8
 *                      -Djdbc.user=[user] -Djdbc.password=[pwd] [import file]
 */
public class XMLImporter {

    /**
     * delete existing translations
     */
    public static final int MERGE_STRATEGY_DELETE_EXISTING = 1;

    /**
     * merge existing with imported
     */
    public static final int MERGE_STRATEGY_MERGE_ALL = 2;

    private FormatReader reader;
	
	public XMLImporter(FormatReader reader) {
		this.reader = reader;
	}
	
	public void importDict(String file) throws Exception {
		reader.readFile(file);
	}
	
	public static void main(String[] args) throws Exception {
		//DictLogger.setup();
		String sImpFile = "vlad.vdict.xml";
		if (args.length > 0) {
			sImpFile = args[0];
		} else {
			throw new Exception("First param must be the name of import file");
		}

        int mergeStrategy = MERGE_STRATEGY_DELETE_EXISTING;

		AnnotationCacheImpl annotationCache = new AnnotationCacheImpl();
		FieldsConverterImpl fieldsConverter = new FieldsConverterImpl(null, null);
		
		// List all used classes
		ObjectFactoryImpl objectFactory = new ObjectFactoryImpl(
				(List<String>)Arrays.asList(
                        "Header",
                        "Inf",
						"WordForm",
						"WordProperty",
						"Translation",
						"WordType"));
		
		//ProcessingEngineImpl processingEngine = new ProcessingEngineImpl();
		Connection con = DBUtil.getConnection(System.getProperty(Constants.SYS_JDBC_USER),
				System.getProperty(Constants.SYS_JDBC_PASSWORD),
				System.getProperty(Constants.SYS_JDBC_URL),
				System.getProperty(Constants.SYS_JDBC_DRIVER));
		con.setAutoCommit(false);
		ProcessingEngine processingEngine = new SimpleVDictToDBConvertor(new SQLDBImporter(con), mergeStrategy);
        FormatFieldMapperImpl formatFieldMapper = new FormatFieldMapperImpl(annotationCache, fieldsConverter);
        FormatObjectFactoryImpl formatObjectFactory = new FormatObjectFactoryImpl(objectFactory, annotationCache);

		
		DictFormatReaderImpl reader = new DictFormatReaderImpl( processingEngine, formatObjectFactory,
	            formatFieldMapper /*, objectFactory*/);

        //DTD to check input file
        //TODO use local file vlad/lsfdict/jsfdict.dtd
        String dtdFile = "/home/vlad/Dokumente/my_dev/de_uk/jsfdict.dtd";
        reader.validateXml(sImpFile, dtdFile);

		XMLImporter xmlImporter = new XMLImporter(reader);
		xmlImporter.importDict(sImpFile);
		System.exit(0);
	}
	
}
