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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.ethz.inspire.emod.model.IOContainer;
import ch.ethz.inspire.emod.model.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;

/**
 * @author dhampl
 *
 */
public class MachineComponentSelectGUI {

	private Composite parent;
	private List<MachineComponent> mclist;
	
	public MachineComponentSelectGUI(Composite parent) {
		this.parent = parent;
		mclist = Machine.getInstance().getMachineComponentList();
		init();
	}
	
	private void init() {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout());
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		c.setLayoutData(gd);
		int maxWidth=0;
		List<MachineComponentComposite> mcclist = new ArrayList<MachineComponentComposite>();
		for(MachineComponent mc : mclist) {
			MachineComponentComposite temp = new MachineComponentComposite(c, SWT.NONE, mc);
			
			if(temp.getSize().x>maxWidth)
				maxWidth = temp.getSize().x;
			mcclist.add(temp);
		}
		int noCols = parent.getSize().x / maxWidth;
		c.setLayout(new GridLayout(noCols, true));
		
		
		//c.pack();
		Button calc = new Button(c, SWT.PUSH);
		calc.setText("go");
		calc.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		c.setSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		c.layout(true, true);
	}
	
	
	class MachineComponentComposite extends Composite {

		MachineComponent comp;
		Button complete;
		List<Button> outputs;
		/**
		 * @param parent
		 * @param style
		 */
		public MachineComponentComposite(Composite parent, int style, MachineComponent machineComponent) {
			super(parent, SWT.BORDER);
			// TODO Auto-generated constructor stub
			this.comp = machineComponent;
			outputs = new ArrayList<Button>();
			init();
		}
		
		private void init() {
			setLayout(new GridLayout(2,false));
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.grabExcessVerticalSpace = true;
			setLayoutData(gd);
			complete = new Button(this,SWT.CHECK);
			Label componentLabel = new Label(this, SWT.NONE);
			componentLabel.setText(comp.getName());
			for(IOContainer ioc : comp.getComponent().getOutputs()) {
				Button b = new Button(this,SWT.CHECK);
				outputs.add(b);
				Label l = new Label(this, SWT.NONE);
				l.setText(ioc.getName()+" ["+ioc.getUnit()+"]");
			}
			setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
			layout(true);
			//this.pack();
		}
		
	}
}
