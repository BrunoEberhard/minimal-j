package org.minimalj.example.erp.model;

import java.util.Locale;

import org.minimalj.model.Code;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.Size;

public class ArticleCategory implements Code, Rendering {

	private static int nextId = 1;
	public static final ArticleCategory attendance = new ArticleCategory(nextId++, null, "Attendance");
	public static final ArticleCategory consulting = new ArticleCategory(nextId++, attendance, "Consulting");
	public static final ArticleCategory training = new ArticleCategory(nextId++, attendance, "Training");
	public static final ArticleCategory projectmanagement = new ArticleCategory(nextId++, attendance, "Projectmanagement");
	public static final ArticleCategory stuff = new ArticleCategory(nextId++, null, "Stuff");
	public static final ArticleCategory trainingMaterial = new ArticleCategory(nextId++, stuff, "Training material");
	public static final ArticleCategory hardware = new ArticleCategory(nextId++, stuff, "Hardware");
	public static final ArticleCategory software = new ArticleCategory(nextId++, stuff, "Software");
	public static final ArticleCategory manyArticles = new ArticleCategory(nextId++, stuff, "Many Articles");
	
	@Size(3)
	public Integer id;
	
	public ArticleCategory category;
	@Size(32)
	public String text;
 
	public ArticleCategory() {
		// empty
	}

	private ArticleCategory(int id, ArticleCategory category, String text) {
		this.id = id;
		this.category = category;
		this.text = text;
	}
	
	@Override
	public String render(RenderType renderType, Locale locale) {
		return text;
	}

}