/*
 * Copyright 2012, Bruno Eberhard, Open-eCH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minimalj.frontend.swing.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class HideableTabbedPane extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private JTabbedPane tabbedPane;
	private final Map<Component, String> titles = new HashMap<Component, String>();
	private final Map<Component, String> tooltips = new HashMap<Component, String>();
	private final Map<Component, Icon> icons = new HashMap<Component, Icon>();
	
	public HideableTabbedPane() {
		super(new BorderLayout());
	}
	
	public void addTab(String title, Component tab) {
		addTab(title, null, tab, null);
    }
	
	public void addTab(String title, Icon icon, Component tab, String tip) {
		titles.put(tab, title);
		icons.put(tab, icon);
		tooltips.put(tab, tip);
		
		if (getComponentCount() == 0) {
			add(tab, BorderLayout.CENTER);
		} else if (tabbedPane != null) {
			tabbedPane.addTab(title, icon, tab, tip);
		} else {
			tabbedPane = new JTabbedPane();
			Component firstComponent = getComponent(0);
			remove(firstComponent);
			tabbedPane.addTab(titles.get(firstComponent), icons.get(firstComponent), firstComponent, tooltips.get(firstComponent));
			tabbedPane.addTab(title, icon, tab, tip);
			add(tabbedPane, BorderLayout.CENTER);
		}
	}
	
	public void removeTab(JComponent tab) {
		titles.remove(tab);
		icons.remove(tab);
		tooltips.remove(tab);
		
		if (tabbedPane != null) {
			tabbedPane.remove(tab);
			if (tabbedPane.getComponentCount() == 1) {
				Component firstComponent = tabbedPane.getComponentAt(0);
				remove(tabbedPane);
				tabbedPane = null;
				add(firstComponent, BorderLayout.CENTER);
			} else {
				tabbedPane.remove(tab);
			}
		} else if (getComponentCount() > 0) {
			if (getComponent(0).equals(tab)) {
				remove(tab);
			}
		}
	}

	public int getTabCount() {
		if (getComponentCount() == 0) {
			return 0;
		} else if (tabbedPane != null) {
			return tabbedPane.getTabCount();
		} else {
			return 1;
		}
	}

	public Component getTab(int index) {
		if (getComponentCount() == 0) {
			return null;
		} else if (tabbedPane != null) {
			return tabbedPane.getComponentAt(index);
		} else {
			return getComponent(0);
		}
	}

	public void setSelectedComponent(Component component) {
		if (tabbedPane != null) {
			tabbedPane.setSelectedComponent(component);
		}
	}

	public Component getSelectedComponent() {
		if (getComponentCount() == 0) {
			return null;
		} else if (tabbedPane != null) {
			return tabbedPane.getSelectedComponent();
		} else {
			return getComponent(0);
		}
	}

	public void setTitleAt(int index, String title) {
		titles.put(getTab(index), title);
		if (tabbedPane != null) {
			tabbedPane.setTitleAt(index, title);
		}
	}

	@Deprecated // at the moment Page doesn't support Icon anymore
	public void setIconAt(int index, Icon titleIcon) {
		icons.put(getTab(index), titleIcon);
		if (tabbedPane != null) {
			tabbedPane.setIconAt(index, titleIcon);
		}
	}

	@Deprecated // at the moment Page doesn't support Tooltip anymore
	public void setToolTipTextAt(int index, String titleToolTip) {
		tooltips.put(getTab(index), titleToolTip);
		if (tabbedPane != null) {
			tabbedPane.setToolTipTextAt(index, titleToolTip);
		}
	}

}
