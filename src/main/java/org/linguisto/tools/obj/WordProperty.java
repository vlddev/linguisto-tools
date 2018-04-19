package org.linguisto.tools.obj;

import org.linguisto.tools.imp.core.annotation.FormatField;
import org.linguisto.tools.imp.core.annotation.FormatObject;
import org.linguisto.tools.imp.core.base.BaseObj;
import org.linguisto.tools.imp.core.converter.annotation.StringToString;

@FormatObject( name = "word_prop")
public class WordProperty extends BaseObj {

	private static final long serialVersionUID = -4349830756779396549L;
	
	private final static String TYPE_WORD_FORM = "wf";
	private final static String TYPE_NOTE = "note";
	
	private String type;
	private String pname;
	private String pvalue;

	public WordProperty() {
		this.type = TYPE_WORD_FORM;
	}

	public WordProperty(String pname, String pvalue) {
		this();
		this.pname = pname;
		this.pvalue = pvalue;
	}

	public WordProperty(String type, String pname, String pvalue) {
		this.type = type;
		this.pname = pname;
		this.pvalue = pvalue;
	}

	public String getType() {
		return type;
	}
	@StringToString
    @FormatField(name = "type")
	public void setType(String type) {
		this.type = type;
	}
	
	public String getPname() {
		return pname;
	}
	@StringToString
    @FormatField(name = "pname")
	public void setPname(String pname) {
		this.pname = pname;
	}
	public String getPvalue() {
		return pvalue;
	}
	@StringToString
    @FormatField(name = "pvalue")
	public void setPvalue(String pvalue) {
		this.pvalue = pvalue;
	}

	@Override
	public void reset() {
        super.reset();
    	type = null;
    	pname = null;
    	pvalue = null;
    }
}
