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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.gui.utils.ConsumerData;
import ch.ethz.inspire.emod.gui.utils.LineChart;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.simulation.MachineState;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * @author sizuest
 * 
 */
public class FEMExportGUI extends AEvaluationGUI {

	private static Logger logger = Logger.getLogger(FEMExportGUI.class.getName());

	Composite graphComp;
	Tree chooseTree;
	ArrayList<TreeItem> consumerTreeItems = new ArrayList<TreeItem>();
	Text textFilter;
	private TabFolder aTabFolder;
	private TabItem tChartItem, sChartItem;

	int maxWidth;

	/**
	 * @param dataFile
	 * @param parent
	 */
	public FEMExportGUI(String dataFile, Composite parent) {
		super(parent, dataFile);
		this.setLayout(new FillLayout());
		init();
	}

	@Override
	public void init() {
		aTabFolder = new TabFolder(this, SWT.NONE);

		postDataImportAction();
	}

	@Override
	public void update() {

		aTabFolder.setEnabled(false);

		// Read data
		readData();
	}

	private Composite createPTChart(TabFolder aTabFolder2) {

		// scrolling composite to ensure visibility
		// sc = new ScrolledComposite(aTabFolder2, SWT.NONE | SWT.V_SCROLL |
		// SWT.H_SCROLL);
		// composite containing the elements
		final SashForm c = new SashForm(aTabFolder2, SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		c.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		c.setBackground(getBackground());

		// Left hand composite
		Composite cl = new Composite(c, SWT.BORDER);
		cl.setLayout(new GridLayout(2, false));
		cl.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		cl.setBackground(getBackground());

		// Text field for Filtering
		textFilter = new Text(cl, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		textFilter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		textFilter.setMessage("Filter");
		textFilter.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				redrawComponentList(textFilter.getText());
			}

			@Override
			public void mouseDown(MouseEvent e) {
				// Not used
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// Not used
			}
		});
		textFilter.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				redrawComponentList(textFilter.getText());
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// Not used
			}
		});

		chooseTree = new Tree(cl, SWT.V_SCROLL);
		chooseTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		chooseTree.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				for (TreeItem ti : chooseTree.getSelection()) {
					String consumer;
					String signal = ti.getText();
					if (null == ti.getParentItem())
						consumer = signal;
					else
						consumer = ti.getParentItem().getText();

					toggleConsumerData(consumer, signal);
				}

				redrawComponentList(textFilter.getText());
				redrawGraph();
			}
		});

		consumerTreeItems = new ArrayList<TreeItem>();
		for (ConsumerData cd : availableConsumers) {
			TreeItem item = new TreeItem(chooseTree, SWT.NONE);
			item.setText(cd.getConsumer());
			consumerTreeItems.add(item);
		}

		// Consumer List
		redrawComponentList("");

		// button to clear the graph
		Button clear = new Button(cl, SWT.PUSH);
		clear.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		clear.setText(LocalizationHandler
				.getItem("app.gui.analysis.button.clear"));
		clear.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				for (ConsumerData cd : availableConsumers)
					for (int i = 0; i < cd.getActive().size(); i++)
						cd.getActive().set(i, false);

				redrawGraph();
				redrawComponentList(textFilter.getText());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Not used
			}
		});

		// button to draw the graph
		Button calc = new Button(cl, SWT.PUSH);
		calc.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false, 1, 1));
		calc.setText(LocalizationHandler
				.getItem("app.gui.analysis.button.show"));
		calc.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				redrawGraph();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Not used
			}
		});

		cl.pack();

		// composite containing the chart
		graphComp = new Composite(c, SWT.NONE);
		graphComp.setLayout(new FillLayout());

		redrawGraph();
		redrawComponentList("");

		c.setWeights(new int[] { 1, 3 });

		c.pack();
		c.redraw();

		return c;
	}

	/**
	 * redraw the plot
	 */
	public void redrawGraph() {
		graphComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		LineChart.createChart(graphComp, getConsumerDataList());
	}

	/**
	 * Redraw the list of components
	 * @param filter
	 */
	public void redrawComponentList(String filter) {
		maxWidth = 0;

		for (ConsumerData cd : availableConsumers) {
			TreeItem itemTop = consumerTreeItems.get(availableConsumers.indexOf(cd));
			boolean wasExpanded = itemTop.getExpanded();

			for (TreeItem ti : itemTop.getItems())
				ti.dispose();

			consumerTreeItems.get(availableConsumers.indexOf(cd));

			for (int i = 0; i < cd.getNames().size(); i++) {
				if ("" == filter | cd.getNames().get(i).toLowerCase().contains(filter.toLowerCase()) | ("[" + cd.getUnits().get(i).toString() + "]").contains(filter)) {
					final TreeItem item = new TreeItem(itemTop, SWT.NONE);
					item.setText(cd.getNames().get(i) + " ["+ cd.getUnits().get(i) + "]");

					if (cd.getActive().get(i))
						item.setFont(new Font(Display.getCurrent(), item
								.getFont().getFontData()[0].getName(), item
								.getFont().getFontData()[0].getHeight(),
								SWT.BOLD));
				}
			}

			itemTop.setExpanded(wasExpanded);

		}

	}

	private void toggleConsumerData(String consumerName, String signalName) {
		ConsumerData consumer = null;

		for (ConsumerData cd : availableConsumers)
			if (cd.getConsumer().equals(consumerName))
				consumer = cd;

		if (null == consumer)
			return;

		if (consumerName.equals(signalName)) {
			boolean active = true;
			if (consumer.getActive().get(0))
				active = false;

			for (int i = 0; i < consumer.getActive().size(); i++)
				consumer.getActive().set(i, active);

			return;
		}

		int idx = 0;

		while (idx < consumer.getNames().size()
				& !signalName.equals(consumer.getNames().get(idx) + " ["
						+ consumer.getUnits().get(idx) + "]"))
			idx++;

		if (idx >= consumer.getNames().size())
			return;

		consumer.getActive().set(idx, !consumer.getActive().get(idx));

	}

	@Override
	protected void postDataImportAction() {
		// try to close old tabs
		if (null != tChartItem)
			tChartItem.dispose();
		if (null != sChartItem)
			sChartItem.dispose();

		// Create Tabs
		tChartItem = new TabItem(aTabFolder, SWT.NONE);
		sChartItem = new TabItem(aTabFolder, SWT.NONE);

		tChartItem.setText(LocalizationHandler.getItem("Time"));

		sChartItem.setText(LocalizationHandler.getItem("State"));

		tChartItem.setControl(createPTChart(aTabFolder));
		
		sChartItem.setControl(createStateTable(aTabFolder));

		aTabFolder.setSelection(0);

		aTabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(
					org.eclipse.swt.events.SelectionEvent event) {
				logger.log(LogLevel.DEBUG,
						"atab" + aTabFolder.getSelectionIndex());
			}
		});

		this.redraw();

		aTabFolder.setEnabled(true);
	}

	/**
	 * Creates a table including the state specific averages of all FEM values
	 * @param aTabFolder2
	 * @return
	 */
	private Composite createStateTable(TabFolder aTabFolder) {
		Table table = new Table(aTabFolder, SWT.None | SWT.MULTI);
		
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		// Table  headers
		(new TableColumn(table, SWT.NULL)).setText("Component");
		(new TableColumn(table, SWT.NULL)).setText("Name");
		(new TableColumn(table, SWT.NULL)).setText("Unit");
		for (int i = 0; i < MachineState.values().length; i++) {
			TableColumn column = new TableColumn(table, SWT.NULL);
			column.setText(MachineState.values()[i].toString());
		}
		(new TableColumn(table, SWT.NULL)).setText("Overall");
		
		try {
			TableUtils.addCopyToClipboard(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/* Fill the table */ 		
		for(ConsumerData cd: availableConsumers){
			cd.calculateStateAverage();
			// Loop through all boundary conditions of the component
			for(int bcIdx=0; bcIdx<cd.getNames().size(); bcIdx++){
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(0, cd.getConsumer());
				item.setText(1, cd.getNames().get(bcIdx));
				item.setText(2, cd.getUnits().get(bcIdx)+"");
				// Loop through all state specific values
				for (int i = 0; i < MachineState.values().length; i++) {
					if(!Double.isNaN(cd.getStateMap().get(MachineState.values()[i])[bcIdx]))
						item.setText(3+i, (new BigDecimal(cd.getStateMap().get(MachineState.values()[i])[bcIdx])).round(new MathContext(3)).toEngineeringString());
				}
				item.setText(3+MachineState.values().length, (new BigDecimal(cd.getAverage().get(bcIdx))).round(new MathContext(3)).toEngineeringString());
			}
		}
		
		/* Tabelle packen */
		TableColumn[] columns = table.getColumns();
		for (int i = 0; i < columns.length; i++) {
			columns[i].pack();
		}
		
		/* Spaltenbreite gleich ab index 3 */
		int maxWith = 0;
		for (int i = 3; i < columns.length; i++) {
			maxWith = Math.max(columns[i].getWidth(), maxWith);
		}
		for (int i = 3; i < columns.length; i++) {
			columns[i].setWidth(maxWith);
		}
		
		return table;
	}
}
