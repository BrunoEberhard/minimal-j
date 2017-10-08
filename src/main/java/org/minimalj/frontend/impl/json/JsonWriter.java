package org.minimalj.frontend.impl.json;

import java.lang.reflect.Array;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class JsonWriter {

	private final StringBuilder s = new StringBuilder(512);
	private final Stack<Object> calls = new Stack<>();

	private static final String spaces = "                                                            ";
	private static final int stepsize = 3;
	private static final int max = spaces.length() / stepsize;
	private int level = 0;

	public JsonWriter() {
	}

	public String write(Map<String, Object> values) {
		s.setLength(0);
		value(values);
		return s.toString();
	}

	private void indent() {
		if (0 == level)
			return;
		if (level < max) {
			add(spaces.substring(0, level * stepsize));
		} else {
			add(spaces);
			for (int i = level - max; i < level; ++i) {
				for (int j = 0; j < stepsize; ++j) {
					add(" ");
				}
			}
		}
	}

	private void nl() {
		add("\n");
		indent();
	}

	private void stepIn(char c) {
		add(c);
		++level;
		nl();
	}

	private void stepOut(char c) {
		--level;
		nl();
		add(c);
	}

	private void emptyArray() {
		add("[]");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void value(Object object) {
		if (object == null || cyclic(object)) {
			add("null");
		} else {
			calls.push(object);
			if (object instanceof Boolean)
				bool(((Boolean) object).booleanValue());
			else if (object instanceof Number || object instanceof Temporal)
				add(object);
			else if (object instanceof String || object instanceof Character)
				string(object);
			else if (object instanceof Map)
				map((Map) object);
			else if (object.getClass().isArray())
				array(object);
			else if (object instanceof Iterator)
				array((Iterator) object);
			else if (object instanceof Collection)
				array(((Collection) object).iterator());
			else {
				System.err.println(s.toString());
				throw new IllegalArgumentException("cannot be serialized in json: " + object);
			}
			calls.pop();
		}
	}

	private boolean cyclic(Object object) {
		Iterator<Object> it = calls.iterator();
		while (it.hasNext()) {
			Object called = it.next();
			if (object == called)
				return true;
		}
		return false;
	}

	private void map(Map<String, Object> map) {
		stepIn('{');

		Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<?, ?> e = it.next();
			value(e.getKey());
			add(": ");
			value(e.getValue());
			if (it.hasNext()) {
				add(',');
				nl();
			}
		}

		stepOut('}');
	}

	private void array(Iterator<?> it) {
		if (it.hasNext()) {
			stepIn('[');
			while (it.hasNext()) {
				value(it.next());
				if (it.hasNext()) {
					add(",");
					nl();
				}
			}
			stepOut(']');
		} else {
			emptyArray();
		}
	}

	private void array(Object object) {
		int length = Array.getLength(object);
		if (length > 0) {
			stepIn('[');
			for (int i = 0; i < length; ++i) {
				value(Array.get(object, i));
				if (i < length - 1) {
					add(',');
					nl();
				}
			}
			stepOut(']');
		} else {
			emptyArray();
		}
	}

	private void bool(boolean b) {
		add(Boolean.valueOf(b).toString());
	}

	private void string(Object obj) {
		add('"');
		CharacterIterator it = new StringCharacterIterator(obj.toString());
		for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
			if (c == '"')
				add("\\\"");
			else if (c == '\\')
				add("\\\\");
			else if (Character.isISOControl(c)) {
				if (c == '\b')
					add("\\b");
				else if (c == '\f')
					add("\\f");
				else if (c == '\n')
					add("\\n");
				else if (c == '\r')
					add("\\r");
				else if (c == '\t')
					add("\\t");
				else
					unicode(c);
			} else {
				add(c);
			}
		}
		add('"');
	}

	private void add(Object obj) {
		s.append(obj);
	}

	private void add(char c) {
		s.append(c);
	}

	private static final char[] hex = "0123456789ABCDEF".toCharArray();

	private void unicode(char c) {
		add("\\u");
		int n = c;
		for (int i = 0; i < 4; ++i) {
			int digit = (n & 0xf000) >> 12;
			add(hex[digit]);
			n <<= 4;
		}
	}
	
}