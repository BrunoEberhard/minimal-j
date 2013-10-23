package ch.openech.mj.example;

import static ch.openech.mj.example.model.Lend.*;
import ch.openech.mj.edit.form.Form;
import ch.openech.mj.example.model.Lend;

public class LendForm extends Form<Lend> {

	public LendForm(boolean editable) {
		super(editable);
		
//		line(new BookField(LEND.book));
//		line(new CustomerField(LEND.customer));
		
		line(LEND.book);
		line(LEND.customer);
		
		line(LEND.till);
	}
	
}
