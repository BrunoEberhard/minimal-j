package ch.openech.mj.toolkit;

import ch.openech.mj.toolkit.TextField.TextFieldFilter;


public class MaxLengthTextFieldFilter implements TextFieldFilter {
	private int maxLength;

	public MaxLengthTextFieldFilter(int maxLength) {
		this.maxLength = maxLength;
	}

	@Override
	public int getLimit() {
		return maxLength;
	}

	@Override
	public String getAllowedCharacters() {
		return null;
	}
}