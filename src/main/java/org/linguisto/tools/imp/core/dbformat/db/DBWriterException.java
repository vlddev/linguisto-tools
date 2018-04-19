package org.linguisto.tools.imp.core.dbformat.db;

public final class DBWriterException extends Exception {

	private static final long serialVersionUID = 6172061270770338340L;

	public DBWriterException() {}

    public DBWriterException(Throwable cause) {
        super(cause);
    }

    public DBWriterException(String message) {
        super(message);
    }

    public DBWriterException(String message, Throwable cause) {
        super(message, cause);
    }
}