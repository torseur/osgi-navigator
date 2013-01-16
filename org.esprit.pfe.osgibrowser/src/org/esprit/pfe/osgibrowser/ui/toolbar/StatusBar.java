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

package org.esprit.pfe.osgibrowser.ui.toolbar;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

public class StatusBar{

	protected Label status = null;
	protected ProgressBar progressBar = null;
	private Composite This;
	
	public StatusBar(Composite parent, int style) {
		//super(parent, style);
		
		createUI(parent);
	}
	@PostConstruct
	protected void createUI(Composite parent){
		This = new Composite(parent, SWT.None);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		This.setLayout(gridLayout);
		GridData data;
		
		status = new Label(This, SWT.NONE);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		data.horizontalIndent = 2;
		status.setLayoutData(data);

		progressBar = new ProgressBar(This, SWT.NONE);
		data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		progressBar.setLayoutData(data);
		progressBar.setMaximum( 100 );
		progressBar.setMinimum( 0 );
	}
	
	public void setStatusText( String statusText ){
		status.setText( statusText );
	}
	
	public void showProgress( int percent ){
		if( 0 <= percent && percent <= 100 ){
			if( !progressBar.isVisible() ){
				progressBar.setVisible( true );
			}
			progressBar.setSelection( percent );
		}
	}
	
	public void progressDone(){
		progressBar.setVisible( false );
		This.layout();
	}
	
	public Composite getComposite(){
		return This;
	}
}
