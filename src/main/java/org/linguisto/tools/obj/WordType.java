package org.linguisto.tools.obj;

import org.linguisto.tools.imp.core.base.BaseObj;

public class WordType extends BaseObj {

	private static final long serialVersionUID = 1343525224868617609L;

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

	@Override
	public void reset() {
        super.reset();
        desc = null;
        comment = null;
    }
}
