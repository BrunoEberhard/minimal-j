package org.minimalj.frontend.lanterna.component;

import java.util.List;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.layout.LayoutManager;
import com.googlecode.lanterna.gui.layout.LayoutParameter;
import com.googlecode.lanterna.terminal.TerminalSize;

public class FormLayout implements LayoutManager {

    public FormLayout() {
		super();
	}

	@Override
	public TerminalSize getPreferredSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends LaidOutComponent> layout(TerminalSize layoutArea) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean maximisesVertically() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean maximisesHorisontally() {
		return true;
	}

	@Override
	public void addComponent(Component component, LayoutParameter... parameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeComponent(Component component) {
		// TODO Auto-generated method stub
		
	}
	
	public class ColumnsLayoutParameter extends LayoutParameter {
		private int colSpan;
		
		protected ColumnsLayoutParameter(int colSpan) {
			super("colSapn = " +colSpan);
		}
		
	}

}
