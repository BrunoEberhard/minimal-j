package org.minimalj.example.petclinic.model;

import java.util.List;
import java.util.Random;

import org.fluttercode.datafactory.impl.DataFactory;
import org.minimalj.backend.Backend;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;
import org.minimalj.transaction.criteria.By;
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
    	if (Keys.isKeyObject(this)) return Keys.methodOf(this, "pets", List.class);
    	return Backend.read(Pet.class, By.field(Pet.$.owner, this), 100);
	}
    
    @Override
    public String render(RenderType renderType) {
    	return person.render(renderType);
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