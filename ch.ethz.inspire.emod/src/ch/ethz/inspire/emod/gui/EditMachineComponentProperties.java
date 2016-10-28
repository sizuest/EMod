/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/

package ch.ethz.inspire.emod.gui;

import java.io.File;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * Edit the name and type of a machine component included in the model
 * 
 * @author sizuest
 *
 */

public class EditMachineComponentProperties extends AConfigGUI {
	
	private Text textName;
	private Combo comboType;
	private MachineComponent mc;

	public EditMachineComponentProperties(final Composite parent, int style, final MachineComponent mc) {
		super(parent, style);
		
		this.mc = mc;
		
		this.getContent().setLayout(new GridLayout(3, false));
		
		Label labelName = new Label(this.getContent(), SWT.TRANSPARENT);
		labelName.setText("Name");
		
		textName = new Text(this.getContent(), SWT.BORDER);
		textName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		textName.setText(mc.getName());
		textName.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				wasEdited();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				wasEdited();
			}
		});
		
		Label labelType = new Label(this.getContent(), SWT.TRANSPARENT);
		labelType.setText("Type");
		
		comboType = new Combo(this.getContent(), SWT.NONE);
		comboType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		comboType.setText(mc.getComponent().getType());
		
		String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/" + mc.getComponent().getModelType() + "/";
		File subdir = new File(path);
    	
    	//check if the directory exists, then show possible parameter sets to select
    	if(subdir.exists()){
    		String[] subitems = subdir.list();
    		
    		//remove the "Type_" and the ".xml" part of the filename
    		for(int i=0; i < subitems.length; i++){
    			subitems[i] = subitems[i].replace(mc.getComponent().getModelType() + "_", "");
    			subitems[i] = subitems[i].replace(".xml", "");
    		}   
    		
    		//sort by name
    		Arrays.sort(subitems);
    		
    		//set the possible parameter sets to the combo
    		comboType.setItems(subitems);
    		comboType.setText(mc.getComponent().getType());
    	}
    	
    	comboType.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				wasEdited();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */}
		});
    	
    	final Button buttonEditComponent = new Button(this.getContent(), SWT.PUSH);
        buttonEditComponent.setText("...");
        buttonEditComponent.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true, 1, 1));
        buttonEditComponent.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){      		
        		String model = mc.getComponent().getModelType();
        		String type  = comboType.getText();
        		//open window editComponentEditGUI with the selected component
        		EditMachineComponentGUI.editMachineComponentGUI(parent.getShell(), model, type);
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		// Not used
        	}
        });
        buttonEditComponent.pack();
		
	}
	
	/**
	 * Component Edit GUI for editing a existing Component of the Component DB
 	 * @param parent 
	 * @param mc 
 	 * @param comboType 
 	 * @param parameter 
	 * @return 
	 */
    public static Shell editMachineComponentGUI(final Shell parent, final MachineComponent mc){
    	final Shell shell = new Shell(parent, SWT.TITLE|SWT.SYSTEM_MODAL| SWT.CLOSE | SWT.MAX);
        shell.setText(LocalizationHandler.getItem("app.gui.compdb.editcomp"));
        shell.setLayout(new GridLayout(1, true));
    	
        EditMachineComponentProperties gui = new EditMachineComponentProperties(shell, SWT.NONE, mc);
		
    	shell.pack();
		
		shell.layout();
		shell.redraw();
		shell.open();
		
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				parent.setEnabled(true);
			}
		});
		
		gui.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				shell.dispose();
			}
		});
		
		return shell;
    }

	@Override
	public void save() {
		mc.setName(textName.getText());
		mc.getComponent().setType(comboType.getText());
	}

	@Override
	public void reset() {
		textName.setText(mc.getName());
		comboType.setText(mc.getComponent().getType());
	}

}
