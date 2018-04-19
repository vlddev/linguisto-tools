package org.linguisto.tools.imp.core.dbformat.obj;

public class DbWordType extends BaseDbObj {
	private static final long serialVersionUID = -7773635880379306968L;
	private String desc;
	private String comment;
	
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
}
