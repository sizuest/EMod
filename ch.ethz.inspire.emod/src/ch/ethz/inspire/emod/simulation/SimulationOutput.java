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

package ch.ethz.inspire.emod.simulation;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.inspire.emod.utils.Floodable;
import ch.ethz.inspire.emod.utils.FluidContainer;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.Unit;

/**
 * 
 * @author andreas
 *
 */
public class SimulationOutput {

	private BufferedWriter outfile;
	private ArrayList<MachineComponent> mclist;
	private List<ASimulationControl> simlist;
	
	private int flushcnt;
	private DecimalFormat format;
	
	private String separator = "\t";
	
	/**
	 * Create data logging file. Write file header.
	 * 
	 * @param filename Name of file to write to simulation output to.
	 * @param list List of model components.
	 * @param slist List of simulation control objects.
	 */
	public SimulationOutput(String filename, ArrayList<MachineComponent> list, 
			List<ASimulationControl> slist)
	{
		/* Init variables: */
		flushcnt = 0;
		mclist = list;
		simlist = slist;
		format = new DecimalFormat("####0.00");
		
		try {
			/* Create output file: */
			outfile = new BufferedWriter(new FileWriter(filename));

			/* ****** Make file header: ******* */
			/* 1st Line: Time\tMcName1 Input 1\tMcName1 Input 2\tMcName1 Output 1\t... */
			outfile.write("Time");
			for(MachineComponent mc : mclist) {
				for (int i=0; i<mc.getComponent().getInputs().size(); i++) {
					if(mc.getComponent().getInputs().get(i).hasReference())
						continue;
					if(mc.getComponent() instanceof Floodable & mc.getComponent().getInputs().get(i) instanceof FluidContainer){
							outfile.write(separator + mc.getName() + "-Input-" + (i+1) + "-T");
							outfile.write(separator + mc.getName() + "-Input-" + (i+1) + "-V");
							outfile.write(separator + mc.getName() + "-Input-" + (i+1) + "-p");
					}
					else
						outfile.write(separator + mc.getName() + "-Input-" + (i+1));
				}
				for (int i=0; i<mc.getComponent().getOutputs().size(); i++) {
					if(mc.getComponent().getOutputs().get(i).hasReference())
						continue;
					if(mc.getComponent() instanceof Floodable & mc.getComponent().getOutputs().get(i) instanceof FluidContainer){
							outfile.write(separator + mc.getName() + "-Output-" + (i+1) + "-T");
							outfile.write(separator + mc.getName() + "-Output-" + (i+1) + "-V");
							outfile.write(separator + mc.getName() + "-Output-" + (i+1) + "-p");
					}
					else
						outfile.write(separator + mc.getName() + "-Output-" + (i+1));
				}
				if(null!=mc.getComponent().getDynamicStateList())
					for (int i=0; i<mc.getComponent().getDynamicStateList().size(); i++){
						outfile.write(separator + mc.getName() +"-State-" + (i+1));
					}
			}
			for(ASimulationControl sc : simlist) {
				outfile.write(separator + sc.getName() + "-Sim");
				outfile.write(separator + sc.getName() + "-Sim");
			}
			outfile.write("\n");
			/* 2nd line: Time\tMcName1.InputName1\tMcName1.InputName2\tMcName1.OutputName1\t...*/
			outfile.write("   ");
			for(MachineComponent mc : mclist) {
				for (IOContainer input : mc.getComponent().getInputs()) {
					if(input.hasReference())
						continue;
					if(mc.getComponent() instanceof Floodable & input instanceof FluidContainer){
							outfile.write(separator + mc.getName() + "." + input.getName() + "-Temperature");
							outfile.write(separator + mc.getName() + "." + input.getName() + "-FlowRate");
							outfile.write(separator + mc.getName() + "." + input.getName() + "-Pressure");
					}
					else
						outfile.write(separator + mc.getName() + "." + input.getName());
				}
				for (IOContainer output : mc.getComponent().getOutputs()) {
					if(output.hasReference())
						continue;
					if(mc.getComponent() instanceof Floodable & output  instanceof FluidContainer){
							outfile.write(separator + mc.getName() + "." + output.getName() + "-Temperature");
							outfile.write(separator + mc.getName() + "." + output.getName() + "-FlowRate");
							outfile.write(separator + mc.getName() + "." + output.getName() + "-Pressure");
					}
					else
						outfile.write(separator + mc.getName() + "." + output.getName());
				}
				if(null!=mc.getComponent().getDynamicStateList())
					for (DynamicState state : mc.getComponent().getDynamicStateList()){
						outfile.write(separator + mc.getName() +"." + state.getName());
					}
			}
			for(ASimulationControl sc : simlist) {
				outfile.write(separator + sc.getOutput().getName() + " State");
				outfile.write(separator + sc.getOutput().getName() + " Value");
			}
			outfile.write("\n");
			/* 3rd line: [s]\t[WATT]\t[TEMP]\t...*/
			outfile.write("[s]");
			for(MachineComponent mc : mclist) {
				for (IOContainer input : mc.getComponent().getInputs()) {
					if(input.hasReference())
						continue;
					if(mc.getComponent() instanceof Floodable & input instanceof FluidContainer){
							outfile.write(separator + "[" + (new SiUnit(Unit.KELVIN)).toString() + "]");
							outfile.write(separator + "[" + (new SiUnit(Unit.METERCUBIC_S)).toString() + "]");
							outfile.write(separator + "[" + (new SiUnit(Unit.PA)).toString() + "]");
					}
					else
						outfile.write(separator + "[" + input.getUnit().toString() + "]");
				}
				for (IOContainer output : mc.getComponent().getOutputs()) {
					if(output.hasReference())
						continue;
					if(mc.getComponent() instanceof Floodable & output instanceof FluidContainer){
							outfile.write(separator + "[" + (new SiUnit(Unit.KELVIN)).toString() + "]");
							outfile.write(separator + "[" + (new SiUnit(Unit.METERCUBIC_S)).toString() + "]");
							outfile.write(separator + "[" + (new SiUnit(Unit.PA)).toString() + "]");
					}
					else
						outfile.write(separator + "[" + output.getUnit().toString() + "]");
				}
				if(null!=mc.getComponent().getDynamicStateList())
					for (DynamicState state : mc.getComponent().getDynamicStateList()){
						outfile.write(separator + "[" + state.getUnit().toString() +"]" );
					}
			}
			for(ASimulationControl sc : simlist) {
				outfile.write(separator + "       ");
				outfile.write(separator + "[" + sc.getOutput().getUnit().toString() + "]");
			}
			outfile.write("\n");
			
		}
		catch (IOException e) {
		    e.printStackTrace();
		    System.exit(-1);
		}
	}

