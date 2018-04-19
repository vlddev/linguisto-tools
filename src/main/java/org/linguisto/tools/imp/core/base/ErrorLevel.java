package org.linguisto.tools.imp.core.base;

import java.util.logging.Level;

/**
 */
public enum ErrorLevel {
    NONE,
    INFO,
    WARNING,
    ERROR,
    FATAL;
    
    public static Level convertToLoggingLevel(ErrorLevel errorLevel) {
    	switch(errorLevel) {
    	case NONE: return Level.FINE;
    	case INFO: return Level.INFO;
    	case WARNING: return Level.WARNING;
    	case ERROR: return Level.SEVERE;
    	case FATAL: return Level.SEVERE;
    	default: return Level.INFO;
    	}
    }

}

