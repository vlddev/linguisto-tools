package org.linguisto.tools.obj;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.linguisto.tools.imp.core.annotation.FormatChild;
import org.linguisto.tools.imp.core.annotation.FormatField;
import org.linguisto.tools.imp.core.annotation.FormatObject;
import org.linguisto.tools.imp.core.base.BaseObj;
import org.linguisto.tools.imp.core.converter.annotation.StringToInteger;
import org.linguisto.tools.imp.core.converter.annotation.StringToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@FormatObject( name = "word", topLevel = true)
public class Inf extends BaseObj {

	private static final long serialVersionUID = 340795708168458380L;

    private String inf;
    private Integer type;
	private String transcription;
    private String language;
	private Integer frequency; // Häufigkeitklasse der Grundform laut http://www.ids-mannheim.de/kl/projekte/methoden/derewo.html
	private List<Translation> trList = null;
    private List<WordForm> wfList = null;
    private List<WordProperty> propertyList = null;

    public Inf() {
    }

    public Inf(String inf, Integer type) {
        this();
        setInf(inf);
        setType(type);
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
        if (wfList != null) {
            for(WordForm wf : wfList) {
                wf.setFkInf(id);
            }
        }
    }

    public String getInf() {
        return inf;
    }

	@StringToString
    @FormatField(name = "inf")
    public void setInf(String inf) {
        this.inf = inf;
    }

	public String getTranscription() {
		return transcription;
	}

	@StringToString
    @FormatField(name = "transcription")
	public void setTranscription(String transcription) {
		this.transcription = transcription;
	}

	public String getLanguage() {
		return language;
	}
	public void setLanguage(String lang) {
		this.language = lang;
	}

    public Integer getType() {
        return type;
    }
    @StringToInteger
    @FormatField(name = "type")
    public void setType(Integer type) {
        this.type = type;
    }

	public List<Translation> getTrList() {
		return trList;
	}
    public void setTrList(List<Translation> trList) {
        this.trList = trList;
    }

    @FormatChild( name = "tr")
	public void addTranslation(Translation tr) {
		if (trList == null) {
			trList = new ArrayList<Translation>();
		}
		if (tr.getOrderNr() == 0) {
			tr.setOrderNr(trList.size()+1);
		}
		trList.add(tr);
	}

	public List<WordProperty> getPropertyList() {
		return propertyList;
	}
	public void setPropertyList(List<WordProperty> propertyList) {
		this.propertyList = propertyList;
	}
	@FormatChild( name = "word_prop")
	public void addWordProperty(WordProperty property) {
		if (propertyList == null) {
			propertyList = new ArrayList<WordProperty>();
		}
		propertyList.add(property);
	}

	public Integer getFrequency() {
		return frequency;
	}
	@StringToInteger
	@FormatField(name = "frequency")
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

    public List<WordForm> getWfList() {
        return wfList;
    }

    public void setWfList(List<WordForm> wfList) {
        this.wfList = wfList;
        if (wfList != null ) {
            for(WordForm wf : wfList) {
                wf.setFkInf(id);
                wf.setLanguage(language);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        boolean ret = false;
        if (obj instanceof Inf) {
            Inf otherInf = (Inf)obj;
            ret = new EqualsBuilder().
                    // if deriving: appendSuper(super.equals(obj)).
                            append(getId(), otherInf.getId()).
                    append(getLanguage(), otherInf.getLanguage()).
                    isEquals();
        }
        return ret;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                append(getId()).
                append(getLanguage()).
                toHashCode();
    }

    @Override
	public void reset() {
        super.reset();
    	if (trList != null) trList.clear();
        if (wfList != null) wfList.clear();
        if (propertyList != null) propertyList.clear();
    	inf = null;
    	transcription = null;
    	language = null;
    	type = null;
    	frequency = null;
    }
}
