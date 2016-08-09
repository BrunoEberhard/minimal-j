package org.minimalj.example.newsfeed;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.example.newsfeed.frontend.ChannelPage;
import org.minimalj.example.newsfeed.model.Channel;
import org.minimalj.example.newsfeed.xml.AtomStax;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.PageAction;

public class RssNewsApplication extends Application {

	@Override
	public List<Action> getNavigation() {
		AtomStax atomStax = new AtomStax();
		
		List<Channel> channels = new ArrayList<>();
		try {
			channels.addAll(atomStax.process(new URL("http://www.nzz.ch/startseite.rss").openStream()));
			channels.addAll(atomStax.process(new URL("http://www.tagesanzeiger.ch/rss.html").openStream()));
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
		List<Action> actions = new ArrayList<>();
		
		for (Channel channel : channels) {
			actions.add(new PageAction(new ChannelPage(channel), channel.title));
		}
		
		return actions;
	}

	public static void main(String[] args) {
		Application.main(args);
	}
	
}
