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
	protected final List<CodeItem> items = new ArrayList<CodeItem>();

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
			items.add(new CodeItem(key, code.getText(key)));
		}
	}

	public Code(ResourceBundle resourceBundle, String prefix, Code code, String... allowedKeys) {
		this.resourceBundle = resourceBundle;
		this.prefix = prefix;
		
		displayName = readDisplayName();
		unknownText = readUnknownText();
		defolt = readDefault();
		for (String key : allowedKeys) {
			items.add(new CodeItem(key, code.getText(key)));
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
			String text = getString("text." + index);
			if (StringUtils.isBlank(text)) text = "Wert " + key;
			items.add(new CodeItem(key.trim(), text.trim()));
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
	
	public List<CodeItem> getItems() {
		return Collections.unmodifiableList(items);
	}

	public CodeItem getItem(int index) {
		return items.get(index);
	}
	
	public String getText(int index) {
		return items.get(index).getText();
	}

	public String getKey(int index) {
		return items.get(index).getKey();
	}
	
	public String getText(String key) {
		int index = indexOf(key);
		if (index >= 0) return items.get(index).getText(); else return null;
	}
	
	public int indexOf(String key) {
		for (int i = 0; i<items.size(); i++) {
			if (StringUtils.equals(items.get(i).getKey(), key)) {
				return i;
			}
		}
		return -1;
	}

	public int count() {
		return items.size();
	}

	@Override
	public int getSize() {
		int maxSize = 0;
		for (CodeItem item : items) {
			maxSize = Math.max(maxSize, item.getKey().length());
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
