package org.linguisto.tools.imp.core.dbformat.db;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.linguisto.tools.imp.core.base.BasePersistableObject;
import org.linguisto.tools.imp.core.base.ObjectFactory;
import org.linguisto.tools.imp.core.base.Persistable;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Depending;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Join;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Table;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Depending;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Join;
import org.linguisto.tools.imp.core.AnnotationCache;
import org.linguisto.tools.imp.core.base.BasePersistableObject;
import org.linguisto.tools.imp.core.base.ObjectFactory;
import org.linguisto.tools.imp.core.base.Persistable;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Table;


@SuppressWarnings("unchecked")
public final class DBWriterImpl implements DBWriter {

	public static final Logger logger = Logger.getLogger(DBWriterImpl.class.getName());

    private static final PoisonPill POISON_PILL = new PoisonPill();

	@SuppressWarnings("rawtypes")
	private final Map<Class, List<Persistable>> persistables = new ConcurrentHashMap<Class, List<Persistable>>();
//	@SuppressWarnings("rawtypes")
	//private final Map<Class, DataSource> datasources = new HashMap<Class, DataSource>();
    private final List<WorkerThread> workerThreads = new ArrayList<WorkerThread>();
    private Exception error;

    private DBHelper dbHelper;
    private SQLHelper sqlHelper;
    private DataSource dataSource;
    private ObjectFactory objectFactory;
    private DBImporter parent;
    private DBWriterFilter filter;

    private final BlockingQueue<List<Persistable>> queue;

    private AnnotationCache annotationCache;

    public DBWriterImpl (
            DataSource dataSource,
            DBHelper dbHelper,
            SQLHelper sqlHelper,
            ObjectFactory objectFactory,
            AnnotationCache annotationCache,
            int threadSize,
            int queueSize,
            String name,
            DBWriterFilter filter) throws Exception {
        this.sqlHelper = sqlHelper;
        this.dbHelper = dbHelper;
        this.dataSource = dataSource;
        this.objectFactory = objectFactory;
        this.annotationCache = annotationCache;
        queue = new LinkedBlockingQueue<List<Persistable>>(queueSize);
        for (int i=0;i<threadSize;i++) {
            WorkerThread workerThread = new WorkerThread("Worker"+name+"["+i+"]", queue);
            workerThread.start();
            workerThreads.add(workerThread);
        }
        this.filter = filter;
    }

    private final Object persistableMutex = new Object();

    public void addPersistable(Persistable persistable) throws DBWriterException {
        addPersistable(persistable, true);
    }

    private void addPersistable(Persistable persistable, boolean doFilter) throws DBWriterException {
        if (isError()) throw new DBWriterException(error);
        try {
            if (persistable.isMarkedForPersisting()) {
                if (doFilter) {
                    if (!filter.filterAddPersistable(persistable)) {
                        doPersist(persistable);
                    }
                } else {
                    doPersist(persistable);
                }
            } else {
                try {
                    objectFactory.returnObject(persistable);
                } catch (Exception e) {
                    // do nothing
                	logger.log(Level.WARNING, "Exception by objectFactory.returnObject()", e);
                }
            }
        } catch (Exception e) {
            throw new DBWriterException(e);
        }
    }

    private void doPersist(Persistable persistable) throws Exception {
        synchronized (persistableMutex) {
            List<Persistable> list = persistables.get(persistable.getClass());
            if (list!=null) {
                    list.add(persistable);
            } else {
                    list = new LinkedList<Persistable>();
                    list.add(persistable);
                    persistables.put(persistable.getClass(), list);
            }
            Table table = persistable.getClass().getAnnotation(Table.class);
            if (list.size()>=table.insertSize())
                insert(persistable.getClass());
        }
    }


    public void addPersistables(List<Persistable> persistables) throws DBWriterException {
        addPersistables(persistables, true);
    }

    private void addPersistables(List<Persistable> persistables, boolean doFilter) throws DBWriterException {
        if (isError()) throw new DBWriterException(error);
        for (Persistable persistable : persistables) {
            addPersistable(persistable, doFilter);
        }
    }

