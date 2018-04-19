package org.linguisto.tools.imp.core.dbformat.db;

import java.util.List;

import org.linguisto.tools.imp.core.base.Persistable;


/**
 */
public interface DBWriterFilter {

    boolean filterAddPersistable(Persistable persistable);
    List<Persistable> doAddPersistable();    

}
