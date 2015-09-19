package org.minimalj.example.erp.model;

public enum ArticleCategory_as_enum {
	Attendance, 
	Consulting(Attendance), 
	Training(Attendance), 
	Projectmanagement(Attendance),
	Stuff, 
	Training_material(Stuff, "Training material"),
	Hardware(Stuff), 
	Software(Stuff), 
	Many_Articles(Stuff, "Many Articles");
	
	private ArticleCategory_as_enum category;
	private String text;

	private ArticleCategory_as_enum() {
		this(null, null);
	}
		
	private ArticleCategory_as_enum(ArticleCategory_as_enum category) {
		this(category, null);
	}
	
	private ArticleCategory_as_enum(ArticleCategory_as_enum category, String text) {
		this.category = category;
		this.text = text;
	}
	
	protected ArticleCategory_as_enum getCategory() {
		return category;
	}

	protected void setCategory(ArticleCategory_as_enum category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return text != null ? text : name();
	}
}
