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

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.Range;

import ch.ethz.inspire.emod.gui.utils.ShowButtons;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.parameters.PhysicalValue;
import ch.ethz.inspire.emod.utils.Algo;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.MaterialConfigReader;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * GUI to edit material properties
 * 
 * @author sizuest
 *
 */
public class EditMaterialGUI extends AConfigGUI {

	private MaterialConfigReader material;
	private Table tableMaterialProperties;
	private Chart chart;

	private Color colorDensity = new Color(getDisplay(), new RGB(0, 0, 255)),
			colorHeatCap = new Color(getDisplay(), new RGB(255, 0, 0)),
			colorViscosity = new Color(getDisplay(), new RGB(0, 100, 0));

	private ILineSeries lineDensity, lineHeatCap, lineViscosity;

	private TabFolder tabFolder;

	/**
	 * @param parent
	 * @param style
	 * @param materialName
	 */
	public EditMaterialGUI(Composite parent, int style, String materialName) {
		super(parent, style, ShowButtons.ALL);
		

		try {
			new Material(materialName);
			material = new MaterialConfigReader("Material", materialName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.getContent().setLayout(new GridLayout(1, true));

		tabFolder = new TabFolder(this.getContent(), SWT.FILL);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		tabFolder.setLayout(new GridLayout(1, true));

		tableMaterialProperties = new Table(tabFolder, SWT.FILL | SWT.SINGLE
				| SWT.V_SCROLL);
		tableMaterialProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true, 1, 1));
		tableMaterialProperties.setLinesVisible(true);
		tableMaterialProperties.setHeaderVisible(true);

		String[] titles = {
				LocalizationHandler.getItem("app.gui.compdb.property"),
				LocalizationHandler.getItem("app.gui.compdb.value"),
				LocalizationHandler.getItem("app.gui.compdb.unit"),
				LocalizationHandler.getItem("app.gui.compdb.description") };
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(tableMaterialProperties,
					SWT.NULL);
			column.setText(titles[i]);
		}

		try {
			TableUtils.addCellEditor(tableMaterialProperties, this,
					new int[] { 1 });
		} catch (Exception e) {
			e.printStackTrace();
		}

		chart = new Chart(tabFolder, SWT.FILL);
		chart.getAxisSet().getXAxis(0).getTitle().setText("[K]");
		chart.getAxisSet().getXAxis(0).getTitle()
				.setForeground(Display.getDefault().getSystemColor(0));
		chart.getAxisSet().getXAxis(0).getTick()
				.setForeground(Display.getDefault().getSystemColor(0));
		chart.getTitle().setVisible(false);
		chart.getAxisSet().getYAxis(0).getTitle().setText("[kg/m³]");
		chart.getAxisSet().getYAxis(0).getTitle().setForeground(colorDensity);
		chart.getAxisSet().getYAxis(0).getTick().setForeground(colorDensity);
		chart.getAxisSet().createYAxis();
		chart.getAxisSet().getYAxis(1).getTitle().setText("[J/kg/K]");
		chart.getAxisSet().getYAxis(1).getTitle().setForeground(colorHeatCap);
		chart.getAxisSet().getYAxis(1).getTick().setForeground(colorHeatCap);
		chart.getAxisSet().createYAxis();
		chart.getAxisSet().getYAxis(2).getTitle().setText("mPa s");
		chart.getAxisSet().getYAxis(2).getTitle().setForeground(colorViscosity);
		chart.getAxisSet().getYAxis(2).getTick().setForeground(colorViscosity);

		lineDensity = (ILineSeries) chart.getSeriesSet().createSeries(
				SeriesType.LINE, "Density");
		lineHeatCap = (ILineSeries) chart.getSeriesSet().createSeries(
				SeriesType.LINE, "Heat Capacity");
		lineViscosity = (ILineSeries) chart.getSeriesSet().createSeries(
				SeriesType.LINE, "Viscosity");
		lineDensity.setYAxisId(0);
		lineHeatCap.setYAxisId(1);
		lineViscosity.setYAxisId(2);
		lineDensity.setLineColor(colorDensity);
		lineHeatCap.setLineColor(colorHeatCap);
		lineViscosity.setLineColor(colorViscosity);
		lineDensity.setSymbolType(PlotSymbolType.CIRCLE);
		lineHeatCap.setSymbolType(PlotSymbolType.CROSS);
		lineViscosity.setSymbolType(PlotSymbolType.PLUS);
		lineDensity.setSymbolColor(colorDensity);
		lineHeatCap.setSymbolColor(colorHeatCap);
		lineViscosity.setSymbolColor(colorViscosity);

		TabItem tabDuctDBItem = new TabItem(tabFolder, SWT.NONE);
		tabDuctDBItem.setText("Input");
		tabDuctDBItem.setToolTipText("Input");
		tabDuctDBItem.setControl(tableMaterialProperties);

		TabItem tabStatisticsItem = new TabItem(tabFolder, SWT.NONE);
		tabStatisticsItem.setText("Plot");
		tabStatisticsItem.setToolTipText("Plot");
		tabStatisticsItem.setControl(chart);

