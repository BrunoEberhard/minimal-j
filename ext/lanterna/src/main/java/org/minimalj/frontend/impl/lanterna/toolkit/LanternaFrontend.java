package org.minimalj.frontend.impl.lanterna.toolkit;

import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.lanterna.component.LanternaForm;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.model.Rendering;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.TextBox.Style;

public class LanternaFrontend extends Frontend {
	
	private static ThreadLocal<PageManager> currentPageManager = new ThreadLocal<>();

	public LanternaFrontend() {
	}
	
	public static void run(Component component, Runnable runnable) {
		PageManager pageManager = (PageManager) component.getTextGUI();
		currentPageManager.set(pageManager); 
		try {
			runnable.run();
		} catch (Exception x) {
			pageManager.showError(x.getMessage());
		} finally {
			currentPageManager.set(null);
		}
	}
	
	@Override
	public Input<Boolean> createCheckBox(InputComponentListener changeListener, String text) {
		return new LanternaCheckBox(changeListener, text);
	}

	@Override
	public <T> Input<T> createComboBox(List<T> objects, InputComponentListener changeListener) {
		return new LanternaComboBox<T>(objects, changeListener);
	}

	@Override
	public IList createList(Action... actions) {
		return new LanternaList(actions);
	}

	@Override
	public FormContent createFormContent(int columns, int columnWidth) {
		return new LanternaForm(columns);
	}

	@Override
	public IComponent createComponentGroup(IComponent... components) {
		return new LanternaHorizontalLayout(components);
	}

	@Override
	public IComponent createText(final Action action) {
		LanternaActionText button = new LanternaActionText(action.getName());
		button.addListener(b -> LanternaFrontend.run(b, () -> action.action()));
		return button;
	}

	public static class LanternaActionText extends Button implements IComponent {
		public LanternaActionText(String name) {
			super(name);
		}
	}
	
	@Override
	public PageManager getPageManager() {
		return currentPageManager.get();
	}
	
	@Override
	public IComponent createText(String text) {
		return new LanternaText(text);
	}

	@Override
	public IComponent createText(Rendering rendering) {
		return new LanternaText(rendering);
	}
	
	@Override
	public Input<String> createReadOnlyTextField() {
		return new LanternaReadOnlyTextField();
	}

	@Override
	public Input<byte[]> createImage(int size, InputComponentListener changeListener) {
		throw new RuntimeException("Image not yet implemented in JsonFrontend");
	};

	@Override
	public SwitchContent createSwitchContent() {
		return new LanternaSwitch();
	}
	
	@Override
	public SwitchComponent createSwitchComponent() {
		return new LanternaSwitch();
	}

	@Override
	public <T> ITable<T> createTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		return new LanternaTable<T>(keys, multiSelect, listener);
	}

	@Override
	public IContent createHtmlContent(String htmlOrUrl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContent createQueryContent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Input<String> createTextField(int maxLength, String allowedCharacters, Search<String> suggestionSearch, InputComponentListener changeListener) {
		return new LanternaTextField(changeListener, Style.SINGLE_LINE);
	}
	
	@Override
	public PasswordField createPasswordField(InputComponentListener changeListener, int maxLength) {
		return new LanternaPasswordField(changeListener);
	}

	@Override
	public Input<String> createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener) {
		return new LanternaTextField(changeListener, Style.MULTI_LINE);
	}

	@Override
	public IComponent createTitle(String text) {
		return new LanternaText(text);
	}

	@Override
	public <T> Input<T> createLookup(Runnable lookup, InputComponentListener changeListener) {
		// TODO Auto-generated method stub
		return null;
	}
}
