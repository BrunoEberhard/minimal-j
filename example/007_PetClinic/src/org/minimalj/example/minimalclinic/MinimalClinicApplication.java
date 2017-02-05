package org.minimalj.example.minimalclinic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.minimalj.application.Application;
import org.minimalj.example.minimalclinic.frontend.AddOwnerEditor;
import org.minimalj.example.minimalclinic.frontend.AddVetEditor;
import org.minimalj.example.minimalclinic.frontend.OwnerSearchPage;
import org.minimalj.example.minimalclinic.frontend.OwnerTablePage;
import org.minimalj.example.minimalclinic.frontend.PetTablePage;
import org.minimalj.example.minimalclinic.frontend.VetSearchPage;
import org.minimalj.example.minimalclinic.frontend.VetTablePage;
import org.minimalj.example.minimalclinic.model.Owner;
import org.minimalj.example.minimalclinic.model.Pet;
import org.minimalj.example.minimalclinic.model.Vet;
import org.minimalj.example.petclinic.PetClinicApplication;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.page.HtmlPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageAction;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.util.resources.Resources;

public class MinimalClinicApplication extends Application {

	@Override
	protected Set<String> getResourceBundleNames() {
		return Collections.singleton(PetClinicApplication.class.getName());
	}
	
	@Override
	public List<Action> getNavigation() {
		List<Action> menu = new ArrayList<>();

		ActionGroup groupOwner = new ActionGroup(Resources.getString(Owner.class));
		groupOwner.add(new PageAction(new OwnerTablePage()));
		groupOwner.add(new AddOwnerEditor());
		menu.add(groupOwner);
		
		ActionGroup groupPet = new ActionGroup(Resources.getString(Pet.class));
		groupPet.add(new PageAction(new PetTablePage()));
		menu.add(groupPet);

		ActionGroup groupVet = new ActionGroup(Resources.getString(Vet.class));
		groupVet.add(new PageAction(new VetTablePage()));
		groupVet.add(new AddVetEditor());
		menu.add(groupVet);

		return menu;
	}
	
	@Override
	public Page createDefaultPage() {
		return new HtmlPage("intro_minimalclinic.html", "Minimal Clinic");
	}
	
	@Override
	public Page createSearchPage(String query) {
		return SearchPage.handle(new OwnerSearchPage(query), new VetSearchPage(query));
	}
	
	@Override
	public Class<?>[] getEntityClasses() {
		return new Class[]{Owner.class, Pet.class, Vet.class};
	}
}
