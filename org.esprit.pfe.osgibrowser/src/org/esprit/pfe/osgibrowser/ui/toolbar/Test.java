package org.esprit.pfe.osgibrowser.ui.toolbar;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class Test {

	public Test() {
	}

	/**
	 * Create contents of the view part.
	 */
	@PostConstruct
	public void createControls(Composite parent) {
		
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("New Item");
		
		final ToolBar toolBar = new ToolBar(tabFolder, SWT.FLAT | SWT.RIGHT);
		tabItem.setControl(toolBar);
		
		final ToolItem tltmDropdownItem = new ToolItem(toolBar, SWT.DROP_DOWN);
		final Menu menu = new Menu(parent);
		toolBar.setMenu(menu);
		tltmDropdownItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(e.detail == SWT.ARROW){
					Rectangle rect = tltmDropdownItem.getBounds();
					Point pt = new Point(rect.x, rect.y + rect.height);
					pt = toolBar.toDisplay(pt);
					menu.setLocation(pt.x, pt.y);
					menu.setVisible(true);
				}
			}
		});
		tltmDropdownItem.setText("DropDown Item");
		
		
		MenuItem mntmNew = new MenuItem(menu, SWT.NONE);
		mntmNew.setText("new");
		
		final ToolItem tltmNewItem = new ToolItem(toolBar, SWT.ARROW_DOWN);
		tltmNewItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(e.detail == SWT.ARROW){
					
				}
			}
		});
		tltmNewItem.setText("New Item");
		
		
		
		TabItem tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem.setText("New Item");
		
		TabItem tabItem_1 = new TabItem(tabFolder, SWT.NONE);
		tabItem_1.setText("New Item");
		
		TabItem tbtmNewItem_1 = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem_1.setText("New Item");
		
		DragSource dragSource = new DragSource(tabFolder, DND.DROP_MOVE);
		
		DropTarget dropTarget = new DropTarget(tabFolder, DND.DROP_MOVE);
	}

	@PreDestroy
	public void dispose() {
	}

	@Focus
	public void setFocus() {
		// TODO	Set the focus to control
	}
}
