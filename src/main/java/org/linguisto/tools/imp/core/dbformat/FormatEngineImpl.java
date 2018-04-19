package org.linguisto.tools.imp.core.dbformat;

import org.linguisto.tools.imp.core.ImportObject;
import org.linguisto.tools.imp.core.base.ProcessingEngine;
import org.linguisto.tools.imp.core.ImportObject;
import org.linguisto.tools.imp.core.base.ProcessingEngine;
import org.linguisto.tools.imp.core.dbformat.db.DBImporter;


public class FormatEngineImpl implements FormatEngine {
    protected ProcessingEngine processingEngine;
    protected DBImporter dbImporter;

    public FormatEngineImpl(
            DBImporter writer,
            ProcessingEngine processingEngine) {
        this.dbImporter = writer;
        this.processingEngine = processingEngine;
    }

    public void process(ImportObject importObject, boolean synchron) throws Exception {
        processObject(importObject);
        persistObject(importObject, synchron);
    }

    public void update(ImportObject ginfObject) throws Exception {
        persistObject(ginfObject, true);
    }

    protected void processObject(ImportObject importObject) throws Exception {
        try {
            processingEngine.process(importObject);
        } catch (Exception e) {
            throw new Exception("Error while processing object.", e);
        }            
    }

    protected void persistObject(ImportObject importObject, boolean synchron) throws Exception {
        // Persistieren
        importObject.persist();

        try {
            if (!synchron) {
                dbImporter.addPersistable(importObject);
            }
            else {
                if (!importObject.wasPersisted()) {
                    dbImporter.savePersistable(importObject);
                } else {
                    dbImporter.updatePersistable(importObject);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error while persisting object.", e);
        }
    }

    public void processComplete() throws Exception{
        try {
            dbImporter.insertAll();
        } catch (Exception e) {
            throw new Exception("Error while completing the process.", e);
        }
    }

    public void close() throws Exception {
        try {
            dbImporter.close();
        } catch (Exception e) {
            throw new Exception("Error while closing the connection.", e);
        }
        try {
			processingEngine.tearAllDown();
		} catch (Exception e) {
            throw new Exception("Error while processing file.", e);
		}
    }

    public void process(ImportObject importObject) throws Exception {
        process(importObject, false);
    }
    
}