		update();
	}

	/**
	 * GUI in a new shell
	 * 
	 * @param parent
	 * @param type
	 */
	public static void editMaterialGUI(final Shell parent, String type) {

		final Shell shell = new Shell(parent, SWT.TITLE | SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.MAX | SWT.RESIZE);
		shell.setText("Edit Material: " + type);
		shell.setLayout(new GridLayout(1, true));
		shell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final EditMaterialGUI gui = new EditMaterialGUI(shell, SWT.NONE, type);

		shell.pack();

		shell.layout();
		shell.redraw();
		shell.open();

		shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		shell.addControlListener(new ControlListener() {

			@Override
			public void controlResized(ControlEvent e) {
				gui.layout();
			}

			@Override
			public void controlMoved(ControlEvent e) {
				gui.layout();
			}
		});

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
	}

	/**
	 * Component Edit GUI for creating a new Material
	 * 
	 * @param parent
	 */
	public static void newMaterialGUI(final Shell parent) {
		final Shell shell = new Shell(parent, SWT.TITLE | SWT.SYSTEM_MODAL | SWT.CLOSE | SWT.MAX);
		shell.setText(LocalizationHandler.getItem("app.gui.matdb.newmat"));
		shell.setLayout(new GridLayout(2, false));

		// Text "Material name" of the material
		Label textMaterialName = new Label(shell, SWT.NONE);
		textMaterialName.setText(LocalizationHandler.getItem("app.gui.matdb.matname"));
		textMaterialName.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, 1));

		// Textfield to let the user enter the desired name of the material
		final Text textMaterialNameValue = new Text(shell, SWT.BORDER);
		textMaterialNameValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

		// button to continue
		Button buttonContinue = new Button(shell, SWT.NONE);
		buttonContinue.setText(LocalizationHandler.getItem("app.gui.continue"));
		// selection Listener for the button, actions when button is pressed
		buttonContinue.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				String stringMaterialNameValue = textMaterialNameValue.getText();

				// copy the example type of the selected component and create a
				// copy
				// SOURCE for the file copy:
				// http://www.javapractices.com/topic/TopicAction.do?Id=246
				Path from = Paths.get(PropertiesHandler
						.getProperty("app.MaterialDBPathPrefix")
						+ "/"
						+ "Material_Example.xml");
				Path to = Paths.get(PropertiesHandler
						.getProperty("app.MaterialDBPathPrefix")
						+ "/"
						+ "Material_" + stringMaterialNameValue + ".xml");
				// overwrite existing file, if exists
				CopyOption[] options = new CopyOption[] {
						StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.COPY_ATTRIBUTES };
				try {
					Files.copy(from, to, options);
				} catch (IOException e) {
					e.printStackTrace();
				}

				shell.close();

				// open the edit ComponentEditGUI with the newly created
				// component file
				EditMaterialGUI
						.editMaterialGUI(parent, stringMaterialNameValue);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				// Not used
			}
		});
		buttonContinue.setLayoutData(new GridData(SWT.END, SWT.CENTER, false,
				true, 2, 1));

		shell.pack();

		// open the new shell
		shell.open();
	}

	@Override
	public void update() {
		tableMaterialProperties.setItemCount(0);

		for (String key : material.getKeys()) {
			TableItem item = new TableItem(tableMaterialProperties, SWT.NONE);
			item.setText(0, key);
			try {
				String value, unit;
				try{
					PhysicalValue pvalue = material.getPhysicalValue(key);
					value = pvalue.valuesToString();
					unit  = pvalue.getUnit().toString();
				}catch (Exception e2){
					value = material.getString(key);
					unit = "";
				}
				
				item.setText(1, value);
				item.setText(2, unit);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		TableColumn[] columns = tableMaterialProperties.getColumns();
		for (int j = 0; j < columns.length; j++) {
			columns[j].pack();
		}

		try {
			lineDensity.setXSeries(material.getPhysicalValue("TemperatureSamples").getValues());
			lineDensity.setYSeries(material.getPhysicalValue("DensityMatrix").getValues());
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			lineHeatCap.setXSeries(material.getPhysicalValue("TemperatureSamples").getValues());
			double[] cp = new double[material.getPhysicalValue("TemperatureSamples").getValues().length];
			if (material.getPhysicalValue("HeatCapacity").getValues().length < cp.length)
				for (int i = 0; i < cp.length; i++)
					cp[i] = material.getPhysicalValue("HeatCapacity").getValue();
			else
				cp = material.getPhysicalValue("HeatCapacity").getValues();

			lineHeatCap.setYSeries(cp);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			lineViscosity.setXSeries(material.getPhysicalValue("TemperatureSamples").getValues());
			lineViscosity.setYSeries(material.getPhysicalValue("ViscositySamples").getValues());
		} catch (Exception e) {
			e.printStackTrace();
		}

		chart.getAxisSet().adjustRange();
		try {
			chart.getAxisSet().getXAxis(0).setRange(new Range(
									Algo.getMinimum(material.getPhysicalValue("TemperatureSamples").getValues())*.9,
									Algo.getMaximum(material.getPhysicalValue("TemperatureSamples").getValues())*1.1));
		} catch (Exception e) {
			e.printStackTrace();
		}
		chart.redraw();
	}

	@Override
	public void save() {

		for (TableItem ti : tableMaterialProperties.getItems()) {
			material.setValue(ti.getText(0), ti.getText(1)+" "+ti.getText(2));
		}

		try {
			material.saveValues();
		} catch (IOException e) {
			e.printStackTrace();
		}

		update();

	}

	@Override
	public void reset() {
		try {
			material = new MaterialConfigReader(material.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

		update();
	}
}
