package org.minimalj.example.minimalclinic.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimalclinic.model.Vet;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.repository.query.By;

public class VetTablePage extends TablePage<Vet> {

	@Override
	protected Object[] getColumns() {
		return new Object[] { Vet.$.person.getName(), Vet.$.specialties };
	}

	@Override
	protected List<Vet> load() {
		return Backend.find(Vet.class, By.all());
	}

	@Override
	public void action(Vet selectedObject) {
		Frontend.show(new VetPage(selectedObject));
	}
	
}
