package org.linguisto.tools.imp.core.dbformat.db;

import java.util.List;

import org.linguisto.tools.imp.core.base.Persistable;
import org.linguisto.tools.imp.core.base.Persistable;

/**
 */
public interface DBImporter {
    Persistable savePersistable(Persistable persistable) throws Exception;
    Persistable updatePersistable(Persistable persistable) throws Exception;
    void addPersistable(Persistable persistable) throws Exception;
    void addPersistables(List<Persistable> persistables) throws Exception;
    void insertAll() throws Exception;
    void close() throws Exception;
}
