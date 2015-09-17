package org.minimalj.frontend.page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;

public class HtmlPage extends Page {

	private final String htmlOrUrl;
	
	public HtmlPage(String htmlOrUrl) {
		this.htmlOrUrl = htmlOrUrl;
	}
	
	public HtmlPage(InputStream inputStream) {
		this(readStream(inputStream));
	}
	
	@Override
	public IContent getContent() {
		return Frontend.getInstance().createHtmlContent(htmlOrUrl);
	}

	public static String readStream(InputStream inputStream) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			return reader.lines().collect(Collectors.joining(System.getProperty("line.separator")));
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}
}
