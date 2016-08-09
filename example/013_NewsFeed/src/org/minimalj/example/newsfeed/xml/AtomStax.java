package org.minimalj.example.newsfeed.xml;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.minimalj.example.newsfeed.model.Channel;
import org.minimalj.example.newsfeed.model.Item;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.StringUtils;

public class AtomStax {

	public static final String RSS = "rss";
	public static final String CHANNEL = "channel";
	public static final String ITEM = "item";
	
	public List<Channel> process(InputStream input) throws XMLStreamException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader xml = inputFactory.createXMLEventReader(input);
		
		List<Channel> channels = process(xml);
		xml.close();
		
		return channels;
	}
	
	public List<Channel> process(String xmlString) throws XMLStreamException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader xml = inputFactory.createXMLEventReader(new StringReader(xmlString));
		
		List<Channel> channels = process(xml);
		xml.close();
		
		return channels;
	}

	private List<Channel> process(XMLEventReader xml) throws XMLStreamException {
		while (xml.hasNext()) {
			XMLEvent event = xml.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String startName = startElement.getName().getLocalPart();
				if (startName.equals(RSS)) {
					return rss(xml);
				}
				else StaxUtil.skip(xml);
			} 
		}
		return null;
	}

	private List<Channel> rss(XMLEventReader xml) throws XMLStreamException {
		List<Channel> channels = new ArrayList<>();
		
		while(true) {
			XMLEvent event = xml.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String startName = startElement.getName().getLocalPart();
				if (StringUtils.equals(startName, CHANNEL)) {
					channels.add(channel(xml));
				}
			} else if (event.isEndElement()) {
				return channels;
			}
			// else skip
		}
	}
	
	public Channel channel(XMLEventReader xml) throws XMLStreamException {
		Channel channel = new Channel();
		Map<String, PropertyInterface> properties = FlatProperties.getProperties(Channel.class);
		String[] fields = properties.keySet().toArray(new String[properties.size()]);
		
		while(true) {
			XMLEvent event = xml.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String startName = startElement.getName().getLocalPart();
				if (StringUtils.equals(startName, fields)) {
					StaxUtil.simpleValue(xml, channel, startName);
				} else if (StringUtils.equals(startName, ITEM)) {
					Item item = item(xml);
					item.channel = channel;
					channel.items.add(item);
				} else StaxUtil.skip(xml);
			} else if (event.isEndElement()) {
				return channel;
			}
			// else skip
		}
	}
	
	public Item item(XMLEventReader xml) throws XMLStreamException {
		Item item = new Item();
		Map<String, PropertyInterface> properties = FlatProperties.getProperties(Item.class);
		String[] fields = properties.keySet().toArray(new String[properties.size()]);
		
		while(true) {
			XMLEvent event = xml.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String startName = startElement.getName().getLocalPart();
				if (StringUtils.equals(startName, fields)) {
					StaxUtil.simpleValue(xml, item, startName);
				} else StaxUtil.skip(xml);
			} else if (event.isEndElement()) {
				return item;
			}
			// else skip
		}
	}
	
}
