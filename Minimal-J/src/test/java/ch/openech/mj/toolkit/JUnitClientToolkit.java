package ch.openech.mj.toolkit;

import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.io.InputStream;
import java.util.List;

import javax.swing.event.ChangeListener;

public class JUnitClientToolkit extends ClientToolkit {

	private String lastError = null;
	private int nextConfirmAnswer = Integer.MIN_VALUE;
	
	public String pullError() {
		String error = lastError;
		lastError = null;
		return error;
	}

	public void setNextConfirAnswer(int answer) {
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
	public TextField createTextField(ChangeListener changeListener,
			int maxLength) {
		return new TextField() {
			
			@Override
			public void setText(String text) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setFocusListener(FocusListener focusListener) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setEnabled(boolean editable) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setCommitListener(ActionListener listener) {
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
	public TextField createTextField(ChangeListener changeListener,
			int maxLength, String allowedCharacters) {
		return createTextField(changeListener, maxLength);
	}

	@Override
	public FlowField createFlowField() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ComboBox<T> createComboBox(ChangeListener changeListener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CheckBox createCheckBox(ChangeListener changeListener, String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ITable<T> createTable(Class<T> clazz, Object[] fields) {
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
	public SwitchLayout createSwitchLayout() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GridFormLayout createGridLayout(int columns, int columnWidth) {
		return new GridFormLayout() {
			
			@Override
			public void add(IComponent field, int span) {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	public IComponent createFormAlignLayout(IComponent content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDialog createDialog(IComponent parent, String title,
			IComponent content, IAction... actions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showMessage(Object parent, String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showError(Object parent, String text) {
		if (lastError != null) {
			throw new IllegalStateException();
		}
		lastError = text;
	}

	@Override
	public void showConfirmDialog(IComponent component, String message,
			String title, int type, ConfirmDialogListener listener) {
		if (nextConfirmAnswer == Integer.MIN_VALUE) {
			throw new IllegalStateException();
		}
		listener.onClose(nextConfirmAnswer);
		nextConfirmAnswer = Integer.MIN_VALUE;
	}

	@Override
	public void export(IComponent parent, String buttonText,
			ExportHandler exportHandler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InputStream imprt(IComponent parent, String buttonText) {
		// TODO Auto-generated method stub
		return null;
	}

}
