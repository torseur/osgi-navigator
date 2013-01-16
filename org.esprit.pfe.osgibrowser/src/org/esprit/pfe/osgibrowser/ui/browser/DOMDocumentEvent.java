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

import org.mozilla.interfaces.nsIDOMDocument;

/**
 * This event object contains context information relevant to event
 * related to DOM documents
 * 
 * @author Gino Bustelo
 */
public class DOMDocumentEvent {
	protected nsIDOMDocument targetDocument;
	
	protected boolean isTop; //true if topmost document, false if document in frame
	
	
	public DOMDocumentEvent( nsIDOMDocument targetDocument, boolean isTop ){
		this.targetDocument = targetDocument;
		this.isTop = isTop;
	}
	
	public boolean isTop() {
		return isTop;
	}
	public nsIDOMDocument getTargetDocument() {
		return targetDocument;
	}
	
	
}
