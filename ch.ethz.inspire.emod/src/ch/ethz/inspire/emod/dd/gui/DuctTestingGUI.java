package ch.ethz.inspire.emod.dd.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;

import ch.ethz.inspire.emod.dd.Duct;
import ch.ethz.inspire.emod.dd.model.ADuctElement;
import ch.ethz.inspire.emod.dd.model.DuctBypass;
import ch.ethz.inspire.emod.dd.model.DuctFitting;
import ch.ethz.inspire.emod.gui.AGUITab;
import ch.ethz.inspire.emod.gui.SelectMachineComponentGUI;
import ch.ethz.inspire.emod.gui.SelectMaterialGUI;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.model.Pump;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

public class DuctTestingGUI extends AGUITab{

	private SashForm form;
	private Duct duct;
	private ArrayList<Pump> pumps = new ArrayList<Pump>();
    private static Table tableTesting, tableBC;
    private TabFolder tabAnalysis, tabConfig;
    private static Table tableOpPoint; 
    private Menu menuChartCC, menuChartTesting;
    private Chart chartTesting, chartCC;
    private ILineSeries lineSeriesPressureTesting, lineSeriesHTCTesting, lineSeriesTemperatureFluid,
    					lineSeriesTemperatureWall, lineSeriesPressureCC, lineSeriesHTCCC;
    private ArrayList<ILineSeries> lineSeriesPumps = new ArrayList<ILineSeries>();
    private Color colorPressure, colorHTC, colorTemperature;
    private Material material;
    
    private double flowRate     = 9.3E-5,
    		       pressure     = 3e5,
    		       temperatureF = 293.15;
    
    private double maxFlowRate  = 1e-4;
    
    
    public DuctTestingGUI(Composite parent, Duct duct){
    	super(parent, SWT.NONE);
		this.setLayout(new GridLayout(1, true));
		
		this.duct = duct;
		
		if(null==duct.getMaterial())
			this.material = new Material("GlykolWater_30");
		else
			this.material = this.duct.getMaterial();
		
		this.duct.setMaterial(this.material);
		
		
		init();
    }


