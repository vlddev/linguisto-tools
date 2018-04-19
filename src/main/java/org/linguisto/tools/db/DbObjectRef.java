package org.linguisto.tools.db;

public class DbObjectRef {

	public static final int OBJ_NOT_FOUND = -1;
	public static final int OBJ_FOUND = 0;
	public static final int OBJ_CREATED = 1;
	
	int id;
	int state;
	Object obj;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
	
	
}
