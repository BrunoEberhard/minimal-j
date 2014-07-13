package org.minimalj.frontend.toolkit;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.minimalj.frontend.toolkit.ITable.TableActionListener;

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
	public IComponent createLabel(IAction action) {
		// TODO Auto-generated method stub
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
	public TextField createTextField(InputComponentListener changeListener,
			int maxLength) {
		return new TextField() {
			
			@Override
			public void setText(String text) {
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
			public String getText() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	public TextField createTextField(InputComponentListener changeListener,
			int maxLength, String allowedCharacters) {
		return createTextField(changeListener, maxLength);
	}

	@Override
	public FlowField createFlowField() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ComboBox<T> createComboBox(InputComponentListener changeListener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CheckBox createCheckBox(InputComponentListener changeListener, String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ITable<T> createTable(Object[] fields) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IComponent createLink(String text, String address) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Caption decorateWithCaption(final IComponent component, String caption) {
		return new Caption() {
			@Override
			public void setValidationMessages(List<String> validationMessages) {
			}
			
			@Override
			public IComponent getComponent() {
				return component;
			}
		};
	}

	@Override
	public HorizontalLayout createHorizontalLayout(IComponent... components) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SwitchContent createSwitchContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SwitchComponent createSwitchComponent(IComponent... components) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GridContent createGridContent(int columns, int columnWidth) {
		return new GridContent() {
			
			@Override
			public void add(IComponent field, int span) {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	public IDialog createDialog(IContext context, String title,
			IContent content, IAction... actions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showMessage(IContext context, String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showError(IContext context, String text) {
		if (lastError != null) {
			throw new IllegalStateException();
		}
		lastError = text;
	}

	@Override
	public void showConfirmDialog(IDialog component, String message,
			String title, ConfirmDialogType type, DialogListener listener) {
		if (nextConfirmAnswer == null) {
			throw new IllegalStateException();
		}
		listener.close(nextConfirmAnswer);
		nextConfirmAnswer = null;
	}
	
	@Override
	public OutputStream store(IContext context, String buttonText) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream load(IContext context, String buttonText) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> IDialog createSearchDialog(IContext context, Search<T> index, Object[] keys, TableActionListener<T> listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ILookup<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys) {
		// TODO Auto-generated method stub
		return null;
	}


}
