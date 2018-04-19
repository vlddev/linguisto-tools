package org.linguisto.tools.imp.core.dbformat.db;

public interface DBWriter extends DBImporter {
    void setParent(DBImporter writer);
    boolean isClosed();
}