	@Override
	public void init() {
		
		form = new SashForm(this, SWT.FILL | SWT.VERTICAL);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		form.setLayout(new GridLayout(1, true));
		
		/* Tabs Analysis*/
		tabConfig = new TabFolder(form, SWT.NONE);
		tabConfig.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
		
		/* OP Table */
		tableOpPoint = new Table(tabConfig, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		tableOpPoint.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableOpPoint.setLinesVisible(true);
		tableOpPoint.setHeaderVisible(true);
		
		String[] titlesOP =  {	LocalizationHandler.getItem("app.gui.parameter"), 
								LocalizationHandler.getItem("app.gui.parameter.value"), 
								LocalizationHandler.getItem("app.gui.parameter.unit"), 
								"        "};
		for(int i=0; i < titlesOP.length; i++){
			TableColumn column = new TableColumn(tableOpPoint, SWT.NULL);
			column.setText(titlesOP[i]);
			if(1==i)
				column.setAlignment(SWT.RIGHT);
		}
		
		/* BC Table */
		tableBC = new Table(tabConfig, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		tableBC.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableBC.setLinesVisible(true);
		tableBC.setHeaderVisible(true);
		
		String[] titlesBC =  {	LocalizationHandler.getItem("app.dd.config.gui.element"), 
								LocalizationHandler.getItem("app.dd.testing.gui.bc.temperature")+" ["+(new SiUnit("K")).toString()+"]", 
								LocalizationHandler.getItem("app.dd.testing.gui.bc.heatFlux")+" ["+(new SiUnit("W")).toString()+"]", 
								"        "};
		for(int i=0; i < titlesBC.length; i++){
			TableColumn column = new TableColumn(tableBC, SWT.NULL);
			column.setText(titlesBC[i]);
			if(1==i)
				column.setAlignment(SWT.RIGHT);
		}
		
		/* Tabs Analysis*/
		tabAnalysis = new TabFolder(form, SWT.NONE);
		tabAnalysis.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
		
		/* Testing Table */
		tableTesting = new Table(tabAnalysis, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		tableTesting.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableTesting.setLinesVisible(true);
		tableTesting.setHeaderVisible(true);
		
		String[] titlesTest =  {LocalizationHandler.getItem("app.dd.config.gui.element"), 
				                "V ["+SiUnit.pow(new SiUnit("m"),3).toString()+"]", 
				                "S ["+(new SiUnit("m^2")).toString()+"]", 
				                "l ["+(new SiUnit("m")).toString()+"]", 
				                "Δp ["+(new SiUnit("Pa")).toString()+"]", 
				                "ζ ["+SiUnit.divide(new SiUnit("Pa"), SiUnit.pow(new SiUnit(Unit.METERCUBIC_S), 2)).toString()+"]", 
				                "Rth ["+(new SiUnit("W K^-1")).toString()+"]", 
				                "α ["+(new SiUnit("W m^-2 K^-1")).toString()+"]", 
				                "Q [l/min]",
				                "T F [K]",
				                "T W [K]",
				                ""};
		for(int i=0; i < titlesTest.length; i++){
			TableColumn column = new TableColumn(tableTesting, SWT.NULL);
			column.setText(titlesTest[i]);
			column.setAlignment(SWT.RIGHT);
		}
		
		/* Testing Chart */
		colorPressure = new Color(getDisplay(), new RGB(0, 0, 255));
		colorHTC      = new Color(getDisplay(), new RGB(255, 80, 0));
		colorTemperature = new Color(getDisplay(), new RGB(0, 255, 0));
		
		chartTesting = new Chart(tabAnalysis, SWT.NONE);
		chartTesting.getAxisSet().getXAxis(0).getTitle().setText(LocalizationHandler.getItem("app.dd.testing.gui.location")+" ["+(new SiUnit("m")).toString()+"]");
		chartTesting.getAxisSet().getXAxis(0).getTick().setForeground(Display.getDefault().getSystemColor(0));
		chartTesting.getAxisSet().getXAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(0));
		chartTesting.getAxisSet().createYAxis();
		chartTesting.getAxisSet().createYAxis();
		chartTesting.getAxisSet().getYAxis(0).getTitle().setText(LocalizationHandler.getItem("app.dd.testing.gui.htc")+" ["+(new SiUnit("W/K")).toString()+"]");
		chartTesting.getAxisSet().getYAxis(0).getTick().setForeground(colorPressure);
		chartTesting.getAxisSet().getYAxis(0).getTitle().setForeground(colorPressure);
		chartTesting.getAxisSet().getYAxis(1).getTick().setFormat(new DecimalFormat("0.###E0"));
		chartTesting.getAxisSet().getYAxis(1).getTitle().setText(LocalizationHandler.getItem("app.dd.testing.gui.pressure")+" ["+(new SiUnit("Pa")).toString()+"]");
		chartTesting.getAxisSet().getYAxis(1).getTick().setForeground(colorHTC);
		chartTesting.getAxisSet().getYAxis(1).getTitle().setForeground(colorHTC);
		chartTesting.getAxisSet().getYAxis(2).getTitle().setText(LocalizationHandler.getItem("app.dd.testing.gui.temperature")+" ["+(new SiUnit("K")).toString()+"]");
		chartTesting.getAxisSet().getYAxis(2).getTick().setForeground(colorTemperature);
		chartTesting.getAxisSet().getYAxis(2).getTitle().setForeground(colorTemperature);
		chartTesting.getTitle().setVisible(false);
		
		final Composite plotArea = chartTesting.getPlotArea();

	    plotArea.addListener(SWT.MouseHover, new Listener() {
			@Override
			public void handleEvent(Event event) {
				IAxis xAxis = chartTesting.getAxisSet().getXAxis(0);
				
				double x = xAxis.getDataCoordinate(event.x);
				
				plotArea.setToolTipText(getElementName(duct, x));
			}
	    });

		lineSeriesHTCTesting = (ILineSeries) chartTesting.getSeriesSet().createSeries(SeriesType.LINE, LocalizationHandler.getItem("app.dd.testing.gui.htc"));
		lineSeriesHTCTesting.setYAxisId(0);
		lineSeriesHTCTesting.setLineColor(colorHTC);
		lineSeriesHTCTesting.setLineWidth(2);
		lineSeriesHTCTesting.enableArea(true);
		lineSeriesHTCTesting.setSymbolType(PlotSymbolType.NONE);
		
		lineSeriesPressureTesting = (ILineSeries) chartTesting.getSeriesSet().createSeries(SeriesType.LINE, LocalizationHandler.getItem("app.dd.testing.gui.pressure"));
		lineSeriesPressureTesting.setYAxisId(1);
		lineSeriesPressureTesting.setLineColor(colorPressure);
		lineSeriesPressureTesting.setLineWidth(2);
		lineSeriesPressureTesting.setSymbolType(PlotSymbolType.NONE);
		
		lineSeriesTemperatureFluid = (ILineSeries) chartTesting.getSeriesSet().createSeries(SeriesType.LINE, LocalizationHandler.getItem("app.dd.testing.gui.temperature.fluid"));
		lineSeriesTemperatureFluid.setYAxisId(2);
		lineSeriesTemperatureFluid.setLineColor(colorTemperature);
		lineSeriesTemperatureFluid.setLineWidth(2);
		lineSeriesTemperatureFluid.setSymbolType(PlotSymbolType.NONE);
		
		lineSeriesTemperatureWall = (ILineSeries) chartTesting.getSeriesSet().createSeries(SeriesType.LINE, LocalizationHandler.getItem("app.dd.testing.gui.temperature.bulk"));
		lineSeriesTemperatureWall.setYAxisId(2);
		lineSeriesTemperatureWall.setLineColor(colorTemperature);
		lineSeriesTemperatureWall.setLineWidth(2);
		lineSeriesTemperatureWall.setSymbolType(PlotSymbolType.NONE);
		lineSeriesTemperatureWall.setLineStyle(LineStyle.DASH);
		
		/* Chart Characteristics */
		chartCC = new Chart(tabAnalysis, SWT.NONE);
		chartCC.getAxisSet().getXAxis(0).getTitle().setText(LocalizationHandler.getItem("app.dd.testing.gui.flowrate")+" ["+(new SiUnit("m^3 s^-1")).toString()+"]");
		chartCC.getAxisSet().getXAxis(0).getTick().setForeground(Display.getDefault().getSystemColor(0));
		chartCC.getAxisSet().getXAxis(0).getTitle().setForeground(Display.getDefault().getSystemColor(0));
		chartCC.getAxisSet().getXAxis(0).getTick().setFormat(new DecimalFormat("0.###E0"));
		chartCC.getAxisSet().createYAxis();
		chartCC.getAxisSet().getYAxis(0).getTitle().setText(LocalizationHandler.getItem("app.dd.testing.gui.htc")+" ["+(new SiUnit("W/K")).toString()+"]");
		chartCC.getAxisSet().getYAxis(0).getTick().setForeground(colorPressure);
		chartCC.getAxisSet().getYAxis(0).getTitle().setForeground(colorPressure);
		chartCC.getAxisSet().getYAxis(1).getTitle().setText(LocalizationHandler.getItem("app.dd.testing.gui.pressure")+" ["+(new SiUnit("Pa")).toString()+"]");
		chartCC.getAxisSet().getYAxis(1).getTick().setFormat(new DecimalFormat("0.###E0"));
		chartCC.getAxisSet().getYAxis(1).getTick().setForeground(colorHTC);
		chartCC.getAxisSet().getYAxis(1).getTitle().setForeground(colorHTC);
		chartCC.getTitle().setVisible(false);
		
		lineSeriesHTCCC = (ILineSeries) chartCC.getSeriesSet().createSeries(SeriesType.LINE, LocalizationHandler.getItem("app.dd.testing.gui.htc"));
		lineSeriesHTCCC.setYAxisId(0);
		lineSeriesHTCCC.setLineColor(colorHTC);
		lineSeriesHTCCC.setLineWidth(2);
		lineSeriesHTCCC.enableArea(true);
		lineSeriesHTCCC.setSymbolType(PlotSymbolType.NONE);
		
		lineSeriesPressureCC = (ILineSeries) chartCC.getSeriesSet().createSeries(SeriesType.LINE, LocalizationHandler.getItem("app.dd.testing.gui.pressure"));
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
		TabItem tabConfigFlow = new TabItem(tabConfig, SWT.NONE);
		tabConfigFlow.setText(LocalizationHandler.getItem("app.dd.testing.gui.fluidproperties"));
		tabConfigFlow.setToolTipText("");
		tabConfigFlow.setControl(tableOpPoint); 
		
		TabItem tabConfigBC = new TabItem(tabConfig, SWT.NONE);
		tabConfigBC.setText(LocalizationHandler.getItem("app.dd.testing.gui.bc"));
		tabConfigBC.setToolTipText("");
		tabConfigBC.setControl(tableBC); 
		
		TabItem tabAnalysisNum = new TabItem(tabAnalysis, SWT.NONE);
		tabAnalysisNum.setText(LocalizationHandler.getItem("app.dd.testing.gui.analysis.numerical"));
		tabAnalysisNum.setToolTipText("");
		tabAnalysisNum.setControl(tableTesting); 
		
		TabItem tabAnalysisPlot = new TabItem(tabAnalysis, SWT.NONE);
		tabAnalysisPlot.setText(LocalizationHandler.getItem("app.dd.testing.gui.analysis.graphical"));
		tabAnalysisPlot.setToolTipText("");
		tabAnalysisPlot.setControl(chartTesting); 
		
		TabItem tabCharacteristicsPlot = new TabItem(tabAnalysis, SWT.NONE);
		tabCharacteristicsPlot.setText(LocalizationHandler.getItem("app.dd.testing.gui.analysis.characteristicdiag"));
		tabCharacteristicsPlot.setToolTipText("");
		tabCharacteristicsPlot.setControl(chartCC); 
		
		/* Add editor and cp */
		try {
			TableUtils.addCellEditor(tableOpPoint, this.getClass().getDeclaredMethod("setOperationalPoint"), this, new int[] {1});
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			TableUtils.addCellEditor(tableBC, this.getClass().getDeclaredMethod("setBoundaryConditions"), this, new int[] {1,2});
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
		itemPrint.setText(LocalizationHandler.getItem("app.gui.file.saveasimg"));
		itemPrint.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				saveChartCC(chartCC);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				// Not used
			}
		});
		
		MenuItem itemAddPump = new MenuItem(menuChartCC, SWT.NONE);
		itemAddPump.setText(LocalizationHandler.getItem("app.dd.testing.gui.analysis.characteristicdiag.addpump"));
		itemAddPump.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				openModelSelectGUI();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				// Not used
			}
		});

		
		chartCC.setMenu(menuChartCC);
		chartCC.getPlotArea().setMenu(menuChartCC);
		
		menuChartTesting = new Menu(chartTesting);
		MenuItem itemPrintTesting = new MenuItem(menuChartTesting, SWT.NONE);
		itemPrintTesting.setText(LocalizationHandler.getItem("app.gui.file.saveasimg"));
		itemPrintTesting.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				saveChartCC(chartTesting);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				// Not used
			}
		});

		
		chartTesting.setMenu(menuChartTesting);
		chartTesting.getPlotArea().setMenu(menuChartTesting);
		
		/* Update */
		update();
		
	}
	
	private void openModelSelectGUI(){
		SelectMachineComponentGUI compGUI= new SelectMachineComponentGUI(this.getShell());		
		String selection = compGUI.open("Pump");
		try {
			addPump(selection);
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
		
		this.setEnabled(false);

		this.redraw();
		this.layout();
		
		duct.setMaterial(material);
		
		
		updateTestingTable();
		updateOPTable();
		updateBCTable();
		updateChartTesting();
		updateChartCC();

		this.setEnabled(true);
	}
	
	public void setOperationalPoint(){
		readOPTable();
		update();
	}
	
	public void setBoundaryConditions(){
		readBCTable();
		update();
	}


	public void setMaterial(String type){
		this.material = new Material(type);
		
		duct.setMaterial(this.material);
		for (Pump p:pumps)
			p.getFluidPropertiesList().get(0).setMaterial(this.material);
		
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
					duct.setMaterial(new Material(tableOpPoint.getItem(i).getText(1)));
				}
			} catch(Exception e){
				// Not used
			}
			
	}
	
	private void readBCTable() {
		ADuctElement curElement;
		
		for(int i=0; i<tableBC.getItemCount(); i++){
			// Fetch element object by name
			curElement = duct.getElement(tableBC.getItem(i).getText(0).replaceAll("\\s*[0-9]:\\s*", ""));
			
			// First try: wall temperature
			try{
				curElement.setWallTemperature(Double.parseDouble(tableBC.getItem(i).getText(1)));
			} catch(Exception e1) {
				
				tableBC.getItem(i).setText(1,"");
				
				// Second try: heat source
				try{
					curElement.setHeatSource(Double.parseDouble(tableBC.getItem(i).getText(2)));
				} catch (Exception e2){
					tableBC.getItem(i).setText(2,"");
					curElement.setHeatSource(Double.NaN);
				}
			}
			
		}
		
		update();
	}
	
	private void updateBCTable(){
		for(TableItem it: tableBC.getItems()){
			it.dispose();
		}
		
		tableBC.clearAll();
		tableBC.setItemCount(0);
		
		addToBCTable(duct, "", this.flowRate, this.temperatureF, this.pressure);
		
		TableColumn[] columns = tableBC.getColumns();
        for (int j = 0; j < columns.length; j++) {
        	columns[j].pack();
        }
		
	}

	private void updateOPTable(){
		for(TableItem it: tableOpPoint.getItems()){
			it.dispose();
		}
		
		tableOpPoint.clearAll();
		tableOpPoint.setItemCount(0);
		
		TableItem itemFlowRate, itemPressure, itemTemperatureF, itemMaterial;
		
		itemFlowRate     = new TableItem(tableOpPoint, SWT.LEFT, 0);
		itemPressure     = new TableItem(tableOpPoint, SWT.LEFT, 1);
		itemTemperatureF = new TableItem(tableOpPoint, SWT.LEFT, 2);
		itemMaterial     = new TableItem(tableOpPoint, SWT.LEFT, 3);
		
		itemFlowRate.setText(0, LocalizationHandler.getItem("app.dd.testing.gui.flowrate"));
		itemFlowRate.setText(1, this.flowRate+"");
		itemFlowRate.setText(2, (new SiUnit("m^3/s")).toString());
		
		itemPressure.setText(0, LocalizationHandler.getItem("app.dd.testing.gui.pressurein"));
		itemPressure.setText(1, this.pressure+"");
		itemPressure.setText(2, (new SiUnit("Pa")).toString());
		
		itemTemperatureF.setText(0, LocalizationHandler.getItem("app.dd.testing.gui.temperaturefluid"));
		itemTemperatureF.setText(1, this.temperatureF+"");
		itemTemperatureF.setText(2, (new SiUnit("K")).toString());
		
		itemMaterial.setText(0, LocalizationHandler.getItem("app.dd.testing.gui.coolant"));
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
				// Not used
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
		SelectMaterialGUI matGUI = new SelectMaterialGUI(this.getShell());
    	String selection = matGUI.open();
    	if(selection != "" & selection !=null)
			setMaterial(selection);
	}
	
	private void addToTestingTable(Duct duct, String prefix, double flowRate, double temperatureIn, double pressureIn){
		
		for(ADuctElement e: duct.getElements()){
    		int i                    = tableTesting.getItemCount();
			final TableItem itemProp = new TableItem(tableTesting, SWT.RIGHT, i);
			
			itemProp.setText(0, prefix + e.getName());
			itemProp.setText(1, String.format("%.3g", e.getVolume()));
			itemProp.setText(2, String.format("%.3g", e.getSurface()));
			itemProp.setText(3, String.format("%.3g", e.getLength()));
			itemProp.setText(4, String.format("%.3g", e.getPressureDrop(flowRate, pressureIn, temperatureIn)));
			itemProp.setText(5, String.format("%.3g", e.getPressureLossCoefficient(flowRate, pressureIn, temperatureIn)));
			itemProp.setText(6, String.format("%.3g", e.getHTC(flowRate, pressureIn, temperatureIn)*e.getSurface()));
			itemProp.setText(7, String.format("%.3g", e.getHTC(flowRate, pressureIn, temperatureIn)));
			itemProp.setText(8, String.format("%.3g", flowRate*60E3));
			itemProp.setText(9, String.format("%.3g", e.getTemperatureOut(temperatureIn, flowRate, pressureIn)));
			itemProp.setText(10, String.format("%.3g", e.getWallTemperature(temperatureIn, flowRate, pressureIn)));
			
			if(e instanceof DuctBypass){
				addToTestingTable(((DuctBypass) e).getPrimary(),   prefix.replaceFirst("[0-9]:", "  ")+"  1: ", ((DuctBypass) e).getFlowRatePrimary(flowRate, pressureIn, temperatureIn), temperatureIn, pressureIn);
				addToTestingTable(((DuctBypass) e).getSecondary(), prefix.replaceFirst("[0-9]:", "  ")+"  2: ", ((DuctBypass) e).getFlowRateSecondary(flowRate, pressureIn, temperatureIn), temperatureIn, pressureIn);
			}
			
			temperatureIn = e.getTemperatureOut(temperatureIn, flowRate, pressureIn);
			pressureIn    = e.getPressureOut(flowRate, pressureIn, temperatureIn);
				
    	}
	}
	
	private void addToBCTable(Duct duct, String prefix, double flowRate, double temperatureIn, double pressureIn){
			
		for(ADuctElement e: duct.getElements()){
			if(!(e instanceof DuctFitting)){
				int i                    = tableBC.getItemCount();
				final TableItem itemProp = new TableItem(tableBC, SWT.RIGHT, i);
				
				itemProp.setText(0, prefix + e.getName());
				
				if(e instanceof DuctBypass){
					addToBCTable(((DuctBypass) e).getPrimary(),   prefix.replaceFirst("[0-9]:", "  ")+"  1: ", ((DuctBypass) e).getFlowRatePrimary(flowRate, pressureIn, temperatureIn), temperatureIn, pressureIn);
					addToBCTable(((DuctBypass) e).getSecondary(), prefix.replaceFirst("[0-9]:", "  ")+"  2: ", ((DuctBypass) e).getFlowRateSecondary(flowRate, pressureIn, temperatureIn), temperatureIn, pressureIn);
				}
				else {
					if(e.hasWallTemperature())
						itemProp.setText(1, String.format("%.3g", e.getWallTemperature(0, 0, 0)));
					else
						itemProp.setText(1, "       ");
					
					if(e.hasHeatSource())
						itemProp.setText(2, String.format("%.3g", e.getWallHeatFlux(0, 0, 0)));
					else
						itemProp.setText(2, "        ");
				}
			}
			
			temperatureIn = e.getTemperatureOut(temperatureIn, flowRate, pressureIn);
			pressureIn    = e.getPressureOut(flowRate, pressureIn, temperatureIn);
				
    	}
	}
	
	private void updateTestingTable(){
    	tableTesting.clearAll();
    	tableTesting.setItemCount(0);
    	
    	addToTestingTable(duct, "", this.flowRate, this.temperatureF, this.pressure);
    	
    	int i              = tableTesting.getItemCount();
		TableItem itemProp = new TableItem(tableTesting, SWT.RIGHT, i);
		
		itemProp.setText(0, "TOTAL");
		itemProp.setText(1, String.format("%.3g", duct.getVolume()));
		itemProp.setText(2, String.format("%.3g", duct.getSurface()));
		itemProp.setText(3, String.format("%.3g", duct.getLength()));
		itemProp.setText(4, String.format("%.3g", duct.getPressureDrop(flowRate, pressure, temperatureF)));
		itemProp.setText(5, String.format("%.3g", duct.getPressureLossCoefficient(flowRate, pressure, temperatureF)));
		itemProp.setText(6, String.format("%.3g", duct.getThermalResistance(flowRate, pressure, temperatureF)));
		itemProp.setText(7, String.format("%.3g", duct.getHTC(flowRate, pressure, temperatureF)));
		itemProp.setFont(new Font(itemProp.getDisplay(), "Arial", 10, SWT.BOLD));
    	
    	TableColumn[] columns = tableTesting.getColumns();
        for (int j = 0; j < columns.length; j++) {
        	columns[j].pack();
        }
    }
		
	private void updateChartTesting(){
		
		if(duct.getElements().size()==0) {
			lineSeriesPressureTesting.setXSeries(new double[] {0});
			lineSeriesPressureTesting.setYSeries(new double[] {0});
			
			lineSeriesHTCTesting.setXSeries(new double[] {0});
			lineSeriesHTCTesting.setYSeries(new double[] {0});
			
			lineSeriesTemperatureFluid.setXSeries(new double[] {0});
			lineSeriesTemperatureFluid.setYSeries(new double[] {0});
			
			lineSeriesTemperatureWall.setXSeries(new double[] {0});
			lineSeriesTemperatureWall.setYSeries(new double[] {0});
		}
		else{
		
			double[] pressure, position1, htc, position2, temperatureFluid, temperatureWall;
			ArrayList<Double> positionArr = getPositions(this.duct, 0), 
							  htcArr      = getHTCs(this.duct, this.pressure, this.temperatureF,this.flowRate),
							  pressureArr = getPressures(this.duct, this.pressure, this.temperatureF, this.flowRate),
							  tempFluidArr = getTemperaturesOut(this.duct, this.pressure, this.temperatureF,this.flowRate), 
							  tempWallArr = getTemperaturesWall(this.duct, this.pressure, this.temperatureF,this.flowRate);
			
			if(positionArr.size()<1)
				return;
			
			pressure  = new double[pressureArr.size()+1];
			temperatureFluid = new double[pressureArr.size()+1];
			temperatureWall = new double[positionArr.size()*2];
			position1 = new double[positionArr.size()+1];
			htc       = new double[htcArr.size()*2];
			position2 = new double[positionArr.size()*2];
			
			pressure[0]  = this.pressure;
			temperatureFluid[0] = this.temperatureF;
			position1[0] = 0;
			for(int i=0; i<pressureArr.size(); i++){
				pressure[i+1]  = pressureArr.get(i);
				temperatureFluid[i+1] = tempFluidArr.get(i);
				position1[i+1] = positionArr.get(i);
			}
			
			
			htc[0]       = htcArr.get(0);
			position2[0] = 0;
			temperatureWall[0] = tempWallArr.get(0);
			
			int i;
			for(i=0; i<positionArr.size(); i++){
				htc[2*i+1]       = htcArr.get(i);
				position2[2*i+1] = positionArr.get(i);
				
				temperatureWall[2*i+1] = tempWallArr.get(i);
				
				if(i<positionArr.size()-1){
					htc[2*i+2] = htcArr.get(i+1);
					position2[2*i+2] = positionArr.get(i);
					
					temperatureWall[2*i+2] = tempWallArr.get(i+1);
				}
			}		
			
			lineSeriesPressureTesting.setXSeries(position1);
			lineSeriesPressureTesting.setYSeries(pressure);
			
			lineSeriesHTCTesting.setXSeries(position2);
			lineSeriesHTCTesting.setYSeries(htc);
			
			lineSeriesTemperatureFluid.setXSeries(position1);
			lineSeriesTemperatureFluid.setYSeries(temperatureFluid);
			
			lineSeriesTemperatureWall.setXSeries(position2);
			lineSeriesTemperatureWall.setYSeries(temperatureWall);
		}
			
		chartTesting.getLegend().setVisible(false);
		
		try{
			chartTesting.getAxisSet().adjustRange();
			chartTesting.redraw();
		} catch(Exception e){
			System.err.println("Chart update failed!");
		}
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
			pressure[i] = duct.getPressureDrop(flowRate[i], this.pressure, this.temperatureF);
			htc[i]      = duct.getThermalResistance(flowRate[i], this.pressure, this.temperatureF);
		}

		
		lineSeriesPressureCC.setXSeries(flowRate);
		lineSeriesPressureCC.setYSeries(pressure);
		
		lineSeriesHTCCC.setXSeries(flowRate);
		lineSeriesHTCCC.setYSeries(htc);
		
		/* Pumps */
		pressurePump  = new double[N];
		
		
		for(int i=0; i<this.pumps.size(); i++){
			pumps.get(i).updatePumpMap(this.temperatureF);
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
		} catch(Exception e2){
			// Not used
		}
	}
	
	private void addPump(final Pump pump){
		// Check if type is unique
		for(Pump p: this.pumps)
			if(p.getType().equals(pump.getType()))
				return;
		
		pump.getFluidPropertiesList().get(0).setMaterial(this.material);
		pump.updatePumpMap(temperatureF);
		
		// Add Pump to array
		this.pumps.add(pump);
		
		// Create and add new line series
		ILineSeries line = (ILineSeries) chartCC.getSeriesSet().createSeries(SeriesType.LINE, LocalizationHandler.getItem("app.dd.testing.gui.analysis.characteristicdiag.pump")+": "+pump.getType());
		line.setYAxisId(1);
		line.setLineStyle(LineStyle.DOT);
		line.setLineWidth(2);
		line.setSymbolType(PlotSymbolType.NONE);
		this.lineSeriesPumps.add(line);
		
		// Add menu item to remove pump
		if(menuChartCC.getItemCount()==2)
			new MenuItem(menuChartCC, SWT.SEPARATOR);
		MenuItem item = new MenuItem(menuChartCC, SWT.NONE);
		item.setText(LocalizationHandler.getItem("app.dd.testing.gui.analysis.characteristicdiag.removepump")+": '"+pump.getType()+"'");
		item.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				removePump(pump.getType());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				// Not used
			}
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
			menuChartCC.getItem(2).dispose();
		
		// Adjust colors
		setPumpColors();
		
		updateChartCC();
			
	}
	
	private void saveChartCC(Chart chart) {
		FileDialog fileChooser = new FileDialog(getShell(), SWT.OPEN);
		fileChooser.setFilterExtensions(new String[]{"*.png", "*.jpg", "*.bmp"});
	    String fileName = fileChooser.open();
	    
	    if(null==fileName)
	    	return;
	      
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


	public void setDuct(Duct duct) {
		this.duct = duct;
	}
	
	/**
	 * Retuens and ArrayList with the position of the elements outlets
	 * @param duct
	 * @param offset
	 * @return
	 */
	private ArrayList<Double> getPositions(Duct duct, double offset){
		ArrayList<Double> positions = new ArrayList<Double>();
		
		for(ADuctElement e: duct.getElements()){
			if(e instanceof DuctBypass) {
				positions.addAll(getPositions(((DuctBypass) e).getPrimary(), offset));
				if( positions.size()>0)
					offset = positions.get(positions.size()-1);
			}
			else{
				offset+=e.getLength();
				positions.add(offset);
			}
		}
		
		return positions;
	}
	
	/**
	 * Returns an ArrayList with the pressures at the outlets of the elements
	 * @param duct
	 * @param pressureIn
	 * @param flowRate
	 * @return
	 */
	private ArrayList<Double> getPressures(Duct duct, double pressureIn, double temperatureIn, double flowRate){
		ArrayList<Double> pressures = new ArrayList<Double>();
		
		for(ADuctElement e: duct.getElements()){
			if(e instanceof DuctBypass) {
				pressures.addAll(getPressures(((DuctBypass) e).getPrimary(), pressureIn,((DuctBypass) e).getFlowRatePrimary(flowRate, pressureIn, this.temperatureF), temperatureIn));
				if( pressures.size()>0)
					pressureIn = pressures.get(pressures.size()-1);
			}
			else{
				pressureIn = e.getPressureOut(this.flowRate, pressureIn, temperatureIn);
				pressures.add(pressureIn);
			}
			
			temperatureIn = e.getTemperatureOut(temperatureIn, flowRate, pressureIn);
		}
		
		return pressures;
	}
	
	/**
	 * Returns an ArrayList with the temperatures at the outlets of the elements
	 * @param duct
	 * @param inlet
	 * @param flowRate
	 * @return
	 */
	private ArrayList<Double> getTemperaturesOut(Duct duct, double pressureIn, double temperatureIn, double flowRate){
		ArrayList<Double> temperatures = new ArrayList<Double>();
		
		for(ADuctElement e: duct.getElements()){
			if(e instanceof DuctBypass) {
				temperatures.addAll(getTemperaturesOut(((DuctBypass) e).getPrimary(), pressureIn, temperatureIn, ((DuctBypass) e).getFlowRatePrimary(flowRate, pressureIn, temperatureIn)));
				if( temperatures.size()>0)
					temperatureIn = temperatures.get(temperatures.size()-1);
			}
			else{
				temperatureIn = e.getTemperatureOut(temperatureIn, flowRate, pressureIn);
				temperatures.add(temperatureIn);
			}
			
			pressureIn = e.getPressureOut(flowRate, pressureIn, temperatureIn);
		}
		
		return temperatures;
	}
	
	/**
	 * Returns an ArrayList with the temperatures at the walls of the elements
	 * @param duct
	 * @param inlet
	 * @param flowRate
	 * @return
	 */
	private ArrayList<Double> getTemperaturesWall(Duct duct, double pressureIn, double temperatureIn, double flowRate){
		ArrayList<Double> temperatures = new ArrayList<Double>();
		
		for(ADuctElement e: duct.getElements()){
			if(e instanceof DuctBypass) {
				temperatures.addAll(getTemperaturesWall(((DuctBypass) e).getPrimary(), pressureIn, temperatureIn, ((DuctBypass) e).getFlowRatePrimary(flowRate, pressureIn, temperatureIn)));
				if( temperatures.size()>0)
					pressureIn = temperatures.get(temperatures.size()-1);
			}
			else{
				temperatures.add(e.getWallTemperature(temperatureIn, flowRate, pressureIn));
			}
			
			pressureIn    = e.getPressureOut(flowRate, pressureIn, temperatureIn);
			temperatureIn = e.getTemperatureOut(temperatureIn, flowRate, pressureIn);
		}
		
		return temperatures;
	}
	
	/**
	 * Returns an ArrayList with the HTC of each element
	 * @param duct
	 * @param pressureIn
	 * @param flowRate
	 * @return
	 */
	private ArrayList<Double> getHTCs(Duct duct, double pressureIn, double temperatureIn, double flowRate){
		ArrayList<Double> htcs = new ArrayList<Double>();
		ArrayList<Double> pressures = getPressures(duct, pressureIn, temperatureIn, flowRate);
		ArrayList<Double> temperatures = getTemperaturesOut(duct, pressureIn, temperatureIn, flowRate);
		
		
		for(ADuctElement e: duct.getElements()){
			if(e instanceof DuctBypass) {
				if( htcs.size()>0)		
					htcs.addAll(getHTCs(((DuctBypass) e).getPrimary(), pressures.get(htcs.size()-1), temperatures.get(htcs.size()-1),((DuctBypass) e).getFlowRatePrimary(flowRate, pressureIn, this.temperatureF)));
				else
					htcs.addAll(getHTCs(((DuctBypass) e).getPrimary(), pressureIn, temperatureIn, ((DuctBypass) e).getFlowRatePrimary(flowRate, pressureIn, this.temperatureF)));
			}
			else{
				htcs.add(e.getHTC(flowRate,  pressures.get(htcs.size()), temperatures.get(htcs.size()))*e.getSurface());
			}
		}	
		
		return htcs;
	}
	
	private String getElementName(Duct duct, double x) {
		
		double pos = 0;
		
		for(ADuctElement e: duct.getElements())
			if(pos+e.getLength()>x){
				if(e instanceof DuctBypass)
					return e.getName()+": "+getElementName(((DuctBypass) e).getPrimary(), x-pos);
				else
					return e.getName();
			}
			else
				pos+=e.getLength();
		
		return "";
	}

}
