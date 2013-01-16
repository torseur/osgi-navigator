/*******************************************************************************
 * Copyright (c) 2008 by EclipseMozilla.org.
 * 
 * Based on portions Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thomas Derflinger tderflinger@gmail.com - refactoring based on the Eclipse ATF base.
 *******************************************************************************/
package org.esprit.pfe.osgibrowser.ui.browser;

import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipsemozilla.swt.browser.MozillaHelper;
import org.esprit.pfe.osgibrowser.Activator;
import org.esprit.pfe.osgibrowser.historique.Historique;
import org.esprit.pfe.osgibrowser.ui.toolbar.NavigationBar;
import org.esprit.pfe.osgibrowser.ui.toolbar.StatusBar;
import org.mozilla.interfaces.nsIDOMDocument;
import org.mozilla.interfaces.nsIDOMKeyEvent;
import org.mozilla.xpcom.XPCOMException;

/**
 * This is the Mozilla browser encapsulated as an editor. <br>
 * It contains a navigation bar and a status bar.
 * 
 */
public class MozBrowserEditor {

	public final static String ID = "org.eclipsemozilla.mozeditor.ui.browser.MozBrowserEditor";

	public static final String DEFAULT_URL = "about:blank";

	private Browser browser = null;

	protected NavigationBar navBar = null;

	protected StatusBar statusBar = null;
	
	private Historique historique = null;

	static int id = 0;
	MPart part;

	// actions
	protected Action backAction = null;
	protected Action forwardAction = null;
	protected Action refreshAction = null;
	protected Action stopAction = null;
	protected Action goAction = null;

	// currently loaded document
	// during a new load, this document will point to the old document until
	// the load is completed.
	protected nsIDOMDocument document = null;
	protected boolean loading = true;

	/*
	 * This enables the use of the mouse to click on an element in the browser
	 * and set it as the Selection.
	 */
	protected boolean controlSelectEnabled = false;

	protected MozillaBrowserListener browserListener = new MozillaBrowserListener(
			this);

	/*
	 * DOMDocumentContainer Support
	 */
	protected ListenerList domDocumentListeners = new ListenerList(); // nofified
	// of
	// document
	// loading
	// and
	// loaded
	protected ListenerList domMutationListeners = new ListenerList(); // notifies

	// changes
	// in
	// the
	// document's
	// structure
	@PostConstruct
	public void createPartControl(Composite parent,EPartService ePartService,@Active MPart part) {
		// In some cases createPartcontrol is called be the
		// early startup code sets the XULRunner path
		MozillaHelper.definedContributedXulRunner(null);
		//this.part = ePartService.findPart("org.esprit.pfe.osgibrowser.part."+id);
		this.part = part;
		ePartService.showPart(part, PartState.ACTIVATE);
		System.out.println("new Part "+part);
		id++;
		Composite displayArea = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginWidth = 1;
		gridLayout.marginHeight = 1;
		gridLayout.verticalSpacing = 1;
		displayArea.setLayout(gridLayout);

		GridData data;

		// Navigation Bar
		navBar = new NavigationBar(displayArea, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 1;
		navBar.getThis().setLayoutData(data);

		// separator
		Label upperBarSeparator = new Label(displayArea, SWT.SEPARATOR
				| SWT.HORIZONTAL | SWT.LINE_SOLID);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 1;
		upperBarSeparator.setLayoutData(data);

		// Actions for the Nav Bar
		createActions(); 
		// add the actions to the navBar
		navBar.setBackAction(backAction);
		navBar.setForwardAction(forwardAction);
		navBar.setRefreshAction(refreshAction);
		navBar.setStopAction(stopAction);
		navBar.setGoAction(goAction);

		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.horizontalSpan = 1;
		data.verticalSpan = 1;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = false;

		// Browser
		browser = new Browser(displayArea, SWT.FILL | SWT.MOZILLA);
		if(browser ==null)
			System.err.println("BROWSER WASN'T INSTANCIATE");
		browser.setJavascriptEnabled(true);
		// setting up the network observer (needs to be setup and connected even
		// if the view is not active so that all net calls are registered).
		// netMonAdapter = new MozNetworkMonitorAdapter( this );
		// netMonAdapter.connect();
		// TODO: for later, when I want to add debugging, I could enable the
		// network monitor

		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.horizontalSpan = 1;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		this.browser.setLayoutData(data);

		// toolbars = ToolbarExtensionManager.create(navBar, displayArea, this
		// );
		// configure toolbar via extension

		// separator
		Label lowerBarSeparator = new Label(displayArea, SWT.SEPARATOR
				| SWT.HORIZONTAL | SWT.LINE_SOLID);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 1;
		lowerBarSeparator.setLayoutData(data);

		// Status Bar
		statusBar = new StatusBar(displayArea, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		statusBar.getComposite().setLayoutData(data);
		
		// historique de navigation
		historique = new Historique();
		/*
		 * This object is an instance of an inner class that takes care of
		 * adapting events from the browser that deal with Loading and Progress
		 */
		browserListener.init();

		browser.setUrl("http://www.eclipsemozilla.org");
		historique.setCurrentUrl(browser.getUrl());
		
		//goToURL("http://www.eclipsemozilla.org");

	}
	
	public void activeAction(){
		backAction.setEnabled(historique.isBackEnabled());
		forwardAction.setEnabled(historique.isForwardEnabled());
	}
	
	/*
	 * This method creates all the actions that are added to the NavigationBar.
	 * These actions control the navigation aspects of the Browser, plus others.
	 */
	protected void createActions() {

		backAction =  new Action(null, IAction.AS_DROP_DOWN_MENU) {
			
			@Override
			public void  runWithEvent(Event event) {
				//run();
				System.err.println(" event :"+event.detail);
				
				if(event.detail == SWT.ARROW){
					historique.showPrevious(navBar.getThis().getShell(), 
							navBar.getToolBarLeftMenu(), browser);
					activeAction();
				}
				if(event.detail == 0){
//					browser.back();
					historique.goToPrevious(browser);
					activeAction();
				}
				

			}
		};
		
		backAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/browser/e_back.gif"));
		backAction.setDisabledImageDescriptor(Activator
				.getImageDescriptor("icons/browser/d_back.gif"));
		backAction.setEnabled(false);
		
		forwardAction = new Action(null, IAction.AS_DROP_DOWN_MENU) {
//			@Override
//			public void run() {
//				browser.forward();
//			}
			@Override
			public void  runWithEvent(Event event) {
				//run();
				System.err.println(" event :"+event.detail);
				
				if(event.detail == SWT.ARROW){
					historique.showNext(navBar.getThis().getShell(), 
							navBar.getToolBarLeftMenu(), browser);
					activeAction();
				}
				if(event.detail == 0){
					historique.goToNext(browser);
					activeAction();
				}
			}
		};
		forwardAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/browser/e_forward.gif"));
		forwardAction.setDisabledImageDescriptor(Activator
				.getImageDescriptor("icons/browser/d_forward.gif"));
		forwardAction.setEnabled(false);

		refreshAction = new Action(null, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				browser.refresh();
			}
		};

		refreshAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/browser/e_refresh.gif"));
		refreshAction.setDisabledImageDescriptor(Activator
				.getImageDescriptor("icons/browser/d_refresh.gif"));

		stopAction = new Action(null, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				browser.stop();
			}
		};

		stopAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/browser/e_stop.gif"));
		stopAction.setDisabledImageDescriptor(Activator
				.getImageDescriptor("icons/browser/d_stop.gif"));
		stopAction.setEnabled(false);

		goAction = new Action(null, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				goToURL(navBar.getLocationURL());
			}
		};

		goAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/browser/e_go.gif"));
		goAction.setDisabledImageDescriptor(Activator
				.getImageDescriptor("icons/browser/dgo.gif"));

	}

	/**
	 * Singleton that provides the main modifier key for the platform. For Linux
	 * and Windows, that is the 'Ctrl' key. For Mac OS X, it is 'Cmd' (Apple)
	 * key.
	 */
	protected static class OSModifierKey {
		private static OSModifierKey instance = new OSModifierKey();
		private long osModifierKeyCode;

		private OSModifierKey() {
			if (SWT.getPlatform() == "carbon")
				osModifierKeyCode = nsIDOMKeyEvent.DOM_VK_META;
			else
				osModifierKeyCode = nsIDOMKeyEvent.DOM_VK_CONTROL;
		}

		public static OSModifierKey getInstance() {
			return instance;
		}

		public long getKeyCode() {
			return osModifierKeyCode;
		}
	}

	public void setPartName(String partName) {
		// TODO Auto-generated method stub
		//super.setPartName(partName);
		part.setLabel(partName);
	}


	public void setTitleToolTip(String toolTip) {
		// TODO Auto-generated method stub
		//super.setTitleToolTip(toolTip);
		part.setTooltip(toolTip);
	}


	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}


	public void doSaveAs() {
		// TODO Auto-generated method stub

	}
	
	public Historique getHistorique() {
		return historique;
	}

	public Browser getMozillaBrowser() {
		return browser;
	}


	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		//		setSite(site);
		//		setInput(input);
	}


	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}


	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * This method is used to programatically change the URL pointed to by the
	 * embedded browser. It defaults to "about:blank" in the case of an error.
	 * 
	 * @TODO: Handle errors by instead displaying, for example, a 404 message
	 */
	public void goToURL(String url) {

		try {
			browser.setUrl(url);
//			if(!historique.getCurrentUrl().equals(browser.getUrl())){
//				historique.addUrlToPrevious();
//				historique.setCurrentUrl(browser.getUrl());
//			}
		} catch (XPCOMException xpcome) {
			// might be a bad URL so try opening with "about:blank"
			//browser.setUrl(DEFAULT_URL);
		}

	}

	public void clearCache() {

		// if( cacheService == null )
		// cacheService =
		// (nsICacheService)Mozilla.getInstance().getServiceManager().getServiceByContractID(
		// "@mozilla.org/network/cache-service;1",
		// nsICacheService.NS_ICACHESERVICE_IID );
		//		
		// /*
		// * for now since the NSI interface for nsICache in Java does not
		// provide access to
		// * the nsICache.STORE_ON_DISK and nsICache.STORE_IN_MEMORY, need to
		// pass the actual
		// * values. (Got values from LXR)
		// *
		// * const nsCacheStoragePolicy STORE_IN_MEMORY = 1;
		// * const nsCacheStoragePolicy STORE_ON_DISK = 2;
		// */
		// cacheService.evictEntries( 1 );
		// cacheService.evictEntries( 2 );

	}




	@Focus
	public void setFocus() {
		if (!browser.isDisposed())
			browser.setFocus();
	}

}
