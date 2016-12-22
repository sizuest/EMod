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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.ethz.inspire.emod.gui.utils.BarChart;
import ch.ethz.inspire.emod.gui.utils.ConsumerData;
import ch.ethz.inspire.emod.gui.utils.LineChart;
import ch.ethz.inspire.emod.gui.utils.StackedAreaChart;

/**
 * composite to select consumer i/o elements to be drawn in a {@link LineChart}
 * 
 * @author dhampl
 * 
 */
public class MachineComponentSelectGUI extends AEvaluationGUI {

	private Composite parent;
	List<MachineComponentComposite> mcclist;

	/**
	 * @param parent
	 */
	public MachineComponentSelectGUI(Composite parent) {
		super(parent, "simulation_output.dat");
		this.parent = parent;
		init();
	}

	@Override
	public void init() {
		// Not used
	}

	/**
	 * inner class with the actual composites
	 * 
	 * @author dhampl
	 * 
	 */
	class MachineComponentComposite extends Composite {

		ConsumerData comp;
		Button complete;
		List<Button> outputs;

		/**
		 * @param parent
		 * @param style
		 * @param data
		 */
		public MachineComponentComposite(Composite parent, int style,
				ConsumerData data) {
			super(parent, SWT.BORDER);
			this.comp = data;
			outputs = new ArrayList<Button>();
			init();
		}

		private void init() {
			setLayout(new GridLayout(2, false));
			GridData gd = new GridData();
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			gd.grabExcessVerticalSpace = true;
			setLayoutData(gd);
			complete = new Button(this, SWT.CHECK);
			complete.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					for (Button b : outputs)
						b.setSelection(complete.getSelection());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// Not used
				}
			});
			Label componentLabel = new Label(this, SWT.NONE);
			componentLabel.setText(comp.getConsumer());
			for (int i = 0; i < comp.getNames().size(); i++) {
				Button b = new Button(this, SWT.CHECK);
				outputs.add(b);
				Label l = new Label(this, SWT.NONE);
				l.setText(comp.getNames().get(i) + " ["
						+ comp.getUnits().get(i) + "]");
			}
			setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
			layout(true);
			// this.pack();
		}

		public void updateActive() {
			for (int i = 0; i < outputs.size(); i++) {
				comp.setActive(i, outputs.get(i).getSelection());
			}
		}

	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void postDataImportAction() {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout());
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		c.setLayoutData(gd);
		int maxWidth = 0;
		mcclist = new ArrayList<MachineComponentComposite>();
		for (ConsumerData cd : availableConsumers) {
			MachineComponentComposite temp = new MachineComponentComposite(c,
					SWT.NONE, cd);

			if (temp.getSize().x > maxWidth)
				maxWidth = temp.getSize().x;
			mcclist.add(temp);
		}
		int noCols = parent.getSize().x / maxWidth;
		c.setLayout(new GridLayout(noCols, true));

		// c.pack();
		Button calc = new Button(c, SWT.PUSH);
		calc.setText("go");
		calc.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				for (MachineComponentComposite mcc : mcclist) {
					mcc.updateActive();
				}
				LineChart.createChart(parent, getConsumerDataList());
				BarChart.createBarChart(parent, getConsumerDataList());
				StackedAreaChart.createChart(parent, getConsumerDataList());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Not used
			}
		});
		c.setSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		c.layout(true, true);
	}
}
