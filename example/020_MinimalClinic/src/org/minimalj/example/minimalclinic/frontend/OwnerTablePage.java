package org.minimalj.example.minimalclinic.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimalclinic.model.Owner;
import org.minimalj.frontend.page.TableDetailPage;
import org.minimalj.repository.query.By;

public class OwnerTablePage extends TableDetailPage<Owner> {

	private OwnerPage page;
	
	@Override
	protected Object[] getColumns() {
		return new Object[] { Owner.$.person.getName(), Owner.$.address, Owner.$.city };
	}

	@Override
	protected List<Owner> load() {
		return Backend.find(Owner.class, By.all());
	}

	@Override
	protected OwnerPage getDetailPage(Owner owner) {
		if (page == null) {
			page = new OwnerPage(owner);
		} else {
			page.setObject(owner);
		}
		return page;
	}
}
