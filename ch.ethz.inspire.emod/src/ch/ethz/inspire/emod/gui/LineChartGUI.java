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
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;

import ch.ethz.inspire.emod.gui.utils.ConsumerData;

/**
 * creates a chart from active ConsumerData objects. currently only one chart can be
 * drawn.
 * 
 * @author dhampl
 *
 */
public class LineChartGUI {

	private static Chart chart=null;
	
	public static Chart createChart(Composite parent, List<ConsumerData> data) {
		if(chart==null)
			chart = new Chart(parent, SWT.NONE);
		else {
			chart.dispose();
			chart = new Chart(parent, SWT.NONE);
		}
		
		int color = 3;
		for(ConsumerData cd: data) {
			for(int i=0;i<cd.getNames().size();i++){
				if(cd.getActive().get(i)) {
					ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, cd.getConsumer()+"."+cd.getNames().get(i));
					lineSeries.setYSeries(cd.getValues().get(i));
					lineSeries.setSymbolType(PlotSymbolType.NONE);
					
					lineSeries.setLineColor(Display.getDefault().getSystemColor(color));
					lineSeries.enableArea(true);
					color++; color++; color++;
				}
			}
		}
		
		chart.getAxisSet().adjustRange();
		return chart;
	}
	
}
