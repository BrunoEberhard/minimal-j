package org.minimalj.example.petclinic.frontend;

import java.util.List;

import org.minimalj.example.petclinic.model.Visit;
import org.minimalj.frontend.page.TablePage;

public class VisitTablePage extends TablePage<Visit> {

	private static final Object[] keys = {Visit.$.visitDate, Visit.$.description};
	
	private final PetTablePage petTablePage;
	
	public VisitTablePage(PetTablePage petTablePage) {
		super(keys);
		this.petTablePage = petTablePage;
	}
	
	@Override
	protected List<Visit> load() {
		return petTablePage.getVisits();
	}
	
}
