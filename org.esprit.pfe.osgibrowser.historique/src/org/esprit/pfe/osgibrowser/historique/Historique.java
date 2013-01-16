package org.esprit.pfe.osgibrowser.historique;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;

public class Historique {
	private LinkedList<String> previous;
	private LinkedList<String> next;
	private String currentUrl = null;
	
	
	public Historique(){
		previous = new LinkedList<>();
		next = new LinkedList<>();
	}
	public Historique(String url){
		previous = new LinkedList<>();
		next = new LinkedList<>();
	}
	
	public String getCurrentUrl() {
		return currentUrl;
	}

	public void setCurrentUrl(String url) {
		//if(!"about:blank".equals(currentUrl) && !url.equals(currentUrl))
		//if(!url.equals(currentUrl))
			this.currentUrl = url;
	}
	
	public boolean isBackEnabled(){
		return !previous.isEmpty();
	}
	
	public boolean isForwardEnabled(){
		return !next.isEmpty();
	}

	public void addUrlToPrevious(){
			if(currentUrl!=null && !currentUrl.equals("about:blank")){
				previous.addFirst(currentUrl);
			}
			
	}

	public void goToPrevious(Browser browser){
		if(!previous.isEmpty()){
			next.addFirst(currentUrl);
			currentUrl = previous.removeFirst();
			browser.setUrl(currentUrl);
		}
	}
	
	public void goToNext(Browser browser){
		if(!next.isEmpty()){
			previous.addFirst(currentUrl);
			currentUrl = next.removeFirst(); 
			browser.setUrl(currentUrl);
		}
	}

	public void showPrevious(Composite parent,ToolBar toolBar, final Browser  browser){
		Menu menu = new Menu(parent);
		Rectangle rect = toolBar.getBounds();
		Point pt = new Point(rect.x, rect.y+ rect.height);
		pt = toolBar.toDisplay(pt);
		menu.setLocation(pt);
		
		for(int i = 0 ; i<previous.size(); i++){
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setID(i);
			item.setText(previous.get(i));
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					System.out.println("event.detail :"+e.detail);
					System.out.println("swt.menu_mousse :"+SWT.MENU_MOUSE);
					if(e.detail == SWT.MENU_MOUSE){
						System.out.println("item: :"+e.widget);
						int id = ((MenuItem)e.widget).getID();
						System.out.println("id :"+id);
						next.addFirst(currentUrl);
						for(int k = 0; k< id; k++){
							next.addFirst(previous.removeFirst());
						}
						currentUrl = previous.removeFirst();
						browser.setUrl(currentUrl);
						
					}
				}
			});
		}
		menu.setVisible(true);
		
	}
	
	public void showNext(Composite parent,ToolBar toolBar, final Browser  browser){
		Menu menu = new Menu(parent);
		Rectangle rect = toolBar.getItems()[1].getBounds();
		Point pt = new Point(rect.x, rect.y+ rect.height);
		pt = toolBar.toDisplay(pt);
		menu.setLocation(pt);
		
		for(int i = 0 ; i<next.size() ; i++){
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setID(i);
			item.setText(next.get(i));
			item.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if(e.detail == SWT.MENU_MOUSE){
						int id = ((MenuItem)e.widget).getID();
						previous.addFirst(currentUrl);
						for(int k = 0; k< id; k++){
							previous.addFirst(next.removeFirst());
						}
						currentUrl = next.removeFirst();
						browser.setUrl(currentUrl);
						
					}
				}
			});
		}
		
		menu.setVisible(true);
		
	}
	
	
	
}