	/**
	 * Write samples to data logger.
	 * 
	 * @param time Actual time in [s].
	 */
	public void logData(double time) 
	{
		try {
			outfile.write(format.format(time));
			for(MachineComponent mc : mclist) {
				for (IOContainer input : mc.getComponent().getInputs()){
					if(input.hasReference())
						continue;
					if(mc.getComponent() instanceof Floodable & input instanceof FluidContainer){
							outfile.write(separator + ((FluidContainer)input).getTemperature());
							outfile.write(separator + ((FluidContainer)input).getFluidCircuitProperties().getFlowRate());
							outfile.write(separator + ((FluidContainer)input).getPressure());
					}
					else
						outfile.write(separator + input.getValue());
				}
				for (IOContainer output : mc.getComponent().getOutputs()){
					if(output.hasReference())
						continue;
					if(mc.getComponent() instanceof Floodable & output instanceof FluidContainer){
							outfile.write(separator + ((FluidContainer)output).getTemperature());
							outfile.write(separator + ((FluidContainer)output).getFluidCircuitProperties().getFlowRate());
							outfile.write(separator + ((FluidContainer)output).getPressure());
					}
					else
						outfile.write(separator + output.getValue());
				}
				if(null!=mc.getComponent().getDynamicStateList())
					for (DynamicState state : mc.getComponent().getDynamicStateList()){
						outfile.write(separator +state.getValue());
					}
			}
			for(ASimulationControl sc : simlist) {
				outfile.write(separator + sc.getState().toString());
				outfile.write(separator + sc.getOutput().getValue());
			}
			outfile.write("\n");
						
			// Flush file every 32th samples only.
			if (flushcnt >= 32) { 
				outfile.flush();
				flushcnt = 0;
			}
			flushcnt++;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Close data logger.
	 */
	public void close()
	{
		try {
			outfile.flush();
			outfile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
