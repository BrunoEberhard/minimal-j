package org.minimalj.example.petclinic.frontend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.petclinic.model.Pet;
import org.minimalj.example.petclinic.model.Visit;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.transaction.criteria.Criteria;

public class PetTablePage extends TablePage<Pet> {

	private static final Object[] keys = {Pet.$.name, Pet.$.birthDate, Pet.$.type};
	
	private final OwnerPage ownerPage;
	private final VisitTablePage visitTablePage;

	private final List<Visit> visits = new ArrayList<>();
	private Pet selectedPet;
	private AddVisitEditor addVisitEditor;
	
	public PetTablePage(OwnerPage ownerPage) {
		super(keys);
		this.ownerPage = ownerPage;
		this.visitTablePage = new VisitTablePage(this);
		this.addVisitEditor = new AddVisitEditor();
	}
	
	@Override
	protected List<Pet> load() {
		return Backend.persistence().read(Pet.class, Criteria.equals(Pet.$.owner, ownerPage.getObject()), 100);
	}

	public List<Visit> getVisits() {
		return visits;
	}
	
	@Override
	public List<Action> getActions() {
		return Collections.singletonList(addVisitEditor);
	}
	
	@Override
	public void selectionChanged(Pet selectedObject, List<Pet> selectedObjects) {
		setSelectedPet(selectedObject);
		visits.clear();
		for (Pet pet : selectedObjects) {
			visits.addAll(pet.visits);
		}
		if (Frontend.getBrowser().isDetailShown(visitTablePage)) {
			visitTablePage.refresh();
		}
	}
	
	@Override
	public void action(Pet selectedObject) {
		setSelectedPet(selectedObject);
		visits.clear();
		visits.addAll(selectedObject.visits);
		visitTablePage.refresh();
		Frontend.getBrowser().showDetail(visitTablePage);
	}

	private void setSelectedPet(Pet pet) {
		this.selectedPet = pet;
		this.addVisitEditor.setEnabled(pet != null);
	}
	
	public class AddVisitEditor extends NewObjectEditor<Visit> {
		
		@Override
		protected Form<Visit> createForm() {
			Form<Visit> form = new Form<>();
			form.line(Visit.$.visitDate);
			form.line(Visit.$.description);
			return form;
		}

		@Override
		protected Object save(Visit visit) {
			selectedPet.visits.add(visit);
			Backend.persistence().update(selectedPet);
			return visit;
		}
		
		@Override
		protected void finished(Object result) {
			super.finished(result);
			if (Frontend.getBrowser().isDetailShown(visitTablePage)) {
				visitTablePage.refresh();
			}
		}
	}
}
