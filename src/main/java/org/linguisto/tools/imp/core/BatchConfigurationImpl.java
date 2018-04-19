package org.linguisto.tools.imp.core;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BatchConfigurationImpl implements BatchConfiguration {
	private final static Logger	log = Logger.getLogger(BatchConfigurationImpl.class.getSimpleName());

	private final Properties configuration;
	private final Map<String, String> dbKeyMapping;

	private final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

	public BatchConfigurationImpl() {
		configuration = new Properties();
		dbKeyMapping = null;
	}

	public BatchConfigurationImpl(Map<String, String> dbKeyMapping) {
		this.configuration = new Properties();
		this.dbKeyMapping = dbKeyMapping;

		log.info("Initializing Configuration");

		InputStream defaults = this.getClass().getClassLoader().getResourceAsStream(
                "vlad/vdict/imp/core/resources/defaults.properties");

		if (defaults != null) {
			try {
				this.configuration.load(defaults);
			}
			catch (IOException e) {
				log.log(Level.SEVERE, "Error reading default configuration properties.", e);
			}
		}
		else
			log.warning("Could not load default configuration properties.");
	}

	public String get(String key) {
		return this.configuration.getProperty(key);
	}

	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(get(key));
	}

	public long getLong(String key) {
		return Long.parseLong(get(key));
	}

	public int getInteger(String key) {
		return Integer.parseInt(get(key));
	}

	public Calendar getDate(String key) {
		String text = this.get(key);
		Calendar calendar = Calendar.getInstance();

		if (text == null)
			return null;

		Pattern pattern = Pattern.compile("^(\\d{1,2})\\.(\\d{1,2})\\.(\\d{2,4})$");
		Matcher matcher = pattern.matcher(text);

		if (!matcher.find()) {
			if (!text.equals(""))
				log.warning("Could not parse given date: '" + text + "'");
			return null;
		}
		else {
			calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(matcher.group(1)));
			calendar.set(Calendar.MONTH, Integer.parseInt(matcher.group(2)));
			int year = Integer.parseInt(matcher.group(3));
			calendar.set(Calendar.YEAR, (year < 100)?year + 2000:year);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);

			return calendar;
		}
	}

	public void set(String key, String value) {
		this.configuration.setProperty(key, value);
	}

	public void setDate(String key, Calendar cal) {
		this.set(key, this.format.format(cal.getTime()));
	}

	public void loadComponentConfiguration(String component) {
		//TODO load config from DB
//		try {
//			mapDatabaseConfiguration(dbKeyMapping, configDao.getConfigurations(component));
//		} catch (DAOException e) {
//			log.error("Could not read configuration '" + component + "' from database.", e);
//		}
	}

	public void printToLog() {
		log.info("Configuration:");
		for (Object key : this.configuration.keySet()) {
			Object value = this.configuration.get(key);
			log.info(key + " = " + value);
		}
	}

	private void mapDatabaseConfiguration(Map<String, String> dbKeyMapping, Map<String, String> dbConfig) {
		for (String key : dbConfig.keySet()) {
			String value = dbConfig.get(key);

			if (dbKeyMapping.containsKey(key))
				key = dbKeyMapping.get(key);

			if (key != null && value != null)
				this.configuration.setProperty(key, value);
		}
	}
}