    public void savePersistables(List<Persistable> persistables) throws DBWriterException {
        for (Persistable persistable : persistables) {
            savePersistable(persistable);
        }
    }

    @SuppressWarnings("rawtypes")
	public synchronized void insertAll() throws DBWriterException{
        if (isError()) throw new DBWriterException(error);
        try {
            List<Class> list = new ArrayList<Class>(persistables.keySet());
                for (Class<?> aClass : list) {
                    if (persistables.get(aClass).size() > 0) {
                        insert(aClass);
                    }
                }
        } catch (Exception e) {
            throw new DBWriterException(e);
        }
    }

    public void close() throws DBWriterException {
        addPersistables(filter.doAddPersistable(), false);
        insertAll();

        for (int i = 0;i < workerThreads.size();i++)
            addPersistable(POISON_PILL);

        if (isError()) throw new DBWriterException(error);
    }

    public boolean isClosed() {
        for (WorkerThread workerThread : workerThreads) {
            if (!workerThread.isPoisoned()) return false;
        }
        return true;
    }

    public void setParent(DBImporter importer) {
        this.parent = importer;
    }

    public int getWorkerThreads() {
        return workerThreads.size();
    }

    public Persistable savePersistable(Persistable persistable) throws DBWriterException {
        Connection con = null;
        PreparedStatement prep = null;
        DataSource dataSource = null;
        try {
            dataSource = getDataSource(persistable);
            sqlHelper.retrieveId(dataSource, persistable);
            con = dataSource.getConnection();
            prep = con.prepareStatement(sqlHelper.createInsertStatement(persistable));
            fillInsertPreparedStatement(prep, persistable);
            prep.executeUpdate();
            con.commit();
            persistable.persisted();
            return persistable;
        } catch (Exception e) {
            throw new DBWriterException(e);
        } finally {
            dbHelper.closePreparedStatement(prep);
            dbHelper.closeConnection(con);
        }
    }

    public Persistable updatePersistable(Persistable persistable) throws DBWriterException {
        Connection con = null;
        PreparedStatement prep = null;
        DataSource dataSource = null;
        try {
            dataSource = getDataSource(persistable);
            con = dataSource.getConnection();
            prep = con.prepareStatement(sqlHelper.createUpdateStatement(persistable));
            fillUpdatePreparedStatement(prep, persistable);
            prep.executeUpdate();
            con.commit();
            return persistable;
        } catch (Exception e) {
            throw new DBWriterException(e);
        } finally {
            dbHelper.closePreparedStatement(prep);
            dbHelper.closeConnection(con);
        }
    }

    private void insert(Class<?> theClass) throws Exception {
        Table table = theClass.getAnnotation(Table.class);
        List<Persistable> toPersist = new ArrayList<Persistable>();

        List<Persistable> saved = persistables.get(theClass);
        int max = 0;
        synchronized (persistableMutex) {
            if (saved.size()>=table.insertSize()) {
                max = table.insertSize();
            } else {
                max = saved.size();
            }
            for (int i = 0; i < max; i++) {
                Persistable persistable = saved.get(i);
                toPersist.add(persistable);
            }
            saved.subList(0,max).clear();
        }
        queue.put(toPersist);
    }

    private class WorkerThread extends Thread {

        private String id;
        private boolean isPoisoned = false;
        private BlockingQueue<List<Persistable>> myQueue;
        private final Logger log;

        public WorkerThread(String id, BlockingQueue<List<Persistable>> queue) throws Exception {
        	super(id);
            this.id = id;
            log = Logger.getLogger(id);
            this.myQueue = queue;
        }

        public boolean isPoisoned() {
            return isPoisoned;
        }

