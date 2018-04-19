package org.linguisto.tools.imp.core;

import org.linguisto.tools.imp.core.base.ObjectFactory;
import org.linguisto.tools.imp.core.dbformat.FormatEngine;
import org.linguisto.tools.imp.core.dbformat.obj.DbDeWord;
import org.linguisto.tools.imp.core.dbformat.obj.DbTranslation;

public class VDictDbFormat extends BaseImportFormat {

	public VDictDbFormat(FormatEngine formatEngine, ObjectFactory objectFactory) {
		super(formatEngine, objectFactory);
	}
	
    public DbDeWord createDbWord() throws Exception {
        try {
        	DbDeWord obj = (DbDeWord)objectFactory.borrowObject(DbDeWord.class);
        	//TODO fill IDs and other initial data
            return obj;
        } catch (Exception e) {
            throw new Exception("Error while creating DbWord.",e);
        }
    }

    public DbTranslation addDbTranslation(DbDeWord word) throws Exception {
        try {
        	DbTranslation obj = (DbTranslation)objectFactory.borrowObject(DbTranslation.class);
            word.addTranslation(obj);
            return obj;
        } catch (Exception e) {
            throw new Exception("Error while creating DbTranslation.",e);
        }
    }
    
    //TODO implement other methods
    
}
