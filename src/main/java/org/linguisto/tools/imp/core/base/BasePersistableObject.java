package org.linguisto.tools.imp.core.base;

public abstract class BasePersistableObject extends BaseObj implements Persistable {

	private static final long serialVersionUID = 3366767671830882176L;

	private boolean isMarkedForPersisting;
    private boolean wasPersisted;
    protected long id;
    private long retention = 1;
    private boolean retentionSet = false;

    public void setRetention(int retention) {
    	if (!retentionSet) {
    		this.retention = retention;
    		retentionSet = true;
    	}
    }

    public synchronized boolean isReadyForPersisting() {
    	return (--this.retention < 1);
    }

	public boolean isMarkedForPersisting() {
        return isMarkedForPersisting;
    }

    public boolean wasPersisted() {
        return wasPersisted;
    }

    public void persist() {
        isMarkedForPersisting = true;
    }

    public void persisted() {
        isMarkedForPersisting = false;
        wasPersisted = true;
    }

    @Override
	public void reset() {
        super.reset();
        isMarkedForPersisting = false;
        wasPersisted = false;
        retentionSet = false;
        retention = 1;
    }
}
