package org.linguisto.tools.imp.core.dbformat.obj;

public class DbTrExample extends BaseDbObj {
	private static final long serialVersionUID = -7199047230141092384L;
	private int frOrder;
	private String origFrase;
	private String trFrase;
	private String desc;
	public int getFrOrder() {
		return frOrder;
	}
	public void setFrOrder(int frOrder) {
		this.frOrder = frOrder;
	}
	public String getOrigFrase() {
		return origFrase;
	}
	public void setOrigFrase(String origFrase) {
		this.origFrase = origFrase;
	}
	public String getTrFrase() {
		return trFrase;
	}
	public void setTrFrase(String trFrase) {
		this.trFrase = trFrase;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
}
