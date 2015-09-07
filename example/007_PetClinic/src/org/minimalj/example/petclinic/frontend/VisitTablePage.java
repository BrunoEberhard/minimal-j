package org.minimalj.example.petclinic.frontend;

import java.util.List;

import org.minimalj.example.petclinic.model.Pet;
import org.minimalj.example.petclinic.model.Visit;
import org.minimalj.frontend.page.TablePage;

public class VisitTablePage extends TablePage<Visit> {

	private static final Object[] keys = {Visit.$.visitDate, Visit.$.description};
	
	private final Pet pet;
	
	public VisitTablePage(Pet pet) {
		super(keys);
		this.pet = pet;
	}
	
	@Override
	protected List<Visit> load() {
		return pet.visits;
	}
	
}
