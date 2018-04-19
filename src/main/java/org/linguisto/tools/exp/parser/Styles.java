package org.linguisto.tools.exp.parser;

public class Styles {
	private String transcriptionBegin = "<c c=\"#808080\">"; //gray
	private String transcriptionEnd = "</c>";
	private String typeBegin = "<c c=\"#008000\">"; //green
	private String typeEnd = "</c>";
	private String exampleBegin = "<c c=\"#A0522D\">"; //sienna
	private String exampleEnd = "</c>";
	private String commentBegin = "<c c=\"#808080\">"; //gray
	private String commentEnd = "</c>";
	private String exampleTrBegin = "<c c=\"#4682B4\">"; //SteelBlue

	public String getTranscriptionBegin() {
		return transcriptionBegin;
	}
	public void setTranscriptionBegin(String transcriptionBegin) {
		this.transcriptionBegin = transcriptionBegin;
	}
	public String getTranscriptionEnd() {
		return transcriptionEnd;
	}
	public void setTranscriptionEnd(String transcriptionEnd) {
		this.transcriptionEnd = transcriptionEnd;
	}
	public String getTypeBegin() {
		return typeBegin;
	}
	public void setTypeBegin(String typeBegin) {
		this.typeBegin = typeBegin;
	}
	public String getTypeEnd() {
		return typeEnd;
	}
	public void setTypeEnd(String typeEnd) {
		this.typeEnd = typeEnd;
	}
	public String getExampleBegin() {
		return exampleBegin;
	}
	public void setExampleBegin(String exampleBegin) {
		this.exampleBegin = exampleBegin;
	}
	public String getExampleEnd() {
		return exampleEnd;
	}
	public void setExampleEnd(String exampleEnd) {
		this.exampleEnd = exampleEnd;
	}
	public String getCommentBegin() {
		return commentBegin;
	}
	public String getCommentEnd() {
		return commentEnd;
	}
	public String getExampleTrBegin() {
		return exampleTrBegin;
	}
}
