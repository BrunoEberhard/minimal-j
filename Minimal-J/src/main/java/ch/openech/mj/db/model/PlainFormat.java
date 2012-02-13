package ch.openech.mj.db.model;

/**
 * A String in this format doesn't have any (used) format. E.g. a name.
 * 
 * @author Bruno
 *
 */
public class PlainFormat implements Format {

	private final int size;
	
	public PlainFormat(int size) {
		this.size = size;
	}

	@Override
	public Class<String> getClazz() {
		return String.class;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public String display(String value) {
		return value;
	}

	@Override
	public String displayForEdit(String value) {
		return value;
	}
}
