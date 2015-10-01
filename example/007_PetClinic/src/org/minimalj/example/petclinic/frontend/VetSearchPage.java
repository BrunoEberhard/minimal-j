package org.minimalj.example.petclinic.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.petclinic.model.Vet;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.SearchPage.SimpleSearchPage;
import org.minimalj.transaction.criteria.By;

public class VetSearchPage extends SimpleSearchPage<Vet> {

	private static final Object[] keys = {Vet.$.person.getName(), Vet.$.specialties};
	
	public VetSearchPage(String query) {
		super(query, keys);
	}

	@Override
	protected List<Vet> load(String query) {
		return Backend.persistence().read(Vet.class, By.search(query), 100);
	}

	@Override
	public ObjectPage<Vet> createDetailPage(Vet owner) {
		return null; // no detail
	}

}
