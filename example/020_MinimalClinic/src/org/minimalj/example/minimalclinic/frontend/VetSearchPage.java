package org.minimalj.example.minimalclinic.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimalclinic.model.Vet;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.repository.query.By;

public class VetSearchPage extends SearchPage<Vet> {

	private static final Object[] keys = {Vet.$.person.getName(), Vet.$.specialties};
	
	public VetSearchPage(String query) {
		super(query, keys);
	}

	@Override
	protected List<Vet> load(String query) {
		return Backend.find(Vet.class, By.search(query));
	}

	@Override
	public ObjectPage<Vet> createDetailPage(Vet vet) {
		return new VetPage(vet);
	}

}
