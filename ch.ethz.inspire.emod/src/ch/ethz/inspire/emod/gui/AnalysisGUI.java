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
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.ethz.inspire.emod.LogLevel;
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
	Composite c;
	ScrolledComposite sc;
	private CTabFolder aTabFolder;
	private CTabItem ptChartItem, varChartItem, energyChartItem;
	
	int maxWidth;
	
	/**
	 * @param dataFile
	 * @param parent
	 */
	public AnalysisGUI(String dataFile, Composite parent) {
		super(parent, dataFile);
		this.setLayout(new FillLayout());
		init();
	}

	public void init() {
		aTabFolder = new CTabFolder(this, SWT.NONE);
		aTabFolder.setBorderVisible(true);
		/*aTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void itemClosed(CTabFolderEvent event) {}
		});*/
		aTabFolder.setSelectionBackground(new Color[] {
				getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW),
		        getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
		        getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW)}, new int[] { 50,
		        100});
		aTabFolder.setUnselectedCloseVisible(false);
		aTabFolder.setSimple(false);
		
		update();
	}
	
	@Override
	public void update(){
		
		// Read data
		readData();
		
		// try to close old tabs
		if(null!=ptChartItem) ptChartItem.dispose();
		if(null!=varChartItem) varChartItem.dispose();
		if(null!=energyChartItem) energyChartItem.dispose();
		
		// Create Tabs
		ptChartItem     = new CTabItem(aTabFolder, SWT.NONE);
		varChartItem    = new CTabItem(aTabFolder, SWT.NONE);
		energyChartItem = new CTabItem(aTabFolder, SWT.NONE);
		
		ptChartItem.setShowClose(true);
		ptChartItem.setText(LocalizationHandler.getItem("app.gui.analysis.ptchart"));

		varChartItem.setShowClose(true);
		varChartItem.setText(LocalizationHandler.getItem("app.gui.analysis.variancechart"));

		energyChartItem.setShowClose(true);
		energyChartItem.setText(LocalizationHandler.getItem("app.gui.analysis.energychart"));
		
		
		ptChartItem.setControl(createPTChart(aTabFolder));
		varChartItem.setControl(StackedAreaChart.createChart(aTabFolder, getConsumerDataList()));
		energyChartItem.setControl(BarChart.createBarChart(aTabFolder, getConsumerDataList()));
		
		aTabFolder.setSelection(0);
		
		aTabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
				logger.log(LogLevel.DEBUG, "atab"+aTabFolder.getSelection().getText());
			}
		});
		
		this.redraw();
	}
	
	private Composite createPTChart(CTabFolder aTabFolder2) {
		// scrolling composite to ensure visibility 
		sc = new ScrolledComposite(aTabFolder2, SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL);
		// composite containing the elements
		c = new Composite(sc, SWT.NONE);
		sc.setContent(c);
		
		RowLayout rl = new RowLayout();
		rl.wrap = false;
		rl.pack = true;
		rl.justify = false;
		rl.pack = true;
		c.setLayout(rl);
		
		// composite containing the consumer checkbox groups
		Composite chooseComp = new Composite(c, SWT.NONE);
		RowLayout rl2 = new RowLayout();
		rl2.wrap = true;
		rl2.pack = true;
		rl2.type = SWT.VERTICAL;
		chooseComp.setLayout(rl2);
		
		maxWidth=0;
		// create the consumer groups
		mcclist = new ArrayList<MachineComponentComposite>();
		for(ConsumerData cd : availableConsumers) {
			MachineComponentComposite temp = new MachineComponentComposite(chooseComp, SWT.NONE, cd);
			
			if(temp.getSize().x>maxWidth)
				maxWidth = temp.getSize().x;
			mcclist.add(temp);
		}
		
		// composite containing the chart
		graphComp = new Composite(c, SWT.NONE);
		graphComp.setLayout(new FillLayout());
		
		for(MachineComponentComposite mcc : mcclist) {
			mcc.setLayoutData(new RowData(maxWidth, mcc.getSize().y));
		}
		
		// button to draw the graph
		Button calc = new Button(chooseComp, SWT.PUSH);
		calc.setText(LocalizationHandler.getItem("app.gui.analysis.button.show"));
		calc.setLayoutData(new RowData(maxWidth, 30));
		calc.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				for(MachineComponentComposite mcc:mcclist) {
					mcc.updateActive();
				}
				graphComp.setLayoutData(new RowData(aTabFolder.getSize().x - maxWidth - 40, aTabFolder.getSize().y -50));
				graphComp.setSize(aTabFolder.getSize().x - maxWidth - 40, aTabFolder.getSize().y -50);
				LineChart.createChart(graphComp, getConsumerDataList());
				sc.setMinSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		// set size and scrollability of composites
		c.setSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		c.layout(true, true);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		return sc;
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
