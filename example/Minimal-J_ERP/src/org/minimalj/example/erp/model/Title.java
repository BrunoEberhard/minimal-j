package org.minimalj.example.erp.model;

public enum Title {
	Dr("Dr."), Mag("Mag."), Msc("Msc.");
	
	private String text;
	
	private Title() {
		this(null);
	}
	
	private Title(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return text;
	}
}
