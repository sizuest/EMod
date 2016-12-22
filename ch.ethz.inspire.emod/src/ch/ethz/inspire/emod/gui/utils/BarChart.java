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
package ch.ethz.inspire.emod.gui.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;

import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * creates bar chart. the chart is a singleton implementation
 * 
 * @author dhampl
 * 
 */
public class BarChart {

	private static Chart chart = null;

	/**
	 * creates a bar chart from {@link ConsumerData} objects. only energy values
	 * are displayed
	 * 
	 * @param parent
	 * @param data
	 * @return
	 */
	public static Chart createBarChart(Composite parent, List<ConsumerData> data) {
		if (chart == null)
			chart = new Chart(parent, SWT.NONE);
		else {
			chart.dispose();
			chart = new Chart(parent, SWT.NONE);
		}

		if (0 == data.size())
			return chart;

		ArrayList<String> consumers = new ArrayList<String>();

		for (ConsumerData cd : data) {
			for (int i = 0; i < cd.getActive().size(); i++) {
				if (cd.getUnits().get(i).equals(new SiUnit(Unit.WATT))) {
					consumers.add(cd.getConsumer());
					break;
				}
			}
		}

		double[] ptotal = new double[consumers.size()];
		double[] ploss = new double[consumers.size()];
		double[] puse = new double[consumers.size()];
		// double[] xs = new ArrayList<String>();

		int idx = 0;
		for (ConsumerData cd : data) {
			for (int i = 0; i < cd.getActive().size(); i++) {
				if (cd.getUnits().get(i).equals(new SiUnit(Unit.WATT))) {
					idx = consumers.indexOf(cd.getConsumer());
					switch (cd.getNames().get(i)) {
					case "PTotal":
						ptotal[idx] = cd.getEnergy().get(i);
						break;
					case "PUse":
						puse[idx] = cd.getEnergy().get(i);
						break;
					case "PLoss":
						ploss[idx] = cd.getEnergy().get(i);
						break;
					}

				}
			}
		}

		IBarSeries seriesTotal = (IBarSeries) chart.getSeriesSet()
				.createSeries(SeriesType.BAR, "Input energy");
		IBarSeries seriesUse = (IBarSeries) chart.getSeriesSet().createSeries(
				SeriesType.BAR, "Usefull energy");
		IBarSeries seriesLoss = (IBarSeries) chart.getSeriesSet().createSeries(
				SeriesType.BAR, "Losses");

		seriesTotal.setYSeries(ptotal);
		seriesUse.setYSeries(puse);
		seriesLoss.setYSeries(ploss);

		seriesTotal.setBarColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_BLUE));
		seriesUse.setBarColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_GREEN));
		seriesLoss.setBarColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_RED));

		String[] xss = new String[consumers.size()];
		consumers.toArray(xss);
		chart.getAxisSet().getXAxis(0).setCategorySeries(xss);
		chart.getAxisSet().getXAxis(0).enableCategory(true);
		chart.getAxisSet().getXAxis(0).getTick().setTickLabelAngle(45);
		chart.getAxisSet().getXAxis(0).getTick()
				.setForeground(Display.getDefault().getSystemColor(0));
		chart.getAxisSet().getXAxis(0).getTitle()
				.setForeground(Display.getDefault().getSystemColor(0));
		chart.getAxisSet().adjustRange();
		chart.getTitle().setText(
				LocalizationHandler.getItem("app.gui.analysis.barchart.title"));
		chart.getAxisSet().getXAxis(0).getTitle().setText("");

		chart.getAxisSet().getYAxis(0).getTitle()
				.setText("[" + (new SiUnit("J")).toString() + "]");
		chart.getAxisSet().getYAxis(0).getTick()
				.setForeground(Display.getDefault().getSystemColor(0));
		chart.getAxisSet().getYAxis(0).getTitle()
				.setForeground(Display.getDefault().getSystemColor(0));

		chart.getTitle().setVisible(false);

		chart.getPlotArea().addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				for (ISeries series : chart.getSeriesSet().getSeries()) {
					Rectangle[] rs = ((IBarSeries) series).getBounds();
					for (int i = 0; i < rs.length; i++) {
						if (rs[i] != null) {
							if (rs[i].x < e.x && e.x < rs[i].x + rs[i].width
									&& rs[i].y < e.y
									&& e.y < rs[i].y + rs[i].height) {
								setToolTipText(series, i);
								return;
							}
						}
					}
				}
				chart.getPlotArea().setToolTipText(null);
			}

			private void setToolTipText(ISeries series, int index) {
				chart.getPlotArea().setToolTipText(
						series.getYSeries()[index] / 3600000 + " kWh");
			}
		});

		return chart;
	}
}
