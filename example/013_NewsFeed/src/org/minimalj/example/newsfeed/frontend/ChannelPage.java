package org.minimalj.example.newsfeed.frontend;

import java.util.List;

import org.minimalj.example.newsfeed.model.Channel;
import org.minimalj.example.newsfeed.model.Item;
import org.minimalj.frontend.page.HtmlPage;
import org.minimalj.frontend.page.TablePage.TablePageWithDetail;

public class ChannelPage extends TablePageWithDetail<Item, HtmlPage> {

	public static final Object[] KEYS = new Object[]{Item.$.title, Item.$.pubDate};
	
	private Channel channel;
	
	public ChannelPage(Channel channel) {
		super(KEYS);
		this.channel = channel;
	}

	@Override
	protected HtmlPage createDetailPage(Item mainObject) {
		// return new HtmlPage(mainObject.link, mainObject.title);
		return new HtmlPage("<html>" + mainObject.description + "</html>", mainObject.title);
	}
	
	@Override
	protected HtmlPage updateDetailPage(HtmlPage page, Item mainObject) {
		return createDetailPage(mainObject);
	}

	@Override
	protected List<Item> load() {
		return channel.items;
	}
}
