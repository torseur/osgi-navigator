/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.esprit.pfe.osgibrowser.ui.browser;

public interface IDOMDocumentListener {
	
	/*
	 * Signals that the event.targetDocument is fully loaded
	 */
	void documentLoaded( DOMDocumentEvent event );
	
	/*
	 * Signals that the event.targetDocument is unloaded
	 */
	void documentUnloaded( DOMDocumentEvent event );
}
