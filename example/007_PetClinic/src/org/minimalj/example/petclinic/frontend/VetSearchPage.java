package org.minimalj.example.petclinic.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.petclinic.model.Vet;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.repository.query.By;

public class VetSearchPage extends SearchPage<Vet> {

	private static final Object[] keys = {Vet.$.person.getName(), Vet.$.specialties};
	
	public VetSearchPage(String query) {
		super(query, keys);
	}

	@Override
	protected List<Vet> load(String query, Object[] sortKey, boolean[] sortDirection, int offset, int rows) {
		return Backend.find(Vet.class, By.search(query).limit(offset, rows));
	}

	@Override
	public ObjectPage<Vet> createDetailPage(Vet owner) {
		return null; // no detail
	}

}
