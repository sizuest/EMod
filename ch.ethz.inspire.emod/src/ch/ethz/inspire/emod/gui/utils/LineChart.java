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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;

/**
 * creates a chart from active ConsumerData objects. currently only one chart
 * can be drawn.
 * 
 * @author dhampl
 * 
 */
public class LineChart {

	private static Chart chart = null;

	/**
	 * @param parent
	 * @param data
	 * @return
	 */
	public static Chart createChart(Composite parent, List<ConsumerData> data) {
		Map<String, Integer> axisMap = new HashMap<String, Integer>();
		ArrayList<Integer> unitCounter = new ArrayList<Integer>();
		ArrayList<Integer> colorCounter = new ArrayList<Integer>();
		ArrayList<ILineSeries> lines = new ArrayList<ILineSeries>();

		if (chart == null) {
			chart = new Chart(parent, SWT.NONE);
		} else {
			chart.dispose();
			chart = new Chart(parent, SWT.NONE);
		}

		chart.getTitle().setText("");

		int color = 3;
		for (ConsumerData cd : data) {
			for (int i = 0; i < cd.getNames().size(); i++) {
				if (cd.getActive().get(i)) {
					int axId;
					// Check for axis
					if (axisMap.containsKey(cd.getUnits().get(i).toString())) {
						axId = axisMap.get(cd.getUnits().get(i).toString());
						unitCounter.set(axId, unitCounter.get(axId) + 1);
					} else {
						if (chart.getAxisSet().getYAxes().length == 1
								& axisMap.size() == 0)
							axId = 0;
						else
							axId = chart.getAxisSet().createYAxis();

						axisMap.put(cd.getUnits().get(i).toString(), axId);
						unitCounter.add(axId, 1);

						chart.getAxisSet()
								.getYAxis(axId)
								.getTitle()
								.setText(
										"[" + cd.getUnits().get(i).toString()
												+ "]");
						chart.getAxisSet()
								.getYAxis(axId)
								.getTick()
								.setForeground(
										Display.getDefault().getSystemColor(
												color));
						chart.getAxisSet()
								.getYAxis(axId)
								.getTitle()
								.setForeground(
										Display.getDefault().getSystemColor(
												color));

						color++;
					}

					ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet()
							.createSeries(
									SeriesType.LINE,
									cd.getConsumer() + "."
											+ cd.getNames().get(i));
					lineSeries.setYSeries(cd.getValues().get(i));
					lineSeries.setSymbolType(PlotSymbolType.NONE);
					lineSeries.setYAxisId(axId);
					lineSeries.setLineColor(chart.getAxisSet().getYAxis(axId)
							.getTick().getForeground());
					// lineSeries.enableArea(true);

					lines.add(lineSeries);

				}
			}

			if (unitCounter.size() > 1) {

				/* Adjust colors */
				for (int i = 0; i < unitCounter.size(); i++)
					colorCounter.add(i, 0);

				for (ILineSeries l : lines) {
					int axId = l.getYAxisId(), r, g, b;
					Color lineColor = chart.getAxisSet().getYAxis(axId)
							.getTick().getForeground();

					r = (int) (lineColor.getRed() * (1 - .75
							/ unitCounter.get(axId) * colorCounter.get(axId)));
					g = (int) (lineColor.getGreen() * (1 - .75
							/ unitCounter.get(axId) * colorCounter.get(axId)));
					b = (int) (lineColor.getBlue() * (1 - .75
							/ unitCounter.get(axId) * colorCounter.get(axId)));

					colorCounter.set(axId, colorCounter.get(axId) + 1);

					l.setLineColor(new Color(Display.getCurrent(), r, g, b));
				}
			} else {
				color = 3;
				for (ILineSeries l : lines) {
					l.setLineColor(Display.getDefault().getSystemColor(color));
					color++;
				}
				chart.getAxisSet().getYAxis(0).getTick()
						.setForeground(Display.getDefault().getSystemColor(0));
				chart.getAxisSet().getYAxis(0).getTitle()
						.setForeground(Display.getDefault().getSystemColor(0));
			}
		}

		/* Format time axis */
		chart.getAxisSet().getXAxis(0).getTitle().setText("time [s]");
		chart.getAxisSet().getXAxis(0).getTick()
				.setForeground(Display.getDefault().getSystemColor(0));
		chart.getAxisSet().getXAxis(0).getTitle()
				.setForeground(Display.getDefault().getSystemColor(0));

		/* Adjust range */
		chart.getAxisSet().adjustRange();

		return chart;
	}
}
