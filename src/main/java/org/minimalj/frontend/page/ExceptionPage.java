package org.minimalj.frontend.page;

import java.text.MessageFormat;

import org.minimalj.application.Configuration;
import org.minimalj.util.ExceptionUtils;
import org.minimalj.util.resources.Resources;

public class ExceptionPage extends HtmlPage {

	public ExceptionPage(Exception exception) {
		super(createHtml(exception));
	}

	private static String createHtml(Exception exception) {
		if (Configuration.isDevModeActive()) {
			String stackTrace = ExceptionUtils.getStackTrace(exception);
			return MessageFormat.format("<html><body><pre>{0}</pre></body></html>", stackTrace);

		} else {
			return Resources.getString("ExceptionPage.message");
		}
	}

}
