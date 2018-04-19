package org.linguisto.tools.obj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.linguisto.tools.imp.core.annotation.FormatChild;
import org.linguisto.tools.imp.core.annotation.FormatField;
import org.linguisto.tools.imp.core.annotation.FormatObject;
import org.linguisto.tools.imp.core.converter.annotation.StringToInteger;
import org.linguisto.tools.imp.core.converter.annotation.StringToString;
import org.linguisto.tools.imp.core.base.BaseObj;

@FormatObject( name = "tr", prefixLang=false)
public class Translation extends BaseObj {
	
	private static final long serialVersionUID = -5226206218216871400L;
	private Integer orderNr = 0;
    private Inf infFrom;
	private String translation;
	private String example;
    private String language;


    public Translation() {
    }

    public Translation(Inf infFrom, String translation, String example) {
        setInfFrom(infFrom);
        setTranslation(translation);
        setExample(example);
    }

    public Inf getInfFrom() {
        return infFrom;
    }

    public void setInfFrom(Inf infFrom) {
        this.infFrom = infFrom;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String lang) {
        this.language = lang;
    }

    public int getOrderNr() {
        return orderNr;
    }

    @StringToInteger
    @FormatField(name = "nr")
    public void setOrderNr(Integer orderNr) {
        this.orderNr = orderNr;
    }

    public List<String> getExamples() {
        if (example != null) {
            String[] exampleList = example.split("\\|");
            return Arrays.asList(exampleList);
        } else {
            return new ArrayList<String>();
        }
    }

    public String getTranslation() {
        return translation;
    }

    @StringToString
    @FormatField(name = "txt")
    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String context) {
        this.example = context;
    }

    @StringToString
    @FormatField(name = "ex")
    public void addExample(String ex) {
        if ( this.example == null) {
            this.example = ex;
        } else {
            this.example = this.example + " | " + ex;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        boolean ret = false;
        if (obj instanceof Translation) {
            Translation otherObj = (Translation)obj;
            ret = new EqualsBuilder().
                    // if deriving: appendSuper(super.equals(obj)).
                            append(getInfFrom().getId(), otherObj.getInfFrom().getId()).
                    append(getInfFrom().getInf(), otherObj.getInfFrom().getInf()).
                    append(getTranslation(), otherObj.getTranslation()).
                    append(getExample(), otherObj.getExample()).
                    isEquals();
        }
        return ret;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                append(getInfFrom().getId()).
                append(getInfFrom().getInf()).
                append(getTranslation()).
                append(getExample()).
                toHashCode();
    }

    @Override
	public void reset() {
        super.reset();
    	orderNr = 0;
    	infFrom = null;
    	translation = null;
    	example = null;
    }

}

