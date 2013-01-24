package ch.openech.mj.model;


public class CodeItem<E> {

	private final E key;
	private final String text;
	private final String description;
	
	public CodeItem(E key, String text) {
		this(key, text, null);
	}
	
	public CodeItem(E key, String text, String description) {
		super();
		this.key = key;
		this.text = text;
		this.description = description;
	}

	public E getKey() {
		return key;
	}

	public String getText() {
		return text;
	}

	public String getDescription() {
		return description;
	}
	
	@Override
	public int hashCode() {
		if (key == null) {
			return 0;
		} else {
			return key.hashCode();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof CodeItem)) {
			return false;
		}
		CodeItem other = (CodeItem) obj;
		return key.equals(other.key);
	}

	@Override
	public String toString() {
		return text;
	}
	
}
