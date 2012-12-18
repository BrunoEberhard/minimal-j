package ch.openech.mj.model.annotation;

public interface LimitedString {
	
	public int getMaxLength();
	
	public String getAllowedCharacters();
	
}