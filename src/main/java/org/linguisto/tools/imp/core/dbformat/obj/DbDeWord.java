package org.linguisto.tools.imp.core.dbformat.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.linguisto.tools.imp.core.dbformat.db.annotation.Join;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Join;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Table;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Column;

@Table(name = "DE_WORD")
public class DbDeWord extends BaseDbObj {
	private static final long serialVersionUID = 1769188720841084344L;
	private String word;
	private Locale lang;
	private DbWordType wordType;
	private Integer stress;
	private List<DbTranslation> trList;
	private List<DbWordProperty> propertyList;
	
	@Column(name = "WORD")
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public Locale getLang() {
		return lang;
	}
	public void setLang(Locale lang) {
		this.lang = lang;
	}
	public DbWordType getWordType() {
		return wordType;
	}
	public void setWordType(DbWordType wordType) {
		this.wordType = wordType;
	}

    @Join(name = "Id")
	public List<DbTranslation> getTrList() {
		return trList;
	}
	public void addTranslation(DbTranslation tr) {
		if (trList == null) {
			trList = new ArrayList<DbTranslation>();
		}
		trList.add(tr);
	}
	public List<DbWordProperty> getPropertyList() {
		return propertyList;
	}
	public void setPropertyList(List<DbWordProperty> propertyList) {
		this.propertyList = propertyList;
	}
	public void addWordProperty(DbWordProperty property) {
		if (propertyList == null) {
			propertyList = new ArrayList<DbWordProperty>();
		}
		propertyList.add(property);
	}
	@Column(name = "STRESS")
	public Integer getStress() {
		return stress;
	}
	public void setStress(Integer stress) {
		this.stress = stress;
	}
	
	public String getExportXML(String prefix, String ln) {
		StringBuffer ret = new StringBuffer();
		ret.append("<de_word>").append(ln);
		ret.append(prefix).append("<word>").append(getWord()).append("</word>").append(ln);
		ret.append(prefix).append("<wordtype>").append(getWordType().getId()).append("</wordtype>").append(ln);
		ret.append(prefix).append("<stress>").append(getStress()).append("</stress>").append(ln);
		if (getPropertyList() != null) {
			for (DbWordProperty property : getPropertyList()) {
				ret.append(prefix).append("<de_word_prop>").append(ln);
				ret.append(prefix).append(prefix).append("<pname>").append(property.getPname()).append("</pname>").append(ln);
				ret.append(prefix).append(prefix).append("<pvalue>").append(property.getPvalue()).append("</pvalue>").append(ln);
				ret.append(prefix).append("</de_word_prop>").append(ln);
			}
		}
		if (getTrList() != null) {
			for (DbTranslation tr : getTrList()) {
				ret.append(prefix).append("<tr_de_uk>").append(ln);
				ret.append(prefix).append(prefix).append("<uk_word>").append(ln);
				ret.append(prefix).append(prefix).append(prefix).append("<word>").append(tr.getToWord().getWord()).append("</word>").append(ln);
				ret.append(prefix).append(prefix).append(prefix).append("<wordtype>").append(tr.getToWord().getWordType().getId()).append("</wordtype>").append(ln);
				ret.append(prefix).append(prefix).append("</uk_word>").append(ln);
				if (tr.getFraseList() != null) {
					for (DbTrExample frase : tr.getFraseList()) {
						ret.append(prefix).append(prefix).append("<frase>").append(ln);
						ret.append(prefix).append(prefix).append(prefix).append("<nr>").append(frase.getFrOrder()).append("</nr>").append(ln);
						ret.append(prefix).append(prefix).append(prefix).append("<orig>").append(frase.getOrigFrase()).append("</orig>").append(ln);
						ret.append(prefix).append(prefix).append(prefix).append("<tr>").append(frase.getTrFrase()).append("</tr>").append(ln);
						ret.append(prefix).append(prefix).append(prefix).append("<desc>").append(frase.getDesc()).append("</desc>").append(ln);
						ret.append(prefix).append(prefix).append("</frase>").append(ln);
					}
				}
				ret.append(prefix).append("</tr_de_uk>").append(ln);
			}
		}
		ret.append("</de_word>").append(ln);
		return ret.toString();
	}
}
