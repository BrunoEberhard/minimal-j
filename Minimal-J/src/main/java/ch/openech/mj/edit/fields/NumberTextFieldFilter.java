package ch.openech.mj.edit.fields;

import ch.openech.mj.toolkit.TextField.TextFieldFilter;

class NumberTextFieldFilter implements TextFieldFilter {
	private final int size, decimalPlaces;
	private final String allowedCharacters;
	private final boolean negative;
	
	public NumberTextFieldFilter(int size, int decimalPlaces, boolean negative) {
		this.size = size;
		this.decimalPlaces = decimalPlaces;
		this.negative = negative;
		if (decimalPlaces > 0) {
			allowedCharacters = negative ? "-0123456789." : "0123456789.";
		} else {
			allowedCharacters = negative ? "-0123456789" : "0123456789";
		}
	}

	@Override
	public int getLimit() {
		return size + (negative ? 1 : 0) + (decimalPlaces > 0 ? 1 : 0);
	}

	@Override
	public String getAllowedCharacters() {
		return allowedCharacters;
	}

}