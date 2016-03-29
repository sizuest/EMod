/***********************************
 * $Id: SimGUI.java 203 2016-02-26 14:54:24Z sizuest $
 *
 * $URL: https://icvrdevil.ethz.ch/svn/EMod/trunk/ch.ethz.inspire.emod/src/ch/ethz/inspire/emod/gui/SimGUI.java $
 * $Author: sizuest $
 * $Date: 2016-02-26 15:54:24 +0100 (Fre, 26. Feb 2016) $
 * $Rev: 203 $
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Abtract class for configuration GUIs with cancel, reset and save button
 * 
 * @author sizuest
 *
 */

public abstract class AConfigGUI extends Composite{

	protected Button buttonSave, 
					 buttonReset, 
					 buttonCancel;
	
	protected Composite content;
	
	boolean wasEdited = false;
	
	public AConfigGUI(Composite parent, int style, boolean showCancel) {
		super(parent, style);
		init(showCancel);
	}
	
	public AConfigGUI(Composite parent, int style) {
		super(parent, style);
		init();
	}
	
	public Composite getContent(){
		return content;
	}
	
	public void wasEdited(){
		wasEdited = true;
	}
	
	public boolean getEditedState(){
		return wasEdited;
	}
	
	private void init(){
		init(true);
	}
	
	private void init(boolean showCancel){
		this.setLayout(new GridLayout(3, true));
		
		content = new Composite(this, SWT.NONE );
		if(showCancel){
			this.setLayout(new GridLayout(3, true));
			content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		}
		else{
			this.setLayout(new GridLayout(2, true));
			content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		}
		
		content.setLayout(new GridLayout(1, true));
		
		if(showCancel){
			buttonCancel = new Button(this, SWT.NONE);
			buttonCancel.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
			buttonCancel.setText("Close");
			buttonCancel.setVisible(showCancel);
			buttonCancel.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					close();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {/* Not used */}
			});
		}
		
		buttonReset = new Button(this, SWT.NONE);
		if(showCancel)
			buttonReset.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false));
		else
			buttonReset.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
		
		buttonReset.setText("Reset");
		buttonReset.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				askForSaving();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */}
		});
		
		buttonSave = new Button(this, SWT.NONE);
		buttonSave.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
		buttonSave.setText("Save");
		buttonSave.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				save();
				wasEdited = false;
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */}
		});
	}
	
	public abstract void save();
	
	public abstract void reset();
	
	public void close(){
		if(wasEdited)
			askForSavingGUI();
			
		this.dispose();
	}
	
	public void askForSaving(){
		if(wasEdited)
			askForSavingGUI();
		else
			reset();
		
		wasEdited = false;
	}
	
	protected void askForSavingGUI(){
		final Shell dialog = new Shell(this.getShell(), SWT.APPLICATION_MODAL);
		Button cancel, save;
		Text text;
		
		dialog.setLayout(new GridLayout(2, true));
		dialog.setSize(200, 200);
		dialog.setText("Configuration was modified");
		
		text = new Text(dialog, SWT.NONE);
		text.setText("Save changes made?");
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		cancel = new Button(dialog, SWT.NONE);
		cancel.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
		cancel.setText("No");
		cancel.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				reset();
				dialog.dispose();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */}
		});
		
		save = new Button(dialog, SWT.NONE);
		save.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
		save.setText("Yes");
		save.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				save();
				dialog.dispose();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */}
		});
		
		dialog.pack();
		dialog.open();
	}
}
