package org.linguisto.tools.imp.xml;

import org.linguisto.tools.db.DbObjectRef;
import org.linguisto.tools.imp.XMLImporter;
import org.linguisto.tools.imp.core.base.Processable;
import org.linguisto.tools.imp.core.base.ProcessingEngine;
import org.linguisto.tools.obj.Header;
import org.linguisto.tools.obj.Inf;
import org.linguisto.tools.db.DbObjectRef;
import org.linguisto.tools.imp.XMLImporter;
import org.linguisto.tools.imp.core.base.Processable;
import org.linguisto.tools.imp.core.base.ProcessingEngine;
import org.linguisto.tools.obj.Header;
import org.linguisto.tools.obj.Inf;

import java.util.logging.Logger;

public class SimpleVDictToDBConvertor implements ProcessingEngine {

    public static final Logger log = Logger.getLogger(SimpleVDictToDBConvertor.class.getName());
    int mergeStrategy;
	SQLDBImporter dbImporter;

	public SimpleVDictToDBConvertor(SQLDBImporter dbImporter, int mergeStrategy) {
		this.dbImporter = dbImporter;
        this.mergeStrategy = mergeStrategy;
	}
	
	@Override
	public void addAttribute(String name, Object value) {
	}

	@Override
	public Object getAttribute(String name) {
		return null;
	}

	@Override
	public void setAllUp() throws Exception {
	}

	@Override
	public void process(Processable processable) throws Exception {
		if (processable instanceof Inf) {
            Inf inf = (Inf)processable;
			//1. check or create de_word
			DbObjectRef infId = dbImporter.getOrCreateInf(inf);

			//2. TODO store de_word_property
			//dbImporter.storeProperties(inf, deWordId.getId());

			//3. store translations
            if (mergeStrategy == XMLImporter.MERGE_STRATEGY_DELETE_EXISTING && infId.getState() == DbObjectRef.OBJ_FOUND) {
                //delete old translations
                log.warning("Delete translation(s) of the word '"+inf.getInf()+"', type = "+inf.getType()+".");
                dbImporter.deleteTranslations(infId.getId());
            }
			dbImporter.storeTranslations(inf, infId.getId());
			
		} else if (processable instanceof Header) {
            Header header = (Header)processable;
            //configure importer's languages
            dbImporter.setLangFrom(header.getLangFrom());
            dbImporter.setLangTo(header.getLangTo());
            dbImporter.init();

        }
	}

	@Override
	public void tearAllDown() throws Exception {
        for(String key : dbImporter.getStatisticMap().keySet() ) {
            System.out.println(key + " -> "+dbImporter.getStatisticMap().get(key));
        }
		dbImporter.commit();
		dbImporter.closeAll();
	}

}
