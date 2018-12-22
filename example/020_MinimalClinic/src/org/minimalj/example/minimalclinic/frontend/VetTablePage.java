package org.minimalj.example.minimalclinic.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimalclinic.model.Vet;
import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.repository.query.By;

public class VetTablePage extends TablePage<Vet> {

	private static final Object[] keys = {Vet.$.person.getName(), Vet.$.specialties};
	
	public VetTablePage() {
		super(keys);
	}

	@Override
	protected FormContent getOverview() {
		Form<Object> header = new Form<>();
		header.text("Sali");
		return header.getContent();
	}

	@Override
	protected List<Vet> load() {
		return Backend.find(Vet.class, By.all());
	}

}
