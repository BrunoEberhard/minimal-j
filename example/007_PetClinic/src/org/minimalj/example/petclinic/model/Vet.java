package org.minimalj.example.petclinic.model;

import java.util.HashSet;
import java.util.Set;

import org.minimalj.model.Keys;
import org.minimalj.util.mock.Mocking;

public class Vet implements Mocking {
	public static final Vet $ = Keys.of(Vet.class);
	
	public Object id;
	
	public final Person person = new Person();
	
    public final Set<Specialty> specialties = new HashSet<>();

    @Override
    public void mock() {
    	person.mock();
    	specialties.clear();
    	if (Math.random() < 0.5) specialties.add(Specialty.dentistry);
    	if (Math.random() < 0.5) specialties.add(Specialty.radiology);
    	if (Math.random() < 0.5) specialties.add(Specialty.surgery);
    }
}
