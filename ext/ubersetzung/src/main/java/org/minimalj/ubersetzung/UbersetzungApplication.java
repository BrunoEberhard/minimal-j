package org.minimalj.ubersetzung;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.swing.Swing;
import org.minimalj.frontend.page.PageAction;
import org.minimalj.ubersetzung.frontend.UbersetzungTablePage;
import org.minimalj.ubersetzung.model.Ubersetzung;
import org.minimalj.util.resources.MultiResourceBundle;

public class UbersetzungApplication extends Application {

	@Override
	public List<Action> getNavigation() {
		List<Action> actions = new ArrayList<>();
		actions.add(new PageAction(new UbersetzungTablePage()));
		return actions;
	}

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { Ubersetzung.class };
	}

	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		ResourceBundle resourceBundle = super.getResourceBundle(locale);
		ResourceBundle dynamicResourceBundle = DynamicResources.getResourceBundle(locale);
		if (dynamicResourceBundle != null) {
			return new MultiResourceBundle(dynamicResourceBundle, resourceBundle);
		} else {
			return resourceBundle;
		}
	}
	
	public static void main(String[] args) {
		Swing.start(new UbersetzungApplication());
	}

}
