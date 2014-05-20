package org.minimalj.frontend.lanterna.component;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.component.Button;

public class LanternaMenu extends Button {

	public LanternaMenu(String text) {
		super(text, new LanternaMenuAction());
	}

	
	private static class LanternaMenuAction implements Action {

		@Override
		public void doAction() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
