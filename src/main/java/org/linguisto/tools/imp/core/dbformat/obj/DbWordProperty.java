package org.linguisto.tools.imp.core.dbformat.obj;

public class DbWordProperty extends BaseDbObj {
	private static final long serialVersionUID = -2940336273233968706L;
	private String pname;
	private String pvalue;
	
	public DbWordProperty(String pname, String pvalue) {
		this.pname = pname;
		this.pvalue = pvalue;
	}
	
	public String getPname() {
		return pname;
	}
	public void setPname(String pname) {
		this.pname = pname;
	}
	public String getPvalue() {
		return pvalue;
	}
	public void setPvalue(String pvalue) {
		this.pvalue = pvalue;
	}
}
