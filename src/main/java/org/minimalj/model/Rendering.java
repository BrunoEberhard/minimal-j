package org.minimalj.model;

import java.util.Locale;

public interface Rendering {

	public static enum RenderType {
		PLAIN_TEXT, HMTL;
	}
	
	/**
	 * Note: it's ok to ignore the render type or the locale.
	 * If asked for HTML the answer can be a plain text. If the
	 * application doesn't care about different languages you
	 * can ignore the locale parameter completly.
	 * 
	 */
	public String render(RenderType renderType, Locale locale);
	
	public default String renderTooltip(RenderType renderType, Locale locale) {
		return null;
	}
	
}
