package org.minimalj.tutorial.domain;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class Person {

	public static final Person PERSON = Keys.of(Person.class);
	
	public long id;
	
	@Size(3)
    public Integer age;
    
    @Size(255)
    public String firstname,  lastname;
    
    public final List<Email> emails = new ArrayList<>();
}
