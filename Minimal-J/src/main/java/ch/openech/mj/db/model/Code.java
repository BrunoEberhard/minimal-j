package ch.openech.mj.db.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import ch.openech.mj.util.StringUtils;

public class Code implements Format {
	
	private final ResourceBundle resourceBundle;
	private final String prefix;
	private final String displayName;
	private final String unknownText;
	private final String defolt;
	protected final List<String> keys = new ArrayList<String>();
	protected final List<String> texts = new ArrayList<String>();

	public Code(ResourceBundle resourceBundle) {
		this(resourceBundle, null);
	}
	
	public Code(ResourceBundle resourceBundle, String prefix) {
		this.resourceBundle = resourceBundle;
		this.prefix = prefix;
		
		displayName = readDisplayName();
		unknownText = readUnknownText();
		defolt = readDefault();
		readValues();
	}

	public Code(Code code, String... allowedKeys) {
		this.resourceBundle = code.resourceBundle;
		this.prefix = code.prefix;
		
		displayName = code.displayName;
		unknownText = code.unknownText;
		defolt = code.defolt;
		for (String key : allowedKeys) {
			keys.add(key);
			texts.add(code.getText(key));
		}
	}

	public Code(ResourceBundle resourceBundle, String prefix, Code code, String... allowedKeys) {
		this.resourceBundle = resourceBundle;
		this.prefix = prefix;
		
		displayName = readDisplayName();
		unknownText = readUnknownText();
		defolt = readDefault();
		for (String key : allowedKeys) {
			keys.add(key);
			texts.add(code.getText(key));
		}
	}
	
	@Override
	public Class<?> getClazz() {
		return String.class;
	}

	protected String getString(String key) {
		if (prefix != null) {
			key = prefix + "." + key;
		}
		if (resourceBundle.containsKey(key)) return resourceBundle.getString(key);
		else return null;
	}
	
	private String readDisplayName() {
		return getString("object");
	}
	
	private String readUnknownText() {
		String unknownText = getString("unknownText");
		if (StringUtils.isBlank(unknownText)) {
			unknownText = displayName + " unbekannt";
		}
		return unknownText;
	}
	
	private String readDefault() {
		return getString("default");
	}
	
	protected void readValues() {
		int index = 0;
		String key = null;
		while ((key = getString("key." + index)) != null) {
			keys.add(key.trim());
			String text = getString("text." + index);
			if (StringUtils.isBlank(text)) text = "Wert " + key;
			texts.add(text.trim());
			index++;
		}
		if (index == 0) {
			throw new RuntimeException("Code without values: " + prefix);
		}
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getUnknownText() {
		return unknownText;
	}
	
	public String getDefault() {
		return defolt;
	}
	
	public String[] getTextArray() {
		return texts.toArray(new String[texts.size()]);
	}
	
	public List<String> getTexts() {
		return Collections.unmodifiableList(texts);
	}
	
	public String[] getKeyArray() {
		return keys.toArray(new String[keys.size()]);
	}
	
	public String getText(int index) {
		return texts.get(index);
	}

	public String getKey(int index) {
		return keys.get(index);
	}
	
	public String getKey(String key) {
		for (int i = 0; i<keys.size(); i++) {
			if (StringUtils.equals(key, texts.get(i))) {
				return keys.get(i);
			}
		}
		return null;
	}
	
	public String getText(String key) {
		if (key == null) return null;
		key = key.trim();
		int index = keys.indexOf(key);
		if (index >= 0) return texts.get(index); else return null;
	}
	
	public int indexOf(String key) {
		if (key == null) return -1;
		key = key.trim();
		return keys.indexOf(key);
	}

	public int count() {
		return keys.size();
	}

	@Override
	public int getSize() {
		int maxSize = 0;
		for (String key : keys) {
			maxSize = Math.max(maxSize, key.length());
		}
		return maxSize;
	}
	
	@Override
	public String display(String value) {
		return getText(value);
	}

	@Override
	public String displayForEdit(String value) {
		return getText(value);
	}

}
