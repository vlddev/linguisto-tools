package org.linguisto.tools.imp.core.base;

public interface Persistable extends Poolable {
	public void setId(Integer id);
    public Integer getId();
    public void setRetention(int retention);
    public boolean isReadyForPersisting();
    public boolean isMarkedForPersisting();
    public boolean wasPersisted();
    public void persist();
    public void persisted();
}