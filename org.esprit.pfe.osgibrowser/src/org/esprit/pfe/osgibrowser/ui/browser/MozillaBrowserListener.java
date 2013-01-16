/**
 * 
 */
package org.esprit.pfe.osgibrowser.ui.browser;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.esprit.pfe.osgibrowser.handlers.NewOnglet;
import org.mozilla.interfaces.nsIDOMDocument;
import org.mozilla.interfaces.nsIDOMEvent;
import org.mozilla.interfaces.nsIDOMEventListener;
import org.mozilla.interfaces.nsIDOMEventTarget;
import org.mozilla.interfaces.nsIDOMNode;
import org.mozilla.interfaces.nsIDOMNodeList;
import org.mozilla.interfaces.nsIDOMWindow;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIWebBrowser;
import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.XPCOMException;

/**
 * This piece updates the navigation bar and the status bar. <br>
 * Also manages the progress indicator.
 * 
 */
class MozillaBrowserListener implements ProgressListener, LocationListener,
StatusTextListener, nsIDOMEventListener, OpenWindowListener {
	@Inject
	EPartService ePartService;
	@Inject
	EModelService eModelService;
	@Inject
	MApplication application;

	/**
	 * 
	 */
	public final MozBrowserEditor mozBrowserEditor;

	/**
	 * @param mozBrowserEditor
	 */
	MozillaBrowserListener(MozBrowserEditor mozBrowserEditor) {
		this.mozBrowserEditor = mozBrowserEditor;
	}

	public void init() {
		this.mozBrowserEditor.getMozillaBrowser().addLocationListener(this);
		this.mozBrowserEditor.getMozillaBrowser().addStatusTextListener(this);
		this.mozBrowserEditor.getMozillaBrowser().addOpenWindowListener(this);
		this.mozBrowserEditor.getMozillaBrowser().addDisposeListener(
				new DisposeListener() {

					public void widgetDisposed(DisposeEvent e) {
						MozillaBrowserListener.this.mozBrowserEditor
						.getMozillaBrowser().removeLocationListener(
								MozillaBrowserListener.this);
						MozillaBrowserListener.this.mozBrowserEditor
						.getMozillaBrowser().removeStatusTextListener(
								MozillaBrowserListener.this);
						MozillaBrowserListener.this.mozBrowserEditor
						.getMozillaBrowser()
						.removeDisposeListener(this);
					}

				});
	}

	/*
	 * This event is not used because it's proven unreliable and the information
	 * provided is not enough to do anything useful.
	 */
	public void changing(LocationEvent event) {
	}

	/*
	 * LocationEvent:changed is the main event that notifies that a new request
	 * is being done. In this event, need to make sure that we are dealing with
	 * the top window (and document)
	 * 
	 * 1. hook progress listener to keep track of the loading progress 2. hook
	 * to listen when the content document of the window is loaded.
	 * 
	 */
	public void changed(LocationEvent event) {

		/*
		 * Need to ignore "about:blank". When opening a new browser this event
		 * is for some reason fired for about:blank before setting the correct
		 * url.
		 * 
		 */

		if ("about:blank".equals(event.location))
			return;

		if (event.top) {

			// hoow listeners to window
			nsIDOMWindow window = ((nsIWebBrowser) this.mozBrowserEditor.getMozillaBrowser()
					.getWebBrowser()).getContentDOMWindow();
			nsIDOMEventTarget windowEventTarget = (nsIDOMEventTarget) window
					.queryInterface(nsIDOMEventTarget.NS_IDOMEVENTTARGET_IID);

			// add progress listener
			this.mozBrowserEditor.getMozillaBrowser().addProgressListener(this);
			// show progress in status bar
			this.mozBrowserEditor.statusBar.showProgress(0);

			// whichever happens first
			windowEventTarget.addEventListener("pageshow", this, true);
			windowEventTarget.addEventListener("DOMContentLoaded", this, true);

			// whichever happens first
			windowEventTarget.addEventListener("pagehide", this, true);
			windowEventTarget.addEventListener("unload", this, true);

			// Set the tool tip to the browser url address
			this.mozBrowserEditor.setTitleToolTip(this.mozBrowserEditor.getMozillaBrowser()
					.getUrl());
			
			
			// show the current URL in the navBar
			this.mozBrowserEditor.navBar.setLocationURL(this.mozBrowserEditor
					.getMozillaBrowser().getUrl());
			
			//handle historic page
			if(!this.mozBrowserEditor.getHistorique().getCurrentUrl().equals(
					this.mozBrowserEditor.getMozillaBrowser().getUrl())){
				
				this.mozBrowserEditor.getHistorique().addUrlToPrevious();
				
				this.mozBrowserEditor.getHistorique().setCurrentUrl(
						this.mozBrowserEditor.getMozillaBrowser().getUrl());
			}

			// this is call once after a URL change so it is a good place
			// for this
			this.mozBrowserEditor.stopAction.setEnabled(true);

			// save the current url in the memento object
			//			MozBrowserEditorInput input = (MozBrowserEditorInput) this.mozBrowserEditor
			//					.getEditorInput();
			//			input.setURL(event.location);
			MozBrowserEditorInput input =  new MozBrowserEditorInput(this.mozBrowserEditor.getMozillaBrowser().getUrl());

			// clear all the selection info
			// changeSelection(null);

		}
	}

	public void changed(ProgressEvent event) {

		// refresh the progress bar
		if (event.total > 0) {

			int ratio = event.current * 100 / event.total;

			// show progress in status bar
			this.mozBrowserEditor.statusBar.showProgress(ratio);
		} else {
			// @GINO: Temp Hack because total may come as -1
			this.mozBrowserEditor.statusBar.showProgress(50);
		}

	}

	/*
	 * This method has the potential of getting called more than once (when
	 * there are Frames). The problem is that it does not provide enough context
	 * information so the only things that are done here are things that can be
	 * performed more than once without getting undesirable behavoir.
	 * 
	 * We are no using the data member of the event to send additional info
	 * 
	 */
	public void completed(ProgressEvent event) {

		// the event.data should have information about whether the progress
		// has to do with the top document
		if (event.widget instanceof Browser) {

			// remove the progress listener
			this.mozBrowserEditor.getMozillaBrowser().removeProgressListener(
					this);

			// change state of actions
//			this.mozBrowserEditor.backAction
//			.setEnabled(this.mozBrowserEditor.getMozillaBrowser().isBackEnabled());
//			this.mozBrowserEditor.forwardAction
//			.setEnabled(this.mozBrowserEditor.getMozillaBrowser()
//					.isForwardEnabled());
//			this.mozBrowserEditor.stopAction.setEnabled(false);
			
			this.mozBrowserEditor.backAction.setEnabled(mozBrowserEditor.getHistorique()
					.isBackEnabled());
			this.mozBrowserEditor.forwardAction.setEnabled(mozBrowserEditor.getHistorique()
					.isForwardEnabled());

			// show done in the progress bar
			this.mozBrowserEditor.statusBar.progressDone(); // done
			this.mozBrowserEditor.statusBar.setStatusText("");

			// this fixes a problem with giving the browser focus after
			// entering an URL
			// in the NavBar
			this.mozBrowserEditor.setFocus();

			// enable the cache clear action
			// clearCacheAction.setEnabled(true);
			// TODO investigate if we need this here.
		}

	}

	/*
	 * Updating the status bar with new text of the activity in the browser
	 */
	public void changed(StatusTextEvent event) {
		this.mozBrowserEditor.statusBar.setStatusText(event.text);

	}

	/*
	 * Here we handle the real Load and Unload events for the window. When the
	 * target is the top document, the handle is removed.
	 * 
	 * Also, when it is the top document, the browser will adds/remove all event
	 * handlers that it attaches.
	 * 
	 * It seems that it is only getting called for the top frame.
	 */
	public void handleEvent(nsIDOMEvent event) {
		String eventType = event.getType();

		if ("pageshow".equals(eventType)
				|| "DOMContentLoaded".equals(eventType)) {
			// do page show

			nsIDOMWindow topWindow = ((nsIWebBrowser) this.mozBrowserEditor.getMozillaBrowser()
					.getWebBrowser()).getContentDOMWindow();

			nsIDOMEventTarget eventTarget = event.getTarget();
			nsIDOMDocument documentEventTarget = (nsIDOMDocument) eventTarget
					.queryInterface(nsIDOMDocument.NS_IDOMDOCUMENT_IID);

			// check if top
			boolean isTop = topWindow.getDocument().equals(documentEventTarget);
			if (isTop) {
				// done loading, document is ready for use
				this.mozBrowserEditor.loading = false;
				this.mozBrowserEditor.document = topWindow.getDocument();

				// Set title of tab
				if (this.mozBrowserEditor.document != null
						& this.mozBrowserEditor.document
						.getElementsByTagName("TITLE") != null) {
					nsIDOMNodeList titleNodes = this.mozBrowserEditor.document
							.getElementsByTagName("TITLE");
					if (titleNodes.getLength() == 1) {
						nsIDOMNode node = titleNodes.item(0);
						if (node.getFirstChild() != null) {
							
							String title = node.getFirstChild().getNodeValue();
							if (!title.equals("")) {
								// Show first 20 characters of page title or
								// append "..." if greater than 20
								title = title.length() > 20 ? title.substring(
										0, 20)
										+ "..." : title;
								System.out.println("title :"+title);
								this.mozBrowserEditor.setPartName(title);
							}
						}
					}
				}

				// detach listeners

				try {
					event.getCurrentTarget().removeEventListener("pageshow",
							this, true);
				} catch (XPCOMException e) {
					// ignore
				}
				try {
					event.getCurrentTarget().removeEventListener(
							"DOMContentLoaded", this, true);
				} catch (XPCOMException e) {
					// ignore
				}

				// create a selection box for the new document
				// selectionBox = new SelectionBox( getDocument() );
				// TODO: investigate later if we need that

				// hook the nsIDOMMutationEvent handlers
				// hookDOMMutationEvents();
				// TODO: investigate later if we need that

				// need to maintain the feature enabled if the document
				// changes
				// hookAllMouseEvents();
				// hookKeyEvents();

			}

			// notify all listeners of document loaded
			final DOMDocumentEvent e = new DOMDocumentEvent(
					documentEventTarget, isTop);
			Object[] listeners = this.mozBrowserEditor.domDocumentListeners
					.getListeners();
			for (int i = 0; i < listeners.length; ++i) {
				final IDOMDocumentListener l = (IDOMDocumentListener) listeners[i];
				SafeRunnable.run(new SafeRunnable() {
					public void run() {
						l.documentLoaded(e);
					}
				});
			}

		} else if ("pagehide".equals(eventType) || "unload".equals(eventType)) {
			// do page hide

			// detach listeners
			nsIDOMWindow topWindow = ((nsIWebBrowser) this.mozBrowserEditor.getMozillaBrowser()
					.getWebBrowser()).getContentDOMWindow();

			nsIDOMEventTarget eventTarget = event.getTarget();
			nsIDOMDocument documentEventTarget = (nsIDOMDocument) eventTarget
					.queryInterface(nsIDOMDocument.NS_IDOMDOCUMENT_IID);

			// check if top
			boolean isTop = topWindow.getDocument().equals(documentEventTarget);

			if (isTop) {
				// set the state to loading so that clients know to not
				// use the current document for anything but cleanup
				this.mozBrowserEditor.loading = true;
			}

			/*
			 * notify before the document is set to null
			 */
			// notify all listeners of document unloaded
			final DOMDocumentEvent e = new DOMDocumentEvent(
					documentEventTarget, isTop);
			Object[] listeners = this.mozBrowserEditor.domDocumentListeners
					.getListeners();
			for (int i = 0; i < listeners.length; ++i) {
				final IDOMDocumentListener l = (IDOMDocumentListener) listeners[i];
				SafeRunnable.run(new SafeRunnable() {
					public void run() {
						l.documentUnloaded(e);
					}
				});
			}

			if (isTop) {
				try {
					event.getCurrentTarget().removeEventListener("pagehide",
							this, true);
				} catch (XPCOMException xpe) {
					// ignore
				}

				try {
					event.getCurrentTarget().removeEventListener("unload",
							this, true);
				} catch (XPCOMException xpe) {
					// ignore
				}

				// remove all listeners
				if (this.mozBrowserEditor.document != null) {
					// unhookDOMMutationEvents();
					// unhookAllMouseEvents();
					// unhookKeyEvents();

					this.mozBrowserEditor.document = null;
				}
			}
		}
	}

	public nsISupports queryInterface(String id) {
		return Mozilla.queryInterface(this, id);
	}

	@Inject
	IEclipseContext context;
	@Override
	public void open(WindowEvent event) {
		// TODO Auto-generated method stub

				NewOnglet onglet =ContextInjectionFactory.make(NewOnglet.class,context);
				System.err.println(onglet);
	//			try {
					//onglet.execute();
//				} catch (InvocationTargetException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
		//int id  = ePartService.getParts().size();
		
//		MPart part = MBasicFactory.INSTANCE.createPart();
//		part.setCloseable(true);
//		part.setElementId("org.esprit.pfe.osgibrowser.part."+id);
//		part.setContributionURI("bundleclass://org.esprit.pfe.osgibrowser/org.esprit.pfe.osgibrowser.ui.browser.MozBrowserEditor");
//		MPartStack mPartStack = (MPartStack) eModelService.find("org.esprit.pfe.osgibrowser.partstack.0",application );
//		mPartStack.getChildren().add(part);
//		ePartService.getParts().add(part);
//		System.out.println("new Part "+part);
		//ePartService.showPart(part, PartState.ACTIVATE);
		
	}
}