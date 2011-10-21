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
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.gui.MachineComponentSelectGUI.MachineComponentComposite;
import ch.ethz.inspire.emod.gui.utils.BarChart;
import ch.ethz.inspire.emod.gui.utils.ConsumerData;
import ch.ethz.inspire.emod.gui.utils.LineChart;
import ch.ethz.inspire.emod.gui.utils.StackedAreaChart;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * @author dhampl
 *
 */
public class AnalysisGUI extends AEvaluationGUI {

	private static Logger logger = Logger.getLogger(AnalysisGUI.class.getName());
	
	List<MachineComponentComposite> mcclist;
	Composite graphComp;
	final TabFolder aTabFolder = new TabFolder(this, SWT.NONE);
	int maxWidth;
	/**
	 * @param dataFile
	 */
	public AnalysisGUI(String dataFile, Composite parent) {
		super(parent, dataFile);
		this.setLayout(new FillLayout());
		init();
	}

	public void init() {
		
		
		TabItem ptChartItem = new TabItem(aTabFolder, SWT.NONE);
		ptChartItem.setText(LocalizationHandler.getItem("app.gui.analysis.ptchart"));
		ptChartItem.setControl(createPTChart(aTabFolder));
		
		TabItem varChartItem = new TabItem(aTabFolder, SWT.NONE);
		varChartItem.setText(LocalizationHandler.getItem("app.gui.analysis.variancechart"));
		varChartItem.setControl(StackedAreaChart.createChart(aTabFolder, getConsumerDataList()));
		
		TabItem energyChartItem = new TabItem(aTabFolder, SWT.NONE);
		energyChartItem.setText(LocalizationHandler.getItem("app.gui.analysis.energychart"));
		energyChartItem.setControl(BarChart.createBarChart(aTabFolder, getConsumerDataList()));
		
		aTabFolder.setSelection(0);
		
		aTabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
				logger.log(LogLevel.DEBUG, "atab"+aTabFolder.getSelection()[0].getText());
			}
		});
	}
	
	private Composite createPTChart(TabFolder tabFolder) {
		Composite c = new Composite(tabFolder, SWT.NONE);
		//c.setLayout(new FillLayout());
		RowLayout rl = new RowLayout();
		rl.wrap = true;
		rl.pack = true;
		rl.justify = false;
		c.setLayout(rl);
		
		/*c.setLayout(new GridLayout(2,false));
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.LEFT;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = SWT.TOP;
		gd.grabExcessVerticalSpace = true;
		c.setLayoutData(gd);*/
		Composite chooseComp = new Composite(c, SWT.NONE);
		//chooseComp.setLayout(new GridLayout(1, true));
		//chooseComp.setLayout(new FillLayout(SWT.VERTICAL));
		RowLayout rl2 = new RowLayout();
		rl2.wrap = true;
		rl2.pack = true;
		rl2.type = SWT.VERTICAL;
		chooseComp.setLayout(rl2);
		
		maxWidth=0;
		mcclist = new ArrayList<MachineComponentComposite>();
		for(ConsumerData cd : availableConsumers) {
			MachineComponentComposite temp = new MachineComponentComposite(chooseComp, SWT.NONE, cd);
			
			if(temp.getSize().x>maxWidth)
				maxWidth = temp.getSize().x;
			mcclist.add(temp);
		}
		
		graphComp = new Composite(c, SWT.NONE);
		graphComp.setLayout(new FillLayout());
		//graphComp.setLayoutData(new RowData(800, 500));
		
		for(MachineComponentComposite mcc : mcclist) {
			mcc.setLayoutData(new RowData(maxWidth, mcc.getSize().y));
		}
		
		Button calc = new Button(chooseComp, SWT.PUSH);
		calc.setText("show");
		calc.setLayoutData(new RowData(maxWidth, 30));
		calc.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				for(MachineComponentComposite mcc:mcclist) {
					mcc.updateActive();
				}
				graphComp.setLayoutData(new RowData(aTabFolder.getSize().x - maxWidth - 20, aTabFolder.getSize().y -20));
				graphComp.setSize(aTabFolder.getSize().x - maxWidth - 20, aTabFolder.getSize().y -20);
				LineChart.createChart(graphComp, getConsumerDataList());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		c.setSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		c.layout(true, true);
		//c.pack();
		return c;
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
		 */
		public MachineComponentComposite(Composite parent, int style, ConsumerData data) {
			super(parent, SWT.BORDER);
			this.comp = data;
			outputs = new ArrayList<Button>();
			init();
		}
		
		private void init() {
			setLayout(new GridLayout(2,false));

			complete = new Button(this,SWT.CHECK);
			complete.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					for(Button b: outputs)
						b.setSelection(complete.getSelection());
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					
				}
			});
			Label componentLabel = new Label(this, SWT.NONE);
			componentLabel.setText(comp.getConsumer());
			for(int i=0;i<comp.getNames().size();i++) {
				Button b = new Button(this,SWT.CHECK);
				outputs.add(b);
				Label l = new Label(this, SWT.NONE);
				l.setText(comp.getNames().get(i)+" ["+comp.getUnits().get(i)+"]");
			}
			setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
			layout(true);
			this.pack();
		}
		
		public void updateActive() {
			for(int i=0;i<outputs.size();i++) {
				comp.setActive(i, outputs.get(i).getSelection());
			}
		}
		
	}
}
