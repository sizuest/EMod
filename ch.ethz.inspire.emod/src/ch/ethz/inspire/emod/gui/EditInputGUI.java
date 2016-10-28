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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.gui.utils.ShowButtons;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.SiUnitDefinition;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.simulation.GeometricKienzleSimulationControl;
import ch.ethz.inspire.emod.simulation.ProcessSimulationControl;
import ch.ethz.inspire.emod.simulation.StaticSimulationControl;
import ch.ethz.inspire.emod.utils.ConfigReader;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

public class EditInputGUI extends AConfigGUI {
	
	private Text textName;
	private Combo comboUnit;
    protected ConfigReader input;
    protected AEditInputComposite inputComposite = null;
    
    private ASimulationControl sc;

    public EditInputGUI(Composite parent, int style, String type, String parameter){
    	super(parent, style, ShowButtons.ALL);
    	
    	sc = null;
    	
    	init();
    }
    
    public EditInputGUI(Composite parent, int style, final ASimulationControl sc){
    	super(parent, style, ShowButtons.ALL);
    	
    	this.sc = sc;
    	
    	init();
    }
    	
    private void init(){
    	
    	this.getContent().setLayout(new GridLayout(2, false));
    	
    	Label labelName = new Label(this.getContent(), SWT.TRANSPARENT);
		labelName.setText("Name");
		labelName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		textName = new Text(this.getContent(), SWT.BORDER);
		textName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		textName.setText(sc.getName());
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
		
		Label labelUnit = new Label(this.getContent(), SWT.TRANSPARENT);
		labelUnit.setText("Unit");
		labelUnit.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		comboUnit = new Combo(this.getContent(), SWT.NONE);
		comboUnit.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		String[] items = new String[SiUnitDefinition.getConversionMap().keySet().size()]; 
        SiUnitDefinition.getConversionMap().keySet().toArray(items);
        Arrays.sort(items);
        comboUnit.setItems(items);
        
        comboUnit.setText(sc.getUnit().toString());
    	
    	comboUnit.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				wasEdited();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */}
		});		
 
    	
    	if(sc instanceof StaticSimulationControl)
    		inputComposite = new EditStaticSimulationControlGUI(this.getContent(), SWT.NONE, sc);
    	else if(sc instanceof ProcessSimulationControl)
    		inputComposite = null;
    	else if(sc instanceof GeometricKienzleSimulationControl){
    		inputComposite = new EditGeometricKienzleSimulationControlGUI(this.getContent(), SWT.NONE, sc);
    		comboUnit.setEnabled(false);
    	}
        else
        	inputComposite = new EditGenergicSimulationControlGUI(this.getContent(), SWT.NONE, sc);
    	
    	if(null!=inputComposite)
    		inputComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    	
    	update();
	}
    
    public static Shell editInputGUI(final Shell parent, ASimulationControl sc){
    	final Shell shell = new Shell(parent, SWT.TITLE|SWT.SYSTEM_MODAL| SWT.CLOSE | SWT.MAX | SWT.RESIZE);
        shell.setText(LocalizationHandler.getItem("app.gui.compdb.editcomp")+": "+sc.getType()+"/"+sc.getName());
        shell.setLayout(new GridLayout(1, true));
        
        
        EditInputGUI gui = new EditInputGUI(shell, SWT.NONE, sc);
		
    	shell.pack();
		
		shell.layout();
		shell.redraw();
		shell.open();
		
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
		Machine.renameInputObject(sc.getName(), textName.getText());
		sc.setUnit(new SiUnit(comboUnit.getText()));
		if(null!=inputComposite)
			inputComposite.save();
	}

	@Override
	public void reset() {
		textName.setText(sc.getName());
		comboUnit.setText(sc.getUnit().toString());
		if(null!=inputComposite)
			inputComposite.reset();
	}
}
