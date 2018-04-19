package org.linguisto.tools.imp.core.base;


public abstract class BaseObj implements Poolable, Processable {
	private static final long serialVersionUID = -2897704993375942243L;

	protected Integer id;
	private boolean inUse = true;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

    public boolean inUse() {
        return inUse;
    }

    public void activate() {
        inUse = true;
    }

    public boolean validate() {
        return true;
    }
	
    public void reset() {
        inUse = false;
        id = null;
    }
}
