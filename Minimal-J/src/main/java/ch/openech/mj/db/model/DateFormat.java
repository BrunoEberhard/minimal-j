package ch.openech.mj.db.model;

import ch.openech.mj.util.DateUtils;

public class DateFormat implements Format {

	private final boolean partialAllowed;

	public DateFormat(boolean partialAllowed) {
		this.partialAllowed = partialAllowed;
	}

	public boolean isPartialAllowed() {
		return partialAllowed;
	}

	@Override
	public Class<java.util.Date> getClazz() {
		return java.util.Date.class;
	}

	@Override
	public int getSize() {
		return 10;
	}
	
	@Override
	public String display(String value) {
		return DateUtils.formatCH(value);
	}

	@Override
	public String displayForEdit(String value) {
		return DateUtils.formatCH(value);
	}


}
