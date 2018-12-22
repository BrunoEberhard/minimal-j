package org.minimalj.example.minimalclinic.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.action.Action;
import org.minimalj.repository.query.By;
import org.minimalj.transaction.Transaction;

public class MinimalClinicDemoDataAction extends Action implements Transaction<Void> {
	private static final long serialVersionUID = 1L;

	@Override
	public void action() {
		Backend.execute(this);
	}

	@Override
	public Void execute() {
		Random random = new Random();
		List<Owner> owners = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			Owner owner = new Owner();
			owner.mock();
			Object id = Backend.getInstance().getRepository().insert(owner);
			owners.add(Backend.getInstance().getRepository().read(Owner.class, id));
		}
		List<PetType> petTypes = Backend.find(PetType.class, By.ALL);
		for (int i = 0; i < 100; i++) {
			Pet pet = new Pet();
			pet.owner = owners.get(random.nextInt(owners.size()));
			pet.birthDate = LocalDate.now().minusDays(random.nextInt(3000));
			pet.name = NAMES[random.nextInt(NAMES.length)] + random.nextInt(1000);
			pet.type = petTypes.get(random.nextInt(petTypes.size()));
			Backend.getInstance().getRepository().insert(pet);
		}
		for (int i = 0; i < 30; i++) {
			Vet vet = new Vet();
			vet.mock();
			Backend.getInstance().getRepository().insert(vet);
		}
		return null;
	}

	// from https://www.tierchenwelt.de/
	private static final String[] NAMES = new String[] { "Camaxtli", "Chantico", "Chiconahui", "Cihuacoatl", "Cinteotl", "Cochimetl", "Cuaxolotl", "Itzli",
			"Itzpapalotl", "Metzli", "Mextli", "Mixcoatl", "Nagual", "Nanauatzin", "Ometeotl", "Oxomoco", "Patecatl", "Quetzalcoatl", "Xolotl" };

}
