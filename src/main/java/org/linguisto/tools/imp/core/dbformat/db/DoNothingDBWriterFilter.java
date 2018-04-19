package org.linguisto.tools.imp.core.dbformat.db;

import java.util.List;
import java.util.ArrayList;

import org.linguisto.tools.imp.core.base.Persistable;
import org.linguisto.tools.imp.core.base.Persistable;

/**
 */
public class DoNothingDBWriterFilter implements DBWriterFilter {

    public List<Persistable> doAddPersistable() {
        return new ArrayList<Persistable>();
    }

    public boolean filterAddPersistable(Persistable persistable) {
        return false;
    }
}
