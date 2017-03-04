package org.minimalj.example.petclinic.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.petclinic.model.Owner;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.repository.query.By;

public class OwnerSearchPage extends SearchPage<Owner> {

	private static final Object[] keys = {Owner.$.person.getName(), Owner.$.address, Owner.$.city, Owner.$.telephone};
	
	public OwnerSearchPage(String query) {
		super(query, keys);
	}

	@Override
	protected List<Owner> load(String query, Object[] sortKey, boolean[] sortDirection, int offset, int rows) {
		return Backend.find(Owner.class, By.search(query).limit(offset, rows));
	}

	@Override
	public ObjectPage<Owner> createDetailPage(Owner owner) {
		return new OwnerPage(owner);
	}
}
