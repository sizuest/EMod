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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.gui.utils.ShowButtons;

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
	
	public AConfigGUI(Composite parent, int style, int buttons) {
		super(parent, style);
		init(buttons);
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
		init(ShowButtons.ALL);
	}
	
	private void init(int buttons){
		
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		if(ShowButtons.count(buttons)>0){
			this.setLayout(new GridLayout(ShowButtons.count(buttons), true));
			content = new Composite(this, SWT.NONE );
			content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, ShowButtons.count(buttons), 1));
		}
		else{
			this.setLayout(new GridLayout(1, true));
			content = new Composite(this, SWT.NONE );
			content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		}
		
		content.setLayout(new GridLayout( 1, true));
		

		
		if(ShowButtons.cancel(buttons)){
			buttonCancel = new Button(this, SWT.NONE);
			if(ShowButtons.reset(buttons) | ShowButtons.ok(buttons))
				buttonCancel.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
			else
				buttonCancel.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false));
			buttonCancel.setText("Cancel");
			buttonCancel.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					close();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {/* Not used */}
			});
		}
		
		if(ShowButtons.reset(buttons)){
			buttonReset = new Button(this, SWT.NONE);
			if(!ShowButtons.cancel(buttons) & ShowButtons.ok(buttons))
				buttonReset.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
			if(ShowButtons.cancel(buttons) & !ShowButtons.ok(buttons))
				buttonReset.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
			else
				buttonReset.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false));
			
			buttonReset.setText("Reset");
			buttonReset.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					askForSaving();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {/* Not used */}
			});
		}
		if(ShowButtons.ok(buttons)){
			buttonSave = new Button(this, SWT.NONE);
			
			if(ShowButtons.reset(buttons) | ShowButtons.cancel(buttons))
				buttonSave.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
			else
				buttonSave.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, false));
			
			buttonSave.setText("OK");
			buttonSave.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					save();
					wasEdited = false;
					close();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {/* Not used */}
			});
		}
	}
	
	public abstract void save();
	
	public abstract void reset();
	
	public void close(){
		if(wasEdited){
			Shell dialog = askForSavingGUI();
			dialog.addDisposeListener(new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {
					dispose();
				}
			});
		}
		else	
			this.dispose();
	}
	
	public void askForSaving(){
		if(wasEdited){
			Shell dialog = askForSavingGUI();
			dialog.addDisposeListener(new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {
					reset();
				}
			});
		}
		else	
			reset();
		
		wasEdited = false;
	}
	
	protected Shell askForSavingGUI(){
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
		
		return dialog;
	}
}