        @Override
		public void run() {
            log.info("Started");
//            StopWatch stopWatch = StopWatchFactory.createInstance(Logger.getLogger(getName()));
//            stopWatch.start();
            Connection con = null;
            while (true) {
                List<Persistable> persistables = null;
                try {
//                    stopWatch.pause();
                    persistables = myQueue.take();
//                    stopWatch.resume();
                }
                catch (InterruptedException e) {
                    log.log(Level.SEVERE, "Closed due to interrupt!", e);
//                    stopWatch.stop();
                    return;
                }
                if (persistables == null || persistables.size()==0) continue;
                if (persistables.get(0).equals(POISON_PILL)) {
                    isPoisoned = true;
                    log.info("Thread["+this.id+"] closed");
//                    stopWatch.stop();
                    return;
                }
                PreparedStatement prep = null;
                String sql = null;
                Table table = null;
                long maxId = -1;
                long id = -1;
                long maxSequenceRead = 5;

                try {
                    sql = sqlHelper.createInsertStatement(persistables.get(0));
                    table = persistables.get(0).getClass().getAnnotation(Table.class);
                    con = getDataSource(persistables.get(0)).getConnection();

                    while (id < 1 && maxSequenceRead > 0) {
	                    maxId = dbHelper.getNextValue(dataSource, table.seq());
	                    id = maxId - (table.insertSize()-1);
	                    maxSequenceRead--;

	                    if (id < 1)
	                    	log.warning("Sequence returned negative value: " + id + ", trying again.");
                    }
                    if (id < 1 && maxSequenceRead == 0) {
                    	throw new SQLException("Sequence only retrieves negative numbers 5 times. Check your sequence configuration.");
                    }

                    prep = con.prepareStatement(sql);
                    for (Persistable persistable : persistables) {
                    	persistable.setId((int)(id++));
                        fillInsertPreparedStatement(prep, persistable);
                        prep.addBatch();
                        prep.clearParameters();
                    }
                    prep.executeBatch();

                    con.commit();
                    for (Persistable persistable : persistables) {
                        addJoinedPersistables(persistable);
                        addDependingPersistables(persistable);

                        try {
                        	objectFactory.returnObject(persistable);
                        } catch (Exception e) {
                            error = e;
                        }
                    }

                    persistables.clear();
                } catch (Exception e) {
                    log.log(Level.SEVERE, "An error occurred!", e);

                    if (sql != null) {
                        log.severe("Thread["+this.id+"] SQL Statement: " + sql);
                    }

                    error = e;
                } finally {
                    dbHelper.closePreparedStatement(prep);
                    dbHelper.closeConnection(con);
                }
            }
        }
    }

    private void fillInsertPreparedStatement(PreparedStatement prep, Persistable persistable) throws Exception {
        int columns = sqlHelper.getInsertColumnSize(persistable);
        for(int j=0; j<columns;j++) {
            Object value = sqlHelper.getSQLValue(SQLHelper.Action.INSERT, j, persistable);
            int type = sqlHelper.getSQLType(SQLHelper.Action.INSERT, j, persistable);
            if (value == null && type == Types.DATE) {
                prep.setNull(j+1, type);
            } else {
                prep.setObject(j+1, value, type);
            }
        }
    }

    private void fillUpdatePreparedStatement(PreparedStatement prep, Persistable persistable) throws Exception {
        int columns = sqlHelper.getUpdateColumnSize(persistable);
        for(int j=0; j<columns;j++) {
            Object value = sqlHelper.getSQLValue(SQLHelper.Action.UPDATE, j, persistable);
            int type = sqlHelper.getSQLType(SQLHelper.Action.UPDATE, j, persistable);
            if (value == null && type == Types.DATE) {
                prep.setNull(j+1, type);
            } else {
                prep.setObject(j+1, value, type);
            }
        }
        prep.setLong(columns+1, persistable.getId());
    }

	private void addJoinedPersistables(Persistable persistable) throws Exception {
        List<Method> methods = annotationCache.getAnnotatedMethods(persistable.getClass(), Join.class);
        for (Method method : methods) {
            Join join = method.getAnnotation(Join.class);
            List<Persistable> persistables = (List<Persistable>) method.invoke(persistable, new Object[]{});
            if (persistables != null && persistables.size()>0) {
                String setMethod = "set" + join.name();
                List<Persistable> joinedPersistables = new ArrayList<Persistable>();
                for (Persistable persistable1 : persistables) {
                	persistable1.getClass().getMethod(setMethod, Long.TYPE).invoke(persistable1, persistable.getId());
                    if (persistable1.isReadyForPersisting()) {
                    	persistable1.persist();
	                    joinedPersistables.add(persistable1);
                	}
                }
                parent.addPersistables(joinedPersistables);
            }
        }
    }

