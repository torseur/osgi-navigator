 
package org.esprit.pfe.osgibrowser.handlers;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;


public class NewOnglet {
	
	@Inject
	EPartService ePartService;
	@Inject
	EModelService eModelService;
	@Inject
	MApplication application;
	
		
	@Execute
	public void execute() 
			throws InvocationTargetException, InterruptedException {
		//TODO Your code goes here
//		IEclipseContext context = EclipseContextFactory.create();
//		// Add your Java objects to the context
//		context.set(NewOnglet.class.getName(), this);
		
		int id  = ePartService.getParts().size();
		
		MPart part = MBasicFactory.INSTANCE.createPart();
		part.setCloseable(true);
		part.setElementId("org.esprit.pfe.osgibrowser.part."+id);
		part.setContributionURI("bundleclass://org.esprit.pfe.osgibrowser/org.esprit.pfe.osgibrowser.ui.browser.MozBrowserEditor");
//		MPartStack mPartStack = (MPartStack) eModelService.find("org.esprit.pfe.osgibrowser.partstack.0",application );
//		mPartStack.getChildren().add(part);
		ePartService.getParts().add(part);
		
		
		System.out.println("new Part "+part);
//		try{
			ePartService.showPart(part, PartState.ACTIVATE);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
		
	}
		
}