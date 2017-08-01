package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.util.StringUtils;

public class JsonHtmlContent extends JsonComponent implements IContent {

	public JsonHtmlContent(String htmlOrUrl) {
		super(isUrl(htmlOrUrl) ? "Url" : "Html", false);
		put("htmlOrUrl", convert(htmlOrUrl));
	}

	private static boolean isUrl(String htmlOrUrl) {
		return !htmlOrUrl.startsWith("<") && (StringUtils.isUrl(htmlOrUrl) || htmlOrUrl.endsWith(".html"));
	}

	private String convert(String htmlOrUrl) {
		if (get("type").equals("Url") ||  htmlOrUrl.startsWith("<")) {
			return htmlOrUrl;
		} else {
			return "<html>" + StringUtils.escapeHTML(htmlOrUrl) + "</html>";
		}
	}
}
