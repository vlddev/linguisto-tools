package org.linguisto.tools.imp.core.dbformat.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.linguisto.tools.imp.core.base.Persistable;
import org.linguisto.tools.imp.core.dbformat.db.annotation.DBWriterType;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Table;

/**
 */
public final class DBImporterImpl implements DBImporter {
    private Map<String, DBWriter> insert = new HashMap<String, DBWriter>();
	private final DBWriter[] childrens;

    public DBImporterImpl(
            DBWriter standard,
            DBWriterChildrenCascade cascade,
            DBWriter result) {
        insert.put(DBWriterType.STANDARD.toString(), standard);

        childrens = cascade.getAllDBWriter();

        for (int i = 1;i <= childrens.length;i++)
        	insert.put(DBWriterType.CHILDREN.toString() + i, childrens[i-1]);

        insert.put(DBWriterType.RESULT.toString(), result);
        standard.setParent(this);

        for (int i = 0;i < childrens.length;i++)
        	childrens[i].setParent(this);
        result.setParent(this);
    }

    public void addPersistable(Persistable persistable) throws Exception {
        Table table = persistable.getClass().getAnnotation(Table.class);
        insert.get(toWriterName(table)).addPersistable(persistable);
    }

    public void addPersistables(List<Persistable> persistables) throws Exception {
        if (persistables.size()>0) {
            Table table = persistables.get(0).getClass().getAnnotation(Table.class);
            insert.get(toWriterName(table)).addPersistables(persistables);
        }
    }

    public void close() throws Exception {
        try {
            close(insert.get(DBWriterType.STANDARD.toString()));
            for (int i = 1;i <= childrens.length;i++)
            	close(insert.get(DBWriterType.CHILDREN.toString() + i));
            close(insert.get(DBWriterType.RESULT.toString()));
        }
        catch (Exception e) {
            throw new Exception(e);
        }
    }

    public void insertAll() throws Exception {
        insert.get(DBWriterType.STANDARD.toString()).insertAll();
        for (int i = 1;i <= childrens.length;i++)
        	insert.get(DBWriterType.CHILDREN.toString() + i).insertAll();;
        insert.get(DBWriterType.RESULT.toString()).insertAll();
    }

    public Persistable savePersistable(Persistable persistable) throws Exception {
        Table table = persistable.getClass().getAnnotation(Table.class);
        return insert.get(toWriterName(table)).savePersistable(persistable);
    }

	private String toWriterName(Table table) {
		if (table.dbWriterType().equals(DBWriterType.CHILDREN))
			return table.dbWriterType().toString() + table.dbWriterLevel();
		else
			return table.dbWriterType().toString();
	}

    public Persistable updatePersistable(Persistable persistable) throws Exception {
        Table table = persistable.getClass().getAnnotation(Table.class);
        return insert.get(toWriterName(table)).updatePersistable(persistable);
    }

    private void close(DBWriter dbWriter) throws Exception {
        dbWriter.close();
        while (!dbWriter.isClosed()) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {}
        }
    }
}