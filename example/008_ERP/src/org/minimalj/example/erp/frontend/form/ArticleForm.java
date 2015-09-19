package org.minimalj.example.erp.frontend.form;

import static org.minimalj.example.erp.model.Article.*;

import org.minimalj.example.erp.model.Article;
import org.minimalj.frontend.form.Form;

public class ArticleForm extends Form<Article> {

	public ArticleForm(boolean editable) {
		super(editable);
		
		line($.articleNr);
		line($.article);
		line($.articleCategory);
		line($.description);
		line($.price);
		line($.unit);
	}
}
