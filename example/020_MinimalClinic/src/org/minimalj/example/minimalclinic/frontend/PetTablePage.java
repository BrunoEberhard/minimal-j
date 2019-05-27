package org.minimalj.example.minimalclinic.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.minimalclinic.model.Pet;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.repository.query.By;

public class PetTablePage extends TablePage<Pet> {

	@Override
	protected Object[] getColumns() {
		return new Object[] { Pet.$.name, Pet.$.type, Pet.$.owner.person.firstName, Pet.$.owner.person.lastName };
	}
	
	@Override
	protected List<Pet> load() {
		return Backend.find(Pet.class, By.all());
	}

}
