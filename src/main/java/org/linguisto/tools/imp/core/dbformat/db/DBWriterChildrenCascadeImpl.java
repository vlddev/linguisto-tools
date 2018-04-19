package org.linguisto.tools.imp.core.dbformat.db;

import javax.sql.DataSource;

import org.linguisto.tools.imp.core.base.ObjectFactory;
import org.linguisto.tools.imp.core.AnnotationCache;
import org.linguisto.tools.imp.core.base.ObjectFactory;

public class DBWriterChildrenCascadeImpl implements DBWriterChildrenCascade {
	private final DBWriter[] writers;

	public DBWriterChildrenCascadeImpl(DataSource dataSource,
			DBHelper dbHelper, SQLHelper sqlHelper, ObjectFactory objectFactory,
			AnnotationCache annotationCache, int threadSize, int queueSize,
			String namePrefix, DBWriterFilter filter, int numberOfChildren) throws Exception {
		writers = new DBWriter[numberOfChildren];

		for (int i = 0;i < numberOfChildren;i++)
			writers[i] = new DBWriterImpl(dataSource, dbHelper, sqlHelper, objectFactory,
				annotationCache, threadSize, queueSize, namePrefix + (i+1), filter);
	}

	public DBWriter[] getAllDBWriter() {
		return writers;
	}
}