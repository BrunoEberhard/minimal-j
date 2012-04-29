package ch.openech.mj.edit;

import ch.openech.mj.edit.Editor.EditorFinishedListener;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.util.ProgressListener;

public class EditorPage extends Page {

	private final Editor<?> editor;
	private final IComponent layout;
	private boolean finished = false;
	
	public EditorPage(PageContext context, String[] editorClassAndArguments) {
		this(context, createEditor(editorClassAndArguments));
	}
	
	public EditorPage(PageContext context, Class<?> editorClass, String[] arguments) {
		this(context, createEditor(editorClass, arguments));
	}
	
	public EditorPage(PageContext context, String editorClass) {
		this(context, createEditor(editorClass));
	}
	
	static Editor<?> createEditor(String... editorClassAndArguments) {
		try {
			Class<?> clazz = Class.forName(editorClassAndArguments[0]);
			if (editorClassAndArguments.length > 1) {
				Class<?>[] argumentClasses = new Class[editorClassAndArguments.length - 1];
				Object[] arguments = new Object[editorClassAndArguments.length - 1];
				for (int i = 0; i<argumentClasses.length; i++) {
					argumentClasses[i] = String.class;
					arguments[i] = editorClassAndArguments[i + 1];
				}
				return (Editor<?>) clazz.getConstructor(argumentClasses).newInstance(arguments);
			} else {
				return (Editor<?>) clazz.newInstance();
			}
		} catch (Exception x) {
			throw new RuntimeException("EditorPage Erstellung fehlgeschlagen", x);
		}
	}
	
	static Editor<?> createEditor(Class<?> editorClass, String... arguments) {
		try {
			if (arguments.length > 0) {
				Class<?>[] argumentClasses = new Class[arguments.length];
				for (int i = 0; i<argumentClasses.length; i++) {
					argumentClasses[i] = String.class;
				}
				return (Editor<?>) editorClass.getConstructor(argumentClasses).newInstance(arguments);
			} else {
				return (Editor<?>) editorClass.newInstance();
			}
		} catch (Exception x) {
			throw new RuntimeException("EditorPage Erstellung fehlgeschlagen", x);
		}
	}
	
	protected EditorPage(PageContext context, Editor<?> editor) {
		super(context);
		this.editor = editor;
		IForm<?> form = editor.startEditor(context);
		layout = ClientToolkit.getToolkit().createEditorLayout(form, editor.getActions());

		setTitle(editor.getTitle());
		
		editor.setEditorFinishedListener(new EditorFinishedListener() {
			private ProgressListener progressListener;
			
			@Override
			public void finished(String followLink) {
				finished = true;
				if (followLink != null) {
					getPageContext().show(followLink);
				} else {
					getPageContext().closeTab();
				}
				if (progressListener != null) {
					progressListener.showProgress(100, 100);
				}
			}

			@Override
			public void progress(int value, int maximum) {
				if (progressListener == null) {
					progressListener = ClientToolkit.getToolkit().showProgress(getPageContext().getComponent(), "Save");
				}
				progressListener.showProgress(value, maximum);
			}
		});

	}
	
	@Override
	public boolean isExclusive() {
		return !finished;
	}

	@Override
	public IComponent getPanel() {
		return layout;
	}

	
	public void checkedClose() {
		editor.checkedClose();
	}
	
}
