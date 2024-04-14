package org.minimalj.example.newsfeed.model;

import java.time.LocalDateTime;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;

public class Item {

	public static final Item $ = Keys.of(Item.class);
	
	@NotEmpty
	public Channel channel;
	
	@Size(512)
	public String title, link, guid;
	
	@Size(512)
	public String thumbnail;
	
	@Size(2048)
	public String description;
	
	@Size(256)
	public String category;
	
	@Size(Size.TIME_HH_MM)
	public LocalDateTime pubDate;
}
