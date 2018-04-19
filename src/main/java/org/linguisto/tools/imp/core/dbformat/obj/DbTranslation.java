package org.linguisto.tools.imp.core.dbformat.obj;

import java.util.ArrayList;
import java.util.List;

import org.linguisto.tools.imp.core.dbformat.db.annotation.Column;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Table;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Table;
import org.linguisto.tools.imp.core.dbformat.db.annotation.Column;

@Table(name = "TR_DE_UK")
public class DbTranslation extends BaseDbObj {
	
	private static final long serialVersionUID = -4067107222995098077L;

	private int trOrder;
	private String trDesc;
	private DbDeWord fromWord;
	private DbDeWord toWord;
	private List<DbTrExample> fraseList;

	@Column(name = "ORDER_NR")
	public int getTrOrder() {
		return trOrder;
	}
	public void setTrOrder(int trOrder) {
		this.trOrder = trOrder;
	}

	@Column(name = "DESCRIPTION")
	public String getTrDesc() {
		return trDesc;
	}
	public void setTrDesc(String trDesc) {
		this.trDesc = trDesc;
	}

	public DbDeWord getFromWord() {
		return fromWord;
	}
	public void setFromWord(DbDeWord fromWord) {
		this.fromWord = fromWord;
	}

	public DbDeWord getToWord() {
		return toWord;
	}
	public void setToWord(DbDeWord toWord) {
		this.toWord = toWord;
	}

	public List<DbTrExample> getFraseList() {
		return fraseList;
	}
	public void addFrase(DbTrExample frase) {
		if (fraseList == null) {
			fraseList = new ArrayList<DbTrExample>();
		}
		fraseList.add(frase);
	}
}

