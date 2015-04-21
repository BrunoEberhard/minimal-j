package org.minimalj.frontend.toolkit;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.minimalj.application.ApplicationContext;
import org.minimalj.frontend.page.Page;

public class JUnitClientToolkit extends ClientToolkit {

	private String lastError = null;
	private DialogListener nextConfirmAnswer = null;
	
	public String pullError() {
		String error = lastError;
		lastError = null;
		return error;
	}

	public void setNextConfirAnswer(DialogListener answer) {
		this.nextConfirmAnswer = answer;
	}

	@Override
	public IComponent createLabel(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IComponent createLabel(Action action) {
		return null;
	}

	@Override
	public IComponent createTitle(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextField createReadOnlyTextField() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextField createTextField(int maxLength, String allowedCharacters, InputType inputType, Search<String> autocomplete,
			InputComponentListener changeListener) {
		return new TextField() {
			
			@Override
			public void setValue(String text) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setFocusListener(IFocusListener focusListener) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setEditable(boolean editable) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCommitListener(Runnable runnable) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public String getValue() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	public FlowField createFlowField() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextField createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Input<T> createComboBox(List<T> object, InputComponentListener changeListener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Input<Boolean> createCheckBox(InputComponentListener changeListener, String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ITable<T> createTable(Object[] keys, org.minimalj.frontend.toolkit.ClientToolkit.TableActionListener<T> links) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IComponent createComponentGroup(IComponent... components) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SwitchContent createSwitchContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FormContent createFormContent(int columns, int columnWidth) {
		return new FormContent() {
			@Override
			public void add(IComponent component) {
				// TODO Auto-generated method stub
			}

			@Override
			public void add(String caption, IComponent component, int span) {
				// TODO Auto-generated method stub
			}

			@Override
			public void setValidationMessages(IComponent component, List<String> validationMessages) {
				// TODO Auto-generated method stub
			}
		};
	}

	@Override
	public IDialog showDialog(String title,
			IContent content, Action closeAction, Action... actions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showMessage(String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showError(String text) {
		if (lastError != null) {
			throw new IllegalStateException();
		}
		lastError = text;
	}

	@Override
	public void showConfirmDialog(String message,
			String title, ConfirmDialogType type, DialogListener listener) {
		if (nextConfirmAnswer == null) {
			throw new IllegalStateException();
		}
		listener.close(nextConfirmAnswer);
		nextConfirmAnswer = null;
	}
	
	@Override
	public OutputStream store(String buttonText) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream load(String buttonText) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> IDialog showSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Input<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void show(List<Page> pages, int startIndex) {
		// TODO Auto-generated method stub
	}

	@Override
	public void show(Page page) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void refresh() {
		// TODO Auto-generated method stub
	}

	@Override
	public ApplicationContext getApplicationContext() {
		// TODO Auto-generated method stub
		return null;
	}

}
