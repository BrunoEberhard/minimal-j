package ch.openech.mj.db.model;

public class IntegerFormat implements Format {

	private final Class<?> clazz;
	private final boolean nonNegative;
	private final int size;
	
	public IntegerFormat(Class<?> clazz, int size, boolean nonNegative) {
		this.clazz = clazz;
		this.size = size;
		this.nonNegative = nonNegative;
	}

	@Override
	public Class<?> getClazz() {
		return clazz;
	}
	
	@Override
	public int getSize() {
		return size;
	}

	public boolean isNonNegative() {
		return nonNegative;
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
