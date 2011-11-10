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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;


/**
 * @author dhampl
 *
 */
public class StackedAreaChart {

	private static Chart chart=null;
	private static List<ConsumerData> localdata;
	
	public static Chart createChart(Composite parent, List<ConsumerData> data) {
		if(chart==null)
			chart = new Chart(parent, SWT.NONE);
		else {
			chart.dispose();
			chart = new Chart(parent, SWT.NONE);
		}
		
		int color = 3;
		localdata = new ArrayList<ConsumerData>(data);
		
		sort();
		
		List<double[]> series = createStackedSeries();
		
		for(int i=0;i<localdata.size();i++) {
			ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, localdata.get(i).getConsumer());
			lineSeries.setYSeries(series.get(i));
			lineSeries.setSymbolType(PlotSymbolType.NONE);
					
			lineSeries.setLineColor(Display.getDefault().getSystemColor(color));
			lineSeries.enableArea(true);
			color++; color++; color++;
		}
		
		chart.getAxisSet().adjustRange();
		
		return chart;
	}
	
	private static void sort() {
		for(int i=0;i<localdata.size();i++) {
			for(int j=i;j<localdata.size();j++) {
				if(localdata.get(i).getVariance()>localdata.get(j).getVariance()) {
					ConsumerData temp = localdata.get(i);
					localdata.set(i, localdata.get(j));
					localdata.set(j, temp);
				}
			}
		}
	}
	
	private static List<double[]> createStackedSeries() {
		List<double[]> result = new ArrayList<double[]>();
		result.add(localdata.get(0).getPTotal());
		for(int i=1;i<localdata.size();i++) {
			double[] temp = new double[localdata.get(i).getPTotal().length];
			for(int j=0;j<temp.length;j++) {
				temp[j] = localdata.get(i).getPTotal()[j] + result.get(i-1)[j];
			}
			result.add(temp);
		}
		
		return result;
	}
}
