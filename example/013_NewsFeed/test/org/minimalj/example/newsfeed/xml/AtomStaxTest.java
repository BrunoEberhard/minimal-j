package org.minimalj.example.newsfeed.xml;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.example.newsfeed.model.Channel;
import org.minimalj.example.newsfeed.model.Item;

public class AtomStaxTest {

	@Test
	public void readNzz() throws Exception {
		InputStream input = new URL("http://www.nzz.ch/startseite.rss").openStream();
		// InputStream input = getClass().getResourceAsStream("nzz_news.xml");
		AtomStax atomStax = new AtomStax();
		List<Channel> channels = atomStax.process(input);
		
		Assert.assertNotNull(channels);
		Assert.assertTrue(!channels.isEmpty());

		Channel channel = channels.get(0);
		Assert.assertTrue(!channel.items.isEmpty());
		
		Item item = channel.items.get(0);
		Assert.assertNotNull(item.title);
		Assert.assertNotNull(item.link);
		Assert.assertNotNull(item.description);
		Assert.assertNotNull(item.guid);
	}
	
	@Test
	public void readTagi() throws Exception {
		InputStream input = new URL("http://www.tagesanzeiger.ch/rss.html").openStream();
		// InputStream input = getClass().getResourceAsStream("tagi_news.xml");
		
		AtomStax atomStax = new AtomStax();
		List<Channel> channels = atomStax.process(input);
		
		Assert.assertNotNull(channels);
		Assert.assertTrue(!channels.isEmpty());

		Channel channel = channels.get(0);
		Assert.assertTrue(!channel.items.isEmpty());
		
		Item item = channel.items.get(0);
		Assert.assertNotNull(item.title);
		Assert.assertNotNull(item.link);
		Assert.assertNotNull(item.description);
		Assert.assertNotNull(item.guid);
	}
	
	@Test
	public void testRfc1123() {
		LocalDateTime localDateTime = LocalDateTime.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse("Sat, 24 Apr 2010 14:01:00 GMT"));
		Assert.assertEquals(14, localDateTime.getHour());
	}
}
