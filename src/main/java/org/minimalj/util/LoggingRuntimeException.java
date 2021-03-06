package org.minimalj.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public LoggingRuntimeException(Exception x, Logger logger, String text) {
		super(text);
		logger.log(Level.SEVERE, text, x);
	}
}
