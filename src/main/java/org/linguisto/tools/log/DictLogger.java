package org.linguisto.tools.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DictLogger {
	static private FileHandler logFile;
	static private SimpleFormatter logFormatter;

	static public void setup() throws IOException {
		// Create Logger
		Logger logger = Logger.getLogger("");
		logger.setLevel(Level.INFO);
		logFile = new FileHandler("dict.log");

		// Create txt Formatter
		logFormatter = new SimpleFormatter();
		logFile.setFormatter(logFormatter);
		logger.addHandler(logFile);
	}
}
