package ch.openech.mj.db.model;

import ch.openech.mj.util.StringUtils;

public class CodeItem {

	private final String key;
	private final String text;
	
	public CodeItem(String key, String text) {
		super();
		this.key = key;
		this.text = text;
	}

	public String getKey() {
		return key;
	}

	public String getText() {
		return text;
	}

	@Override
	public int hashCode() {
		if (key == null) {
			return 0;
		} else {
			return key.hashCode();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CodeItem)) {
			return false;
		}
		CodeItem other = (CodeItem) obj;
		return StringUtils.equals(key, other.key);
	}

	@Override
	public String toString() {
		return text;
	}
	
}
