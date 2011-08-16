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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.Chart;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries.SeriesType;

import ch.ethz.inspire.emod.gui.utils.ConsumerData;
import ch.ethz.inspire.emod.model.units.Unit;

/**
 * @author dhampl
 *
 */
public class BarChartGUI {

	private static Chart chart=null;
	
	public static Chart createBarChart(Composite parent, List<ConsumerData> data) {
		if(chart==null)
			chart = new Chart(parent, SWT.NONE);
		else {
			chart.dispose();
			chart = new Chart(parent, SWT.NONE);
		}
		List<Double> s = new ArrayList<Double>();
		List<String> xs = new ArrayList<String>();
		for(ConsumerData cd:data){
			for(int i=0;i<cd.getActive().size();i++){
				if(cd.getActive().get(i) && cd.getUnits().get(i)==Unit.WATT) {
					s.add(cd.getEnergy().get(i));
					xs.add(cd.getConsumer()+"."+cd.getNames().get(i));
				}
			}
		}
		double[] serie = new double[s.size()];
		for(int i=0;i<serie.length;i++)
			serie[i] = s.get(i);
		IBarSeries series = (IBarSeries) chart.getSeriesSet().createSeries(SeriesType.BAR, "Energy per consumer");
		series.setYSeries(serie);
		String[] xss=new String[xs.size()];
		xs.toArray(xss);
		chart.getAxisSet().getXAxis(0).setCategorySeries(xss);
		chart.getAxisSet().getXAxis(0).enableCategory(true);
		chart.getAxisSet().adjustRange();
		
		return chart;
	}
}
