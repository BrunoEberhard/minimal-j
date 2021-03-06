package org.minimalj.example.petclinic.model;

import java.util.List;
import java.util.Random;

import org.fluttercode.datafactory.impl.DataFactory;
import org.minimalj.backend.Backend;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.query.By;
import org.minimalj.util.mock.Mocking;

public class Owner implements Rendering, Mocking {
	public static final Owner $ = Keys.of(Owner.class);
	
	public Object id;
	
	public final Person person = new Person();
	
    @NotEmpty @Size(255)
    public String address;

    @NotEmpty @Size(80)
    public String city;

    @NotEmpty @Size(20)
    public String telephone;
    
    public List<Pet> getPets() {
    	if (Keys.isKeyObject(this)) return Keys.methodOf(this, "pets");
    	return Backend.find(Pet.class, By.field(Pet.$.owner, this));
	}
    
    @Override
    public String render() {
    	return person.render();
    }
    
    @Override
    public void mock() {
    	person.mock();
		DataFactory df = new DataFactory();
		address = df.getAddress();
		city = df.getCity();
    	telephone = String.valueOf(100000 + new Random().nextInt(900000));
    }

}