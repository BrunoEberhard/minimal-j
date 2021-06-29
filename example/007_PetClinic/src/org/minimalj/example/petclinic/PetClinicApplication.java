package org.minimalj.example.petclinic;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.example.petclinic.frontend.AddOwnerEditor;
import org.minimalj.example.petclinic.frontend.AddVetEditor;
import org.minimalj.example.petclinic.frontend.OwnerSearchPage;
import org.minimalj.example.petclinic.frontend.VetSearchPage;
import org.minimalj.example.petclinic.frontend.VetTablePage;
import org.minimalj.example.petclinic.model.Owner;
import org.minimalj.example.petclinic.model.Pet;
import org.minimalj.example.petclinic.model.Vet;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.frontend.page.HtmlPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageAction;
import org.minimalj.frontend.page.SearchPage;

public class PetClinicApplication extends Application {

	@Override
	public List<Action> getNavigation() {
		List<Action> menu = new ArrayList<>();
		menu.add(new AddOwnerEditor());
		menu.add(new PageAction(new VetTablePage()));
		menu.add(new AddVetEditor());
		return menu;
	}
	
	@Override
	public Page createDefaultPage() {
		return new HtmlPage("intro.html", "Pet Clinic");
	}
	
	@Override
	public void search(String query) {
		SearchPage.handle(new OwnerSearchPage(query), new VetSearchPage(query));
	}
	
	@Override
	public Class<?>[] getEntityClasses() {
		return new Class[]{Owner.class, Pet.class, Vet.class};
	}
	
	public static void main(String[] args) {
		WebServer.start(new PetClinicApplication());
	}
	
}
