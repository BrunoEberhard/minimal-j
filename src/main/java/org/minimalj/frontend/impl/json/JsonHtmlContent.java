package org.minimalj.frontend.impl.json;

import java.net.URL;

import org.minimalj.frontend.Frontend.IContent;

public class JsonHtmlContent extends JsonComponent implements IContent {

	public JsonHtmlContent(String html) {
		super("Html");
		put("htmlOrUrl", html);
	}

	public JsonHtmlContent(URL url) {
		super("Url");
		put("htmlOrUrl", url.toExternalForm());
	}

}
