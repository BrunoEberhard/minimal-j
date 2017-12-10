package org.minimalj.frontend.impl.lanterna;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.lanterna.toolkit.LanternaDialog;
import org.minimalj.frontend.impl.lanterna.toolkit.LanternaFrontend;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.model.test.ModelTest;

import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class Lanterna extends MultiWindowTextGUI implements PageManager {

	private final LanternaWindow window;
	
	public Lanterna(Screen screen) {
		super(screen);

		window = new LanternaWindow();
		addWindowAndWait(window);
	}
	
	@Override
	public void show(Page page) {
		window.show(page);
	}

	@Override
	public void showError(String text) {
		MessageDialog.showMessageDialog(this, "Error", text);
	}

	@Override
	public void showMessage(String text) {
		MessageDialog.showMessageDialog(this, "Message", text);
	}

	@Override
	public <T> IDialog showSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDialog showDialog(String title, IContent content, Action closeAction, Action saveAction, Action... actions) {
		// TODO use saveAction (Enter in TextFields should save the dialog)
		return new LanternaDialog(this, content, title, closeAction, actions);
	}

	//
	
	public static void start() {
		ModelTest.exitIfProblems();
		Frontend.setInstance(new LanternaFrontend());
		try {
			Terminal terminal = new DefaultTerminalFactory().createTerminal();
			Screen screen = new TerminalScreen(terminal);
			screen.startScreen();
			
			new Lanterna(screen);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}		
	}
	
	public static void start(Application application) {
		Application.setInstance(application);
		start();
	}

	public static void main(String... args) {
		Application.initApplication(args);
		start();
	}
}