	private void addDependingPersistables(Persistable persistable) throws Exception {
        List<Method> methods = annotationCache.getAnnotatedMethods(persistable.getClass(), Depending.class);
        for (Method method : methods) {
            List<Persistable> persistables = (List<Persistable>) method.invoke(persistable, new Object[]{});
            List<Persistable> dependentPersistables = new ArrayList<Persistable>();
            if (persistables != null && persistables.size()>0) {
            	for (Persistable persistable1 : persistables) {
                	if (persistable1.isReadyForPersisting()) {
                		persistable1.persist();
                		dependentPersistables.add(persistable1);
                	}
            	}
                parent.addPersistables(dependentPersistables);
            }
        }
    }

    private DataSource getDataSource(Persistable persistable) {
    	return dataSource;
//        DataSource dataSource = datasources.get(persistable.getClass());
//        if (dataSource != null) return dataSource;
//        if (persistable.getClass().getAnnotation(Table.class).schema().equals(Schema.IMEX_IMPORTER)) {
//            datasources.put(persistable.getClass(), dataSource);
//            return dataSource;
//        }
//        if (persistable.getClass().getAnnotation(Table.class).schema().equals(Schema.IMEX_STAGE)) {
//            datasources.put(persistable.getClass(), imex_stage);
//            return imex_stage;
//        }
//        throw new RuntimeException("Unknown DataSource for persistable "+persistable.getClass().getName());
    }

    @Table(name = "", insertSize = 1)
    private static class PoisonPill extends BasePersistableObject {

		private static final long serialVersionUID = 3529470094365845456L;

		@Override
		public boolean isMarkedForPersisting() {
            return true;
        }
    }

    private boolean isError() {
        return error != null;
    }
    
    public void setSequenceToMaxId() throws DBWriterException {
    	ResultSet rs = null;
    	Connection con = null;
        synchronized (persistableMutex) {
	    	try {
	    		long maxId = -1;
	    		con = dataSource.getConnection();
	    		//Increment PacketId Sequence  
	    		rs = con.createStatement().executeQuery("SELECT max(stage_pkt_id) FROM t_import_packages WHERE target = 'MARS'");
	    		if (rs.next()) {
	    			maxId = rs.getLong(1);
	    		}
	    		rs.close();
	    		long curId = dbHelper.getNextValue(dataSource, "PACKETS_SQ");
	    		if (curId < maxId) {
	        		logger.info("Sequence PACKETS_SQ must be set to " + maxId + ". Current value is "+curId);
	        		while (curId < maxId) {
	        			curId = dbHelper.getNextValue(dataSource, "PACKETS_SQ");
	        			if (curId%100 == 0) {
	        	    		logger.info("Sequence PACKETS_SQ set to " + curId);
	        			}
	        		};
	        		logger.info("Sequence PACKETS_SQ was successfully set to " + curId);
	    		}
	
	    		//Increment PacketItemId Sequence
	    		rs = con.createStatement().executeQuery(
	    				"SELECT max(imf.stage_pktitm_id) " +
	    				"FROM t_import_files imf, t_import_packages imp " +
	    				"WHERE imp.target = 'MARS' AND imp.id = imf.import_pkg_id");
	    		if (rs.next()) {
	    			maxId = rs.getLong(1);
	    		}
	    		curId = dbHelper.getNextValue(dataSource, "PACKITEM_SQ");
	    		if (curId < maxId) {
		    		logger.info("Sequence PACKITEM_SQ must be set to " + maxId + ". Current value is "+curId);
		    		while (curId < maxId) {
		    			curId = dbHelper.getNextValue(dataSource, "PACKITEM_SQ");
		    			if (curId%100 == 0) {
		    	    		logger.info("Sequence PACKITEM_SQ set to " + curId);
		    			}
		    		};
		    		logger.info("Sequence PACKITEM_SQ was successfully set to " + curId);
	    		}
			} catch (Exception e) {
				throw new DBWriterException("Error by incrementing sequences.", e);
			} finally {
				dbHelper.closeResultSet(rs);
				dbHelper.closeConnection(con);
			}
        }
    }
}