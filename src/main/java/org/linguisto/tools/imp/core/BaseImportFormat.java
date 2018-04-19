package org.linguisto.tools.imp.core;

import org.linguisto.tools.imp.core.base.BaseObj;
import org.linguisto.tools.imp.core.base.ObjectFactory;
import org.linguisto.tools.imp.core.base.Poolable;
import org.linguisto.tools.imp.core.dbformat.FormatEngine;
import org.linguisto.tools.imp.core.base.BaseObj;
import org.linguisto.tools.imp.core.base.ObjectFactory;
import org.linguisto.tools.imp.core.base.Poolable;
import org.linguisto.tools.imp.core.dbformat.FormatEngine;

public abstract class BaseImportFormat implements ImportFormat {
	protected ObjectFactory objectFactory;
	protected FormatEngine formatEngine;

	public BaseImportFormat(FormatEngine formatEngine, ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
		this.formatEngine = formatEngine;
	}
	
	public void returnAndSaveImportObject(ImportObject object) throws Exception {
		try {
			formatEngine.process(object);
		} catch (Exception e) {
			throw new Exception("Error while saving and returning object.", e);
		}
	}
	
	public void returnAndSaveImportObject(ImportObject object, boolean sync) throws Exception {
		try {
			formatEngine.process(object, sync);
		} catch (Exception e) {
			throw new Exception("Error while saving and returning object.", e);
		}
	}

	public void returnObject(BaseObj object) throws Exception {
	    try {
	        if (object instanceof Poolable) {
	            objectFactory.returnObject((Poolable)object);
	        }
	    } catch (Exception e) {
	        throw new Exception("Error while returning poolable.",e);
	    }
	}
}
