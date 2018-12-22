package org.minimalj.frontend.impl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.minimalj.application.Configuration;
import org.minimalj.util.StringUtils;

public class HtmlString implements CharSequence {
	private static final Set<String> allowedHtmlTags = new TreeSet<>();

	private final List<FormatElement> elements = new ArrayList<>();

	static {
		String allowedHtmlTagConfiguration = Configuration.get("MjAllowedHtmlTags", "b, i, u, sub, sup");
		Arrays.stream(allowedHtmlTagConfiguration.split(",")).forEach(allowedHtmlTags::add);
	}
	
	public HtmlString format(String tag, String text) {
		if (allowedHtmlTags.contains(tag)) {
			elements.add(new FormatElementTag(tag, text));
		} else {
			append(text);
		}
		return this;
	}
	
	public HtmlString append(String string) {
		elements.add(new FormatElementString(string));
		return this;
	}
	
	public HtmlString append(String label, String value) {
		return this;
		
	}
	
	public HtmlString newLine() {
		elements.add(NEW_LINE);
		return this;
	}

	public HtmlString append(HtmlString otherText) {
		elements.addAll(otherText.elements);
		return this;
	}

	//
	
	public HtmlString appendLine(String string) {
		if (!StringUtils.isEmpty(string)) {
			append(string).newLine();
		}
		return this;
	}

	public HtmlString appendLine(Integer integer) {
		if (integer != null) {
			append(integer.toString()).newLine();
		}
		return this;
	}

	public HtmlString appendLine(String... strings) {
		boolean first = true;
		for (String string : strings) {
			if (StringUtils.isEmpty(string)) continue;
			if (!first) append(" "); else first = false;
			append(string);
		}
		if (!first) newLine();
		return this;
	}
	
	public HtmlString appendSeparated(String... strings) {
		boolean first = true;
		for (String string : strings) {
			if (StringUtils.isEmpty(string)) continue;
			if (!first) append(", "); else first = false;
			append(string);
		}
		return this;
	}
	
	//
	
	public String getString() {
		StringBuilder s = new StringBuilder();
		elements.stream().forEach(str -> s.append(str.getString()));
		return s.toString();
	}
	
	public String getHtml() {
		StringBuilder s = new StringBuilder();
		elements.stream().forEach(str -> s.append(str.getHtml()));
		return s.toString();
	}
	
	public int getRowCount() {
		int rows = (int) elements.stream().filter(e -> e == NEW_LINE).count();
		int i = elements.size() - 1;
		while (i >= 0) {
			if (elements.get(i) == NEW_LINE) {
				rows--;
				i--;
			} else {
				break;
			}
		}
		return rows;
	}

	private interface FormatElement {
		public String getString();
		
		public String getHtml();
	}
	
	private static class FormatElementString implements FormatElement {
		private final String string;
		
		public FormatElementString(String string) {
			this.string = string;
			
		}

		@Override
		public String getString() {
			return string;
		}

		@Override
		public String getHtml() {
			return StringUtils.escapeHTML(string);
		}
	}
	
	private static class FormatElementTag extends FormatElementString {
		private final String tag;

		public FormatElementTag(String tag, String string) {
			super(string);
			this.tag = tag;
		}

		@Override
		public String getHtml() {
			return "<" + tag + ">" + super.getHtml() + "</" + tag + ">";
		}
	}

	private static FormatElementNewLine NEW_LINE = new FormatElementNewLine();
	
	private static class FormatElementNewLine implements FormatElement {
		@Override
		public String getString() {
			return "\n";
		}

		@Override
		public String getHtml() {
			return "<br>";
		}
	}

	@Override
	public int length() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public char charAt(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
