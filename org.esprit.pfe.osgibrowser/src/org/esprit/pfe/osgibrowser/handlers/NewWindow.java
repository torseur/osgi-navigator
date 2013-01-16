 
package org.esprit.pfe.osgibrowser.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class NewWindow {
	
	@Inject
	EPartService ePartService;
	@Inject
	EModelService eModelService;
	@Inject
	MApplication application;
	
	@Execute
	public void execute() {
		//TODO Your code goes here
		MWindow mWindow = MBasicFactory.INSTANCE.createTrimmedWindow();
	    mWindow.setHeight(200);
	    mWindow.setWidth(400);
	    application.getChildren().add(mWindow);
	    
	    MPartSashContainer mPartSashContainer = MBasicFactory.INSTANCE.createPartSashContainer();
	    MPartStack mPartStack =	MBasicFactory.INSTANCE.createPartStack();
	    mPartSashContainer.getChildren().add(mPartStack);
	    
	    mWindow.getChildren().add(mPartSashContainer);
	    //System.out.println(newOnglet);
	    
	    
	}
		
}