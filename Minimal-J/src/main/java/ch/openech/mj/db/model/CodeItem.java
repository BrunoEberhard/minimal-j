package ch.openech.mj.db.model;


public class CodeItem<E> {

	private final E key;
	private final String text;
	
	public CodeItem(E key, String text) {
		super();
		this.key = key;
		this.text = text;
	}

	public E getKey() {
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
