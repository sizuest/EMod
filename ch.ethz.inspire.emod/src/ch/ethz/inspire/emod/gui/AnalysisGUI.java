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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

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
	List<ExpandBar> barlist;
	Composite graphComp;
	ExpandBar chooseBar;
	Text textFilter;
	private TabFolder aTabFolder;
	private TabItem ptChartItem, varChartItem, energyChartItem;
	
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
		aTabFolder = new TabFolder(this, SWT.NONE);		
		
		//update();
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
		ptChartItem     = new TabItem(aTabFolder, SWT.NONE);
		varChartItem    = new TabItem(aTabFolder, SWT.NONE);
		energyChartItem = new TabItem(aTabFolder, SWT.NONE);
		
		ptChartItem.setText(LocalizationHandler.getItem("app.gui.analysis.ptchart"));

		varChartItem.setText(LocalizationHandler.getItem("app.gui.analysis.variancechart"));

		energyChartItem.setText(LocalizationHandler.getItem("app.gui.analysis.energychart"));
		
		
		ptChartItem.setControl(createPTChart(aTabFolder));
		varChartItem.setControl(StackedAreaChart.createChart(aTabFolder, getConsumerDataList()));
		energyChartItem.setControl(BarChart.createBarChart(aTabFolder, getConsumerDataList()));
		
		aTabFolder.setSelection(0);
		
		aTabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
				logger.log(LogLevel.DEBUG, "atab"+aTabFolder.getSelectionIndex());
			}
		});
		
		this.redraw();
	}
	
	
	private Composite createPTChart(TabFolder aTabFolder2) {
		
		
		// scrolling composite to ensure visibility 
		//sc = new ScrolledComposite(aTabFolder2, SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL);
		// composite containing the elements
		final SashForm c = new SashForm(aTabFolder2, SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		c.setBackground(getBackground());

		// Left hand composite
		Composite cl = new Composite(c, SWT.BORDER);
		cl.setLayout(new GridLayout(1, false));
		cl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		cl.setBackground(getBackground());
		
		
		
		// Text field for Filtering
		textFilter = new Text(cl, SWT.BORDER | SWT.SINGLE);
		textFilter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		textFilter.setMessage("Filter");
		textFilter.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				redrawConsumerList(textFilter.getText());
			}
			
			@Override
			public void keyPressed(KeyEvent e) {}
		});
		
		chooseBar = new ExpandBar(cl, SWT.V_SCROLL | SWT.BORDER);
		chooseBar.setLayout(new GridLayout(1, false));
		chooseBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,1,1));
		chooseBar.setBackground(getBackground());
		
		
		// Consumer List
		redrawConsumerList("");
		
		// button to draw the graph
		Button calc = new Button(cl, SWT.PUSH);
		calc.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		calc.setText(LocalizationHandler.getItem("app.gui.analysis.button.show"));
		calc.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				redrawGraph();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		
		cl.pack();
		
		
		// composite containing the chart
		graphComp = new Composite(c, SWT.NONE);
		graphComp.setLayout(new FillLayout());
		
		
		redrawGraph();
		redrawConsumerList("");
		
		c.pack();
		c.redraw();
		
		return c;
	}
	
	public void redrawGraph(){
		graphComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		LineChart.createChart(graphComp, getConsumerDataList());
		
		graphComp.layout();
		graphComp.redraw();
		
		graphComp.getParent().layout();
		graphComp.getParent().redraw();
	}
	
	public void redrawConsumerList(String filter){
		maxWidth=0;
		
		for(ExpandItem ei: chooseBar.getItems()) 
			ei.dispose();
			
		for(Control c: chooseBar.getChildren())
			c.dispose();
		
		
		// create the consumer groups
		mcclist = new ArrayList<MachineComponentComposite>();
		
		
		for(ConsumerData cd : availableConsumers) {
			final ExpandItem item = new ExpandItem(chooseBar, SWT.NONE);
			item.setText(cd.getConsumer());
			//item.setExpanded(true);
			
			final MachineComponentComposite temp = new MachineComponentComposite(chooseBar, SWT.NONE, cd, filter);
			item.setControl(temp);
			item.setHeight(temp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			item.setExpanded(true);
						
			if(temp.getSize().x>maxWidth)
				maxWidth = temp.getSize().x;
			mcclist.add(temp);
		}
		
		for(MachineComponentComposite mcc : mcclist) {
			mcc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
		
		//bar.setSpacing(8);
		chooseBar.layout();
		chooseBar.redraw();
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
		ArrayList<Integer> filtOutputs;
		String filter;
		/**
		 * @param parent
		 * @param style
		 */
		public MachineComponentComposite(Composite parent, int style, ConsumerData data, String filter) {
			super(parent, SWT.BORDER);
			this.comp = data;
			this.filter = filter;
			outputs = new ArrayList<Button>();
			filtOutputs = new ArrayList<Integer>();
			init();			
		}
		
		private void init() {
			setLayout(new GridLayout(2,false));

			complete = new Button(this,SWT.CHECK);
			complete.setSelection(true);
			for(int i=0;i<comp.getNames().size();i++)
				if(!(comp.getActive().get(i)))
					complete.setSelection(false);
			
			complete.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					for(int i=0;i<comp.getNames().size();i++)
						comp.getActive().set(i, complete.getSelection());
					
					redrawConsumerList(textFilter.getText());
					redrawGraph();
					
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
			});
			Label componentLabel = new Label(this, SWT.NONE);
			componentLabel.setText("All");
			componentLabel.setFont(new Font(Display.getCurrent(), "Arial", 10, SWT.ITALIC));
			for(int i=0;i<comp.getNames().size();i++) {
				if(  "" == filter | 
				     comp.getNames().get(i).toLowerCase().contains(filter.toLowerCase()) |
				     ("["+comp.getUnits().get(i).toString()+"]").contains(filter)) {
					final Button b = new Button(this,SWT.CHECK);
					final int idx = i;
					b.setSelection(comp.getActive().get(i));
					b.addSelectionListener(new SelectionListener() {
						
						@Override
						public void widgetSelected(SelectionEvent e) {
							comp.getActive().set(idx, b.getSelection());
							redrawGraph();
						}
						
						@Override
						public void widgetDefaultSelected(SelectionEvent e) {}
					});
					
					filtOutputs.add(i);
					Label l = new Label(this, SWT.NONE);
					l.setText(comp.getNames().get(i)+" ["+comp.getUnits().get(i)+"]");
				}
			}
			setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
			layout(true);
			this.pack();
		}		
	}
}
