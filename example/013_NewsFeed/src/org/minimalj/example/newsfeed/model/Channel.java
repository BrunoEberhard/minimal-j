package org.minimalj.example.newsfeed.model;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class Channel {

	public static final Channel $ = Keys.of(Channel.class);
	
	public final transient List<Item> items = new ArrayList<>();
	
	@Size(512)
	public String title;
	
	@Size(2048)
	public String description, copyright;
	
	@Size(32)
	public String language;

}
