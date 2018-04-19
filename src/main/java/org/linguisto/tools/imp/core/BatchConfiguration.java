package org.linguisto.tools.imp.core;

import java.util.Calendar;

public interface BatchConfiguration extends Configureable {
	public String get(String key);
	public long getLong(String key);
	public int getInteger(String key);
	public boolean getBoolean(String key);
	public Calendar getDate(String key);
	public void setDate(String key, Calendar cal);
	public void set(String key, String value);
	public void loadComponentConfiguration(String component);
	public void printToLog();
}