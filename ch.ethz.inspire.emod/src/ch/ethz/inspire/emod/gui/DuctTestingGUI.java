package ch.ethz.inspire.emod.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;

import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.model.Pump;
import ch.ethz.inspire.emod.model.fluid.ADuctElement;
import ch.ethz.inspire.emod.model.fluid.Duct;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.units.SiUnit;

public class DuctTestingGUI extends AGUITab{

	private Duct duct;
	private ArrayList<Pump> pumps = new ArrayList<Pump>();
    private static Table tableTesting;
    private TabFolder tabMain;
    private static Table tableOpPoint; 
    private Menu menuChartCC, menuChartTesting;
    private Chart chartTesting, chartCC;
    private ILineSeries lineSeriesPressureTesting, lineSeriesHTCTesting,
    					lineSeriesPressureCC, lineSeriesHTCCC;
    private ArrayList<ILineSeries> lineSeriesPumps = new ArrayList<ILineSeries>();
    private Color colorPressure, colorHTC;
    
    private double flowRate     = 9.3E-5,
    		       pressure     = 3e5,
    		       temperatureF = 293.15,
    		       temperatureW = 293.15;
    
    private double maxFlowRate  = 1e-4;
    
    
    public DuctTestingGUI(Composite parent, Duct duct){
    	super(parent, SWT.NONE);
		this.setLayout(new GridLayout(1, true));
		
		this.duct = duct;
		
		if(null==duct.getMaterial())
			this.duct.setMaterial(new Material("GlykolWater_30"));
		else
			this.duct.setMaterial(this.duct.getMaterial());
		
		
		init();
    }


