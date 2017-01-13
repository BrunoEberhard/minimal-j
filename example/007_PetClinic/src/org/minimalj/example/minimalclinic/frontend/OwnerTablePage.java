package org.minimalj.example.minimalclinic.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimalclinic.model.Owner;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.repository.criteria.By;

public class OwnerTablePage extends TablePage.TablePageWithDetail<Owner, OwnerPage> {

	private static final Object[] keys = {Owner.$.person.getName(), Owner.$.address, Owner.$.city};
	
	public OwnerTablePage() {
		super(keys);
	}

	@Override
	protected List<Owner> load() {
		return Backend.read(Owner.class, By.all(), 100);
	}

	@Override
	protected OwnerPage createDetailPage(Owner owner) {
		return new OwnerPage(owner);
	}

	@Override
	protected OwnerPage updateDetailPage(OwnerPage page, Owner owner) {
		page.setObject(owner);
		return page;
	}
}
