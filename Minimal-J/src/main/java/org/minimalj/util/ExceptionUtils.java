package org.minimalj.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionUtils {

	private static final Logger EXCEPTION_LOGGER = Logger.getLogger("Exception");
	
	/**
	 * Logs only the relevant part of the stack trace. For example
	 * if a getter fails it's irrelevant if the getter is called
	 * by a swing class or something else
	 * 
	 * @param logger
	 * @param exception
	 */
	public static void logReducedStackTrace(Logger logger, Exception exception) {
		Exception here = new Exception();
		String[] hereStrings = getStackFrames(here);
		String[] throwableStrings = getStackFrames(exception);
		
		int linesToSkip = 1;
		while (throwableStrings.length - linesToSkip > 0 && hereStrings.length - linesToSkip > 0) {
			if (!StringUtils.equals(hereStrings[hereStrings.length-linesToSkip], throwableStrings[throwableStrings.length-linesToSkip])) {
				break;
			}
			linesToSkip++;
		}
		for (int i = 0; i<=throwableStrings.length-linesToSkip; i++) {
			EXCEPTION_LOGGER.log(Level.SEVERE, throwableStrings[i]);
		}
	}

	// from apache ExceptionUtil
	private static String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}
	
	private static String[] getStackFrames(final Throwable throwable) {
		if (throwable == null) {
			return new String[0];
		}
		return getStackFrames(getStackTrace(throwable));
	}

	private static String[] getStackFrames(final String stackTrace) {
		final String linebreak = System.lineSeparator();
		final StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
		final List<String> list = new ArrayList<String>();
		while (frames.hasMoreTokens()) {
			list.add(frames.nextToken());
		}
		return list.toArray(new String[list.size()]);
	}
	
}
