package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.util.StringUtils;

public class JsonHtmlContent extends JsonComponent implements IContent {
	private static final long serialVersionUID = 1L;
	
	public JsonHtmlContent(String htmlOrUrl) {
		super(StringUtils.isUrl(htmlOrUrl) ? "Url" : "Html", false);
		put("htmlOrUrl", htmlOrUrl);
	}
	
}
