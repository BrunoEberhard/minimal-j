package org.minimalj.example.petclinic;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.example.petclinic.frontend.AddOwnerEditor;
import org.minimalj.example.petclinic.frontend.OwnerSearchPage;
import org.minimalj.example.petclinic.model.Owner;
import org.minimalj.example.petclinic.model.Pet;
import org.minimalj.example.petclinic.model.Vet;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page;

public class PetClinicApplication extends Application {

	@Override
	public List<Action> getMenu() {
		List<Action> menu = new ArrayList<>();
		menu.add(new AddOwnerEditor());
		return menu;
	}
	
	@Override
	public Page createSearchPage(String query) {
		return new OwnerSearchPage(query);
	}
	
	@Override
	public Class<?>[] getEntityClasses() {
		return new Class[]{Owner.class, Pet.class, Vet.class};
	}
}
