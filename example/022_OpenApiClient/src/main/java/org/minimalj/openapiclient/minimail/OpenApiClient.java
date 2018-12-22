package org.minimalj.openapiclient.minimail;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.example.openapiclient.page.MjModelPage;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.frontend.page.Page;

public class OpenApiClient extends Application {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] {};
	}

	@Override
	public List<Action> getNavigation() {
		List<Action> actions = new ArrayList<Action>();
		// TODO generate from openapi.json
		return actions;
	}

	@Override
	public Page createDefaultPage() {
		return new MjModelPage();
	}

	public static void main(String[] args) {
		WebServer.start(new OpenApiClient());
	}

}
