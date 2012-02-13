package ch.openech.mj.db.model;

public class BooleanFormat implements Format {

	private final boolean nullable;
	
	public BooleanFormat(boolean nullable) {
		this.nullable = nullable;
	}

	@Override
	public Class<java.lang.Boolean> getClazz() {
		return java.lang.Boolean.class;
	}

	@Override
	public int getSize() {
		return 1;
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
