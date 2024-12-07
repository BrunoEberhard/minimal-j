package org.minimalj.test.headless;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonComponent;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonRadioButtons;
import org.minimalj.frontend.impl.json.JsonTable;
import org.minimalj.frontend.impl.util.History;
import org.minimalj.frontend.impl.util.History.HistoryListener;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Page.Dialog;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.security.Authorization;
import org.minimalj.security.Subject;
import org.minimalj.test.PageContainerTestFacade;

public class HeadlessFrontend extends JsonFrontend {

	private final HeadlessPageManager pageManager = new HeadlessPageManager();
	
	@Override
	public HeadlessPageManager getPageManager() {
		return pageManager;
	}
	
	@Override
	public boolean showLogin(Dialog dialog) {
		getPageManager().showDialog(dialog);
		return true;
	}
	
	@Override
	public <T> ITable<T> createTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		// no page manager
		return new JsonTable<>(null, keys, multiSelect, listener);
	}
	
	@Override
	public SwitchComponent createSwitchComponent() {
		return new HeadlessSwitch();
	}
	
	@Override
	public SwitchContent createSwitchContent() {
		return new HeadlessSwitch();
	}
	
	public static class HeadlessSwitch extends JsonComponent implements SwitchContent, SwitchComponent {
		public HeadlessSwitch() {
			super("Switch");
		}

		@Override
		public void show(IContent content) {
			if (content != get("component")) {
				putSilent("component", content);
			}
		}

		@Override
		public void show(IComponent component) {
			if (component != get("component")) {
				putSilent("component", component);
			}
		}
	}
	
	@Override
	public <T> Input<T> createRadioButtons(List<T> items, InputComponentListener changeListener) {
		return new HeadlessRadioButtons<>(items, changeListener);
	}
	
	public static class HeadlessRadioButtons<T> extends JsonRadioButtons<T> {

		public HeadlessRadioButtons(List<T> objects, InputComponentListener changeListener) {
			super(objects, changeListener);
		}

		public void setValueText(String text) {
			Map<String, Map<String, String>> options = (Map<String, Map<String, String>>) get("options");
			for (Map.Entry<String, Map<String, String>> optionEntry : options.entrySet()) {
				String optionText = optionEntry.getValue().get("text");
				if (text.equals(optionText)) {
					changedValue(optionEntry.getKey());
					return;
				}
			}
			changedValue(null);
		}

		public String getValueText() {
			String selectedId = (String) get(VALUE);
			if (selectedId != null) {
				Map<String, Map<String, String>> options = (Map<String, Map<String, String>>) get("options");
				return options.get(selectedId).get("text");
			} else {
				return null;
			}
		}
	}
	
	// 
	
	class HeadlessPageManager implements PageManager, PageContainerTestFacade, HistoryListener {
		private final BackPageAction backAction;
		private final ForwardPageAction forwardAction; // , refreshAction, previousAction, nextAction, filterAction, favoriteAction;
		private final History<List<Page>> history;
		private final List<HeadlessDialogTestFacade> dialogs;
		
		private final List<Page> visiblePageAndDetailsList;

		private HeadlessNavigationTestFacade navigationTestFacade;
		
		public HeadlessPageManager() {
			history = new History<>(this);
			visiblePageAndDetailsList = new ArrayList<>();
			
			backAction = new BackPageAction();
			forwardAction = new ForwardPageAction();
			
			dialogs = new ArrayList<>();
		}
		
		// PageContainerTestFacade
		
		@Override
		public NavigationTestFacade getNavigation() {
			if (!dialogs.isEmpty()) {
				for (var dialog: dialogs) {
					System.err.println("Open dialog: " + dialog.getDialog());
				}
			}
			if (!dialogs.isEmpty()) {
				throw new IllegalStateException("Before navigate all Dialogs should be closed");
			}
			return navigationTestFacade;
		}
		
		@Override
		public List<PageTestFacade> getPages() {
			List<PageTestFacade> pages = new ArrayList<>();
			if (history.getPresent() != null) {
				history.getPresent().forEach(page -> {
					pages.add(new HeadlessPageTestFacade(page));
				});
			}
			return pages;
		}

		@Override
		public DialogTestFacade getDialog() {
			if (!dialogs.isEmpty()) {
				return dialogs.get(dialogs.size() - 1);
			} else {
				return null;
			}
		}

		@Override
		public ActionTestFacade getBack() {
			return backAction;
		}

		@Override
		public ActionTestFacade getForward() {
			return forwardAction;
		}
		
		// PageManager
		
		@Override
		public void show(Page page) {
			if (Authorization.hasAccess(Subject.getCurrent(), page)) {
				List<Page> pages = new ArrayList<>();
				pages.add(page);
				history.add(pages);
			} else {
				Frontend.showError("No Access to " + page.getClass().getSimpleName());
			}
		}

		@Override
		public void showDialog(Dialog dialog) {
			var dialogTestFacade = new HeadlessDialogTestFacade(dialog);
			dialogs.add(dialogTestFacade);
		}

		@Override
		public void closeDialog(Dialog dialog) {
			dialogs.removeIf(d -> d.getDialog() == dialog);
		}

		@Override
		public void showMessage(String text) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void showError(String text) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void login(Subject subject) {
			Subject.setCurrent(subject);
			navigationTestFacade = new HeadlessNavigationTestFacade();
			dialogs.clear(); // if previous test has left a Dialog open it should be closed
			
			if (subject == null && /* initializing && */ Application.getInstance().getAuthenticatonMode().showLoginAtStart()) {
				Backend.getInstance().getAuthentication().showLogin();
			} else {
				show(Application.getInstance().createDefaultPage());
			}
		}
		
		//
		
		public Page getVisiblePage() {
			return visiblePageAndDetailsList.get(0);
		}

		protected void updateActions() {
			Page visiblePage = getVisiblePage();
			if (visiblePage != null) {
				backAction.setEnabled(hasPast());
				forwardAction.setEnabled(hasFuture());
			} else {
				backAction.setEnabled(false);
				forwardAction.setEnabled(false);
			}
		}
		
		@Override
		public void onHistoryChanged() {
			visiblePageAndDetailsList.clear();

			for (Page page : history.getPresent()) {
				addPageOrDetail(page);
			}

			updateActions();
		}
		
		public boolean hasFuture() {
			return history.hasFuture();
		}

		public boolean hasPast() {
			return history.hasPast();
		}
		
		private void addPageOrDetail(Page page) {
			visiblePageAndDetailsList.add(page);
		}
		
		//
		
		private abstract class HeadlessActionTestFacade implements ActionTestFacade {
			private boolean enabled;

			@Override
			public boolean isEnabled() {
				return enabled;
			}

			public void setEnabled(boolean enabled) {
				this.enabled = enabled;
			}
			
			@Override
			public final void run() {
				if (isEnabled()) {
					doRun();
				}
			}

			protected abstract void doRun();
		}
		
		protected class BackPageAction extends HeadlessActionTestFacade {
			
			@Override
			public void doRun() {
				history.previous();
			}
		}

		protected class ForwardPageAction extends HeadlessActionTestFacade {
			
			@Override
			public void doRun() {
				history.next();
			}
		}
	}
}
