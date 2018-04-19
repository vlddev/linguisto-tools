package org.linguisto.tools.obj;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.linguisto.tools.imp.core.annotation.FormatField;
import org.linguisto.tools.imp.core.annotation.FormatObject;
import org.linguisto.tools.imp.core.base.BaseObj;
import org.linguisto.tools.imp.core.converter.annotation.StringToString;

@FormatObject( name = "header", topLevel = true)
public class Header extends BaseObj {

	private String langFrom;
    private String langTo;

    public String getLangFrom() {
        return langFrom;
    }

    @StringToString
    @FormatField(name = "lang.from")
    public void setLangFrom(String langFrom) {
        this.langFrom = langFrom;
    }

    public String getLangTo() {
        return langTo;
    }

    @StringToString
    @FormatField(name = "lang.to")
    public void setLangTo(String langTo) {
        this.langTo = langTo;
    }

    @Override
	public void reset() {
        super.reset();
        langFrom = null;
        langTo = null;
    }
}
