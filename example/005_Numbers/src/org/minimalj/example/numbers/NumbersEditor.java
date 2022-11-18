package org.minimalj.example.numbers;

import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class NumbersEditor extends NewObjectEditor<Numbers> {

	@Override
	protected Form<Numbers> createForm() {
		return new NumbersForm();
	}

	@Override
	protected Numbers save(Numbers object) {
		return object;
	}
	
	@Override
	public int getMinWidth() {
		return 600;
	}
	
}
