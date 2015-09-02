package org.minimalj.example.petclinic.model;

import java.util.Set;
import java.util.TreeSet;

import org.minimalj.model.Keys;

public class Vet {
	public static final Vet $ = Keys.of(Vet.class);
	
	public Object id;
	
	public final Person person = new Person();
	
    public final Set<Specialty> specialties = new TreeSet<>();

}
