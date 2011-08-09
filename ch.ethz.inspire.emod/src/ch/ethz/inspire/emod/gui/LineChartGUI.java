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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries.SeriesType;

/**
 * @author dhampl
 *
 */
public class LineChartGUI {

	public static Chart createChart(Composite parent, List<double[]> series) {
		Chart chart = new Chart(parent, SWT.NONE);
		int color = 3;
		for(double[] serie: series) {
			ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "s"+color);
			lineSeries.setYSeries(serie);
			lineSeries.setLineColor(Display.getDefault().getSystemColor(color));
			lineSeries.enableArea(true);
			color++;
		}
		
		chart.getAxisSet().adjustRange();
		return chart;
	}
}
