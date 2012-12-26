package ch.openech.mj.edit;

import ch.openech.mj.application.MjApplication;
import ch.openech.mj.edit.Editor.EditorFinishedListener;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ProgressListener;


/**
 * Do not use the class directly. Use EditorPageAction.
 * 
 */
public class EditorPage extends Page {

	private final Editor<?> editor;
	private final IComponent layout;
	private boolean finished = false;

	public EditorPage(PageContext context, String editorClass) {
		this(context, createEditor(context, editorClass));
	}
	
	public EditorPage(PageContext context, String[] editorClassAndArguments) {
		this(context, createEditor(context, editorClassAndArguments));
	}
	
	static Editor<?> createEditor(PageContext context, String... editorClassAndArguments) {
		try {
			Class<?> clazz = Class.forName(MjApplication.getCompletePackageName("editor") + "." + editorClassAndArguments[0]);
			if (editorClassAndArguments.length > 1) {
				Class<?>[] argumentClasses = new Class[editorClassAndArguments.length];
				Object[] arguments = new Object[editorClassAndArguments.length];
				argumentClasses[0] = PageContext.class;
				arguments[0] = context;
				for (int i = 1; i<argumentClasses.length; i++) {
					argumentClasses[i] = String.class;
					arguments[i] = editorClassAndArguments[i];
				}
				return (Editor<?>) clazz.getConstructor(argumentClasses).newInstance(arguments);
			} else {
				return (Editor<?>) clazz.newInstance();
			}
		} catch (Exception x) {
			throw new RuntimeException("EditorPage Erstellung fehlgeschlagen", x);
		}
	}

	public EditorPage(PageContext context, Editor<?> editor) {
		super(context);
		this.editor = editor;
		IForm<?> form = editor.startEditor();
		layout = ClientToolkit.getToolkit().createEditorLayout(form.getComponent(), editor.getActions());

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
					progressListener = ClientToolkit.getToolkit().showProgress(getPageContext(), "Save");
				}
				progressListener.showProgress(value, maximum);
			}
		});

	}
	
	@Override
	public String getTitle() {
		return editor.getTitle();
	}

	@Override
	protected void setTitle(String title) {
		throw new IllegalStateException("setTitle on EditorPage not allowed");
	}

	@Override
	public boolean isExclusive() {
		return !finished;
	}

	@Override
	public IComponent getComponent() {
		return layout;
	}
	
	public void checkedClose() {
		editor.checkedClose();
	}
	
}