	@Override
	public void init() {
		
		/* OP Table */
		tableOpPoint = new Table(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		tableOpPoint.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableOpPoint.setLinesVisible(true);
		tableOpPoint.setHeaderVisible(true);
		
		String[] titlesOP =  {"Parameter", "Value", "Unit", "        "};
		for(int i=0; i < titlesOP.length; i++){
			TableColumn column = new TableColumn(tableOpPoint, SWT.NULL);
			column.setText(titlesOP[i]);
			if(1==i)
				column.setAlignment(SWT.RIGHT);
		}
		
		/* Tabs */
		tabMain = new TabFolder(this, SWT.NONE);
		tabMain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		/* Testing Table */
		tableTesting = new Table(tabMain, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		tableTesting.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableTesting.setLinesVisible(true);
		tableTesting.setHeaderVisible(true);
		
		String[] titlesTest =  {"Element", "Δp ["+(new SiUnit("Pa")).toString()+"]", "Rth ["+(new SiUnit("W K^-1")).toString()+"]", "α ["+(new SiUnit("W m^-2 K^-1")).toString()+"]"};
		for(int i=0; i < titlesTest.length; i++){
			TableColumn column = new TableColumn(tableTesting, SWT.NULL);
			column.setText(titlesTest[i]);
			column.setAlignment(SWT.RIGHT);
		}
		
		/* Testing Chart */
		colorPressure = new Color(getDisplay(), new RGB(0, 0, 255));
		colorHTC      = new Color(getDisplay(), new RGB(255, 80, 0));
		
		chartTesting = new Chart(tabMain, SWT.NONE);
		chartTesting.getAxisSet().getXAxis(0).getTitle().setText("Location ["+(new SiUnit("m")).toString()+"]");
		chartTesting.getAxisSet().getXAxis(0).getTick().setForeground(Display.getDefault().getSystemColor(0));
		chartTesting.getAxisSet().getXAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(0));
		chartTesting.getAxisSet().createYAxis();
		chartTesting.getAxisSet().getYAxis(0).getTitle().setText("HTC ["+(new SiUnit("W/K")).toString()+"]");
		chartTesting.getAxisSet().getYAxis(0).getTick().setForeground(colorPressure);
		chartTesting.getAxisSet().getYAxis(0).getTitle().setForeground(colorPressure);
		chartTesting.getAxisSet().getYAxis(1).getTick().setFormat(new DecimalFormat("0.###E0"));
		chartTesting.getAxisSet().getYAxis(1).getTitle().setText("Pressure ["+(new SiUnit("Pa")).toString()+"]");
		chartTesting.getAxisSet().getYAxis(0).getTick().setForeground(colorHTC);
		chartTesting.getAxisSet().getYAxis(0).getTitle().setForeground(colorHTC);
		chartTesting.getTitle().setVisible(false);
		
		final Composite plotArea = chartTesting.getPlotArea();

	    plotArea.addListener(SWT.MouseHover, new Listener() {
			@Override
			public void handleEvent(Event event) {
				IAxis xAxis = chartTesting.getAxisSet().getXAxis(0);
				
				double x = xAxis.getDataCoordinate(event.x);
				
				double pos = 0;
				for(ADuctElement e: duct.getElements())
					if(pos+e.getLength()>x){
						plotArea.setToolTipText(e.getName());
						break;
					}
					else
						pos+=e.getLength();
			}
	    });

		lineSeriesHTCTesting = (ILineSeries) chartTesting.getSeriesSet().createSeries(SeriesType.LINE, "HTC");
		lineSeriesHTCTesting.setYAxisId(0);
		lineSeriesHTCTesting.setLineColor(colorHTC);
		lineSeriesHTCTesting.setLineWidth(2);
		lineSeriesHTCTesting.enableArea(true);
		lineSeriesHTCTesting.setSymbolType(PlotSymbolType.NONE);
		
		lineSeriesPressureTesting = (ILineSeries) chartTesting.getSeriesSet().createSeries(SeriesType.LINE, "Pressure");
		lineSeriesPressureTesting.setYAxisId(1);
		lineSeriesPressureTesting.setLineColor(colorPressure);
		lineSeriesPressureTesting.setLineWidth(2);
		lineSeriesPressureTesting.setSymbolType(PlotSymbolType.NONE);
		
		/* Chart Characteristics */
		chartCC = new Chart(tabMain, SWT.NONE);
		chartCC.getAxisSet().getXAxis(0).getTitle().setText("Flow Rate ["+(new SiUnit("m^3 s^-1")).toString()+"]");
		chartCC.getAxisSet().getXAxis(0).getTick().setForeground(Display.getDefault().getSystemColor(0));
		chartCC.getAxisSet().getXAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(0));
		chartCC.getAxisSet().getXAxis(0).getTick().setFormat(new DecimalFormat("0.###E0"));
		chartCC.getAxisSet().createYAxis();
		chartCC.getAxisSet().getYAxis(0).getTitle().setText("HTC ["+(new SiUnit("W/K")).toString()+"]");
		chartCC.getAxisSet().getYAxis(0).getTick().setForeground(colorPressure);
		chartCC.getAxisSet().getYAxis(0).getTitle().setForeground(colorPressure);
		chartCC.getAxisSet().getYAxis(1).getTitle().setText("Pressure ["+(new SiUnit("Pa")).toString()+"]");
		chartCC.getAxisSet().getYAxis(1).getTick().setFormat(new DecimalFormat("0.###E0"));
		chartCC.getAxisSet().getYAxis(0).getTick().setForeground(colorHTC);
		chartCC.getAxisSet().getYAxis(0).getTitle().setForeground(colorHTC);
		chartCC.getTitle().setVisible(false);
		
		lineSeriesHTCCC = (ILineSeries) chartCC.getSeriesSet().createSeries(SeriesType.LINE, "HTC");
		lineSeriesHTCCC.setYAxisId(0);
		lineSeriesHTCCC.setLineColor(colorHTC);
		lineSeriesHTCCC.setLineWidth(2);
		lineSeriesHTCCC.enableArea(true);
		lineSeriesHTCCC.setSymbolType(PlotSymbolType.NONE);
		
		lineSeriesPressureCC = (ILineSeries) chartCC.getSeriesSet().createSeries(SeriesType.LINE, "Pressure");
		lineSeriesPressureCC.setYAxisId(1);
		lineSeriesPressureCC.setLineColor(colorPressure);
		lineSeriesPressureCC.setLineWidth(2);
		lineSeriesPressureCC.setSymbolType(PlotSymbolType.NONE);
		
		chartCC.addMouseWheelListener(new MouseWheelListener() {
			public void mouseScrolled(MouseEvent e) {
				if(e.count>0)
					zoomOut();
				else
					zoomIn();
					
			}
		});
		
		/* Tab Controls */
		TabItem tabAnalysisNum = new TabItem(tabMain, SWT.NONE);
		tabAnalysisNum.setText("Element Analysis: Numerical");
		tabAnalysisNum.setToolTipText("");
		tabAnalysisNum.setControl(tableTesting); 
		
		TabItem tabAnalysisPlot = new TabItem(tabMain, SWT.NONE);
		tabAnalysisPlot.setText("Element Analysis: Graphical");
		tabAnalysisPlot.setToolTipText("");
		tabAnalysisPlot.setControl(chartTesting); 
		
		TabItem tabCharacteristicsPlot = new TabItem(tabMain, SWT.NONE);
		tabCharacteristicsPlot.setText("Characteristic Diagram ");
		tabCharacteristicsPlot.setToolTipText("");
		tabCharacteristicsPlot.setControl(chartCC); 
		
		/* Add editor and cp */
		try {
			TableUtils.addCellEditor(tableOpPoint, this.getClass().getDeclaredMethod("setOperationalPoint", TableCursor.class, Text.class), this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			TableUtils.addCopyToClipboard(tableTesting);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/* Popup */
		menuChartCC = new Menu(chartCC);
		MenuItem itemPrint = new MenuItem(menuChartCC, SWT.NONE);
		itemPrint.setText("Save as image");
		itemPrint.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				saveChartCC(chartCC);
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		MenuItem itemAddPump = new MenuItem(menuChartCC, SWT.NONE);
		itemAddPump.setText("Add new Pump");
		itemAddPump.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				openModelSelectGUI();
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		
		chartCC.setMenu(menuChartCC);
		chartCC.getPlotArea().setMenu(menuChartCC);
		
		menuChartTesting = new Menu(chartTesting);
		MenuItem itemPrintTesting = new MenuItem(menuChartTesting, SWT.NONE);
		itemPrintTesting.setText("Save as image");
		itemPrintTesting.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				saveChartCC(chartTesting);
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		
		chartTesting.setMenu(menuChartTesting);
		chartTesting.getPlotArea().setMenu(menuChartTesting);
		
		/* Update */
		update();
		
	}
	
	private void openModelSelectGUI(){
		SelectMachineComponentGUI compGUI= new SelectMachineComponentGUI();		        		
		try {
			compGUI.getSelectionToTable("Pump", this.getClass().getDeclaredMethod("addPump", String.class), this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void zoomIn(){
		this.maxFlowRate*=.9;
		updateChartCC();
	}
	
	private void zoomOut(){
		this.maxFlowRate*=1.1;
		updateChartCC();
	}


	@Override
	public void update() {
		this.getShell().redraw();
		this.getShell().layout();
		this.getShell().update();
		
		System.out.println(this.getParent().getSize().x+" : "+this.getParent().getSize().y);
		System.out.println(this.getSize().x+" : "+this.getSize().y);

		this.redraw();
		this.layout();
		
		System.out.println(this.getSize().x+" : "+this.getSize().y);
		
		
		updateTestingTable();
		updateOPTable();
		updateChartTesting();
		updateChartCC();

		//this.getParent().pack();
	}
	
	public void setOperationalPoint(TableCursor cursor, Text text){
		TableItem row = cursor.getRow();
        int column = cursor.getColumn();
        if(1==column){
        	row.setText(1, text.getText());
        }
	
		readOPTable();
		update();
	}
	
	public void setMaterial(String type){
		duct.setMaterial(new Material(type));
		update();
	}

	private void readOPTable(){
		for(int i=0; i<tableOpPoint.getItemCount(); i++)
			try{
				switch(i){
				case 0:
					this.flowRate = Double.parseDouble(tableOpPoint.getItem(i).getText(1));
					break;
				case 1:
					this.pressure = Double.parseDouble(tableOpPoint.getItem(i).getText(1));
					break;
				case 2:
					this.temperatureF = Double.parseDouble(tableOpPoint.getItem(i).getText(1));
					break;
				case 3:
					this.temperatureW = Double.parseDouble(tableOpPoint.getItem(i).getText(1));
					break;
				case 4:
					duct.setMaterial(new Material(tableOpPoint.getItem(i).getText(1)));
				}
			} catch(Exception e){}
			
	}

	private void updateOPTable(){
		for(TableItem it: tableOpPoint.getItems()){
			it.dispose();
		}
		
		tableOpPoint.clearAll();
		tableOpPoint.setItemCount(0);
		
		TableItem itemFlowRate, itemPressure, itemTemperatureF, itemTemperatureW, itemMaterial;
		
		itemFlowRate     = new TableItem(tableOpPoint, SWT.LEFT, 0);
		itemPressure     = new TableItem(tableOpPoint, SWT.LEFT, 1);
		itemTemperatureF = new TableItem(tableOpPoint, SWT.LEFT, 2);
		itemTemperatureW = new TableItem(tableOpPoint, SWT.LEFT, 3);
		itemMaterial     = new TableItem(tableOpPoint, SWT.LEFT, 4);
		
		itemFlowRate.setText(0, "Flow Rate");
		itemFlowRate.setText(1, this.flowRate+"");
		itemFlowRate.setText(2, (new SiUnit("m^3/s")).toString());
		
		itemPressure.setText(0, "Inlet pressure");
		itemPressure.setText(1, this.pressure+"");
		itemPressure.setText(2, (new SiUnit("Pa")).toString());
		
		itemTemperatureF.setText(0, "Temperature Fluid (avg.)");
		itemTemperatureF.setText(1, this.temperatureF+"");
		itemTemperatureF.setText(2, (new SiUnit("K")).toString());
		
		itemTemperatureW.setText(0, "Temperature Wall (avg.)");
		itemTemperatureW.setText(1, this.temperatureW+"");
		itemTemperatureW.setText(2, (new SiUnit("K")).toString());
		
		itemMaterial.setText(0, "Coolant");
		itemMaterial.setText(1, duct.getMaterial().getType());
		itemMaterial.setText(2, "");
		
		final TableEditor editorButton = new TableEditor(tableOpPoint);
    	
    	final Button buttonEditMaterial = new Button(tableOpPoint, SWT.NONE);
    	Image imageEdit = new Image(Display.getDefault(), "src/resources/Edit16.gif");
    	buttonEditMaterial.setImage(imageEdit);
    	buttonEditMaterial.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
    	buttonEditMaterial.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		openMaterialGUI();
        	}
			public void widgetDefaultSelected(SelectionEvent event){
        		
        	}
        });
    	buttonEditMaterial.pack();
		editorButton.minimumWidth = buttonEditMaterial.getSize().x;
		editorButton.horizontalAlignment = SWT.LEFT;
        editorButton.setEditor(buttonEditMaterial, itemMaterial, 3);  
        
        itemMaterial.addDisposeListener(new DisposeListener(){	
			@Override
			public void widgetDisposed(DisposeEvent e) {
				buttonEditMaterial.dispose();
				editorButton.dispose();
			}
		});
		
		TableColumn[] columns = tableOpPoint.getColumns();
        for (int j = 0; j < columns.length; j++) {
        	columns[j].pack();
        }
	}
	
	private void openMaterialGUI(){
		try {
			(new SelectMaterialGUI()).getSelectionToTable(this.getClass().getDeclaredMethod("setMaterial", String.class), this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateTestingTable(){
    	tableTesting.clearAll();
    	tableTesting.setItemCount(0);
    	
    	for(ADuctElement e: duct.getElements()){
    		int i                    = tableTesting.getItemCount();
			final TableItem itemProp = new TableItem(tableTesting, SWT.RIGHT, i);
			
			itemProp.setText(0, e.getName());
			itemProp.setText(1, String.format("%.3g", e.getPressureDrop(flowRate, pressure, temperatureF, temperatureW)));
			itemProp.setText(2, String.format("%.3g", e.getHTC(flowRate, pressure, temperatureF, temperatureW)*e.getSurface()));
			itemProp.setText(3, String.format("%.3g", e.getHTC(flowRate, pressure, temperatureF, temperatureW)));
    	}
    	
    	int i              = tableTesting.getItemCount();
		TableItem itemProp = new TableItem(tableTesting, SWT.RIGHT, i);
		
		itemProp.setText(0, "TOTAL");
		itemProp.setText(1, String.format("%.3g", duct.getPressureDrop(flowRate, pressure, temperatureF, temperatureW)));
		itemProp.setText(2, String.format("%.3g", duct.getThermalResistance(flowRate, pressure, temperatureF, temperatureW)));
		itemProp.setText(3, String.format("%.3g", duct.getHTC(flowRate, pressure, temperatureF, temperatureW)));
		itemProp.setFont(new Font(itemProp.getDisplay(), "Arial", 10, SWT.BOLD));
    	
    	TableColumn[] columns = tableTesting.getColumns();
        for (int j = 0; j < columns.length; j++) {
        	columns[j].pack();
        }
    }
	
	private void updateChartTesting(){
		
		
		double[] pressure, position1, htc, position2;
		
		pressure  = new double[duct.getElements().size()+1];
		position1 = new double[duct.getElements().size()+1];
		htc       = new double[duct.getElements().size()*2];
		position2 = new double[duct.getElements().size()*2];
		
		// Inlet:
		pressure[0]  = this.pressure;
		position1[0] = 0;
		
		for(int i=0; i<duct.getElements().size(); i++){
			position1[i+1] = position1[i] + duct.getElement(i).getLength();
			pressure[i+1] = pressure[i] -   duct.getElement(i).getPressureDrop(flowRate, pressure[i], temperatureF, temperatureW);
		}
		
		
		position2[0] = position1[0];
		position2[1] = duct.getElement(0).getLength();
		htc[0]       = duct.getElement(0).getHTC(flowRate, pressure[0], temperatureF, temperatureW)*duct.getElement(0).getSurface();
		htc[1]       = htc[0];
		
		for(int i=1; i<duct.getElements().size(); i++){
			position2[2*i]   = position2[2*i-1];
			position2[2*i+1] = position2[2*i] + duct.getElement(i).getLength();
			htc[2*i] = duct.getElement(i).getHTC(flowRate, pressure[i], temperatureF, temperatureW)*duct.getElement(i).getSurface();
			if(null!=duct.getElement(i).getIsolation())
				htc[2*i] =  1/(1/htc[2*i] + 1/duct.getElement(i).getIsolation().getThermalResistance());
			
			htc[2*i+1] = htc[2*i];
		}

		
		lineSeriesPressureTesting.setXSeries(position1);
		lineSeriesPressureTesting.setYSeries(pressure);
		
		lineSeriesHTCTesting.setXSeries(position2);
		lineSeriesHTCTesting.setYSeries(htc);
		
		chartTesting.getLegend().setVisible(false);
		chartTesting.getAxisSet().adjustRange();
		chartTesting.redraw();
	}
	
	private void updateChartCC(){
		
		
		double[] pressure, htc, flowRate, pressurePump;
		int N = 100;
		
		/* Duct */
		pressure  = new double[N];
		flowRate  = new double[N];
		htc       = new double[N];
		
		
		for(int i=0; i<N; i++){
			flowRate[i] = i*this.maxFlowRate/(N-1);
			pressure[i] = duct.getPressureDrop(flowRate[i], this.pressure, this.temperatureF, this.temperatureW);
			htc[i]      = duct.getThermalResistance(flowRate[i], this.pressure, this.temperatureF, this.temperatureW);
		}

		
		lineSeriesPressureCC.setXSeries(flowRate);
		lineSeriesPressureCC.setYSeries(pressure);
		
		lineSeriesHTCCC.setXSeries(flowRate);
		lineSeriesHTCCC.setYSeries(htc);
		
		/* Pumps */
		pressurePump  = new double[N];
		
		for(int i=0; i<this.pumps.size(); i++){
			for(int j=0; j<N; j++)
				pressurePump[j] = this.pumps.get(i).getPressure(flowRate[j]);
			
			this.lineSeriesPumps.get(i).setXSeries(flowRate);
			this.lineSeriesPumps.get(i).setYSeries(pressurePump);
		}
		
		chartCC.getAxisSet().adjustRange();
		chartCC.redraw();
	}
	
	public void addPump(String type){
		try{
			addPump(new Pump(type));
		} catch(Exception e2){}
	}
	
	private void addPump(final Pump pump){
		// Check if type is unique
		for(Pump p: this.pumps)
			if(p.getType().equals(pump.getType()))
				return;
		
		// Add Pump to array
		this.pumps.add(pump);
		
		// Create and add new line series
		ILineSeries line = (ILineSeries) chartCC.getSeriesSet().createSeries(SeriesType.LINE, "Pump: "+pump.getType());
		line.setYAxisId(1);
		line.setLineStyle(LineStyle.DOT);
		line.setLineWidth(2);
		line.setSymbolType(PlotSymbolType.NONE);
		this.lineSeriesPumps.add(line);
		
		// Add menu item to remove pump
		if(menuChartCC.getItemCount()==2)
			new MenuItem(menuChartCC, SWT.SEPARATOR);
		MenuItem item = new MenuItem(menuChartCC, SWT.NONE);
		item.setText("Remove pump '"+pump.getType()+"'");
		item.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				removePump(pump.getType());
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		
		// Adjust colors
		setPumpColors();
		
		updateChartCC();
	}
	
	private void removePump(String type){
		// Find index
		int index=-1;
		
		for(int i=0; i<this.pumps.size(); i++)
			if(this.pumps.get(i).getType().equals(type)){
				index = i;
				break;
			}
		
		if(-1==index)
			return;
		
			
		// Dispose and remove elements
		this.pumps.remove(index);
		this.chartCC.getSeriesSet().deleteSeries(this.lineSeriesPumps.get(index).getId());
		this.lineSeriesPumps.remove(index);
		menuChartCC.getItem(index+3).dispose();
		
		if(menuChartCC.getItemCount()==3)
			menuChartCC.getItem(1).dispose();
		
		// Adjust colors
		setPumpColors();
		
		updateChartCC();
			
	}
	
	private void saveChartCC(Chart chart) {
		FileDialog fileChooser = new FileDialog(getShell(), SWT.OPEN);
		fileChooser.setFilterExtensions(new String[]{"*.png", "*.jpg", "*.bmp"});
	    String fileName = fileChooser.open();
	      
		Image image = new Image(Display.getDefault(), chart.getBounds().width, chart.getBounds().height);
		ImageLoader loader = new ImageLoader();
		GC gc = new GC(image);
		chart.print(gc);

		loader.data = new ImageData[] {image.getImageData()};
		switch(fileName.substring(fileName.length()-3)){
		case "png":
			loader.save(fileName, SWT.IMAGE_PNG);
			break;
		case "jpg":
			loader.save(fileName, SWT.IMAGE_JPEG);
			break;
		case "bmp":
			loader.save(fileName, SWT.IMAGE_BMP);
		}
        
        image.dispose();
        gc.dispose(); 
	}

	private void setPumpColors(){
		int i=1;
		for(ILineSeries l: this.lineSeriesPumps)
			l.setLineColor(new Color(Display.getCurrent(), new RGB(0, 100+(int)(155*i++/this.lineSeriesPumps.size()),0)));
	}


}
