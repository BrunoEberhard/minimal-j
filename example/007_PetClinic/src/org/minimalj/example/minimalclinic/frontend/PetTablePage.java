package org.minimalj.example.minimalclinic.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimalclinic.model.Pet;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.repository.criteria.By;

public class PetTablePage extends TablePage<Pet> {

	private static final Object[] keys = {Pet.$.name, Pet.$.type, Pet.$.owner.person.firstName, Pet.$.owner.person.lastName};
	
	public PetTablePage() {
		super(keys);
	}

	@Override
	protected List<Pet> load() {
		return Backend.read(Pet.class, By.all(), Integer.MAX_VALUE);
	}

}
