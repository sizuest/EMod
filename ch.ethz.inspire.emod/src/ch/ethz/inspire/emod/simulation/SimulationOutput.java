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

import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.model.MachineComponent;

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
					outfile.write("\t" + mc.getName() + "-Input-" + (i+1));
				}
				for (int i=0; i<mc.getComponent().getOutputs().size(); i++) {
					outfile.write("\t" + mc.getName() + "-Output-" + (i+1));
				}
			}
			for(ASimulationControl sc : simlist) {
				outfile.write("\t" + sc.getName() + "-Sim");
				outfile.write("\t" + sc.getName() + "-Sim");
			}
			outfile.write("\n");
			/* 2nd line: Time\tMcName1.InputName1\tMcName1.InputName2\tMcName1.OutputName1\t...*/
			outfile.write("   ");
			for(MachineComponent mc : mclist) {
				for (IOContainer input : mc.getComponent().getInputs()) {
					outfile.write("\t" + mc.getName() + "." + input.getName());
				}
				for (IOContainer output : mc.getComponent().getOutputs()) {
					outfile.write("\t" + mc.getName() + "." + output.getName());
				}
			}
			for(ASimulationControl sc : simlist) {
				outfile.write("\t" + sc.getOutput().getName() + " State");
				outfile.write("\t" + sc.getOutput().getName() + " Value");
			}
			outfile.write("\n");
			/* 3rd line: [s]\t[WATT]\t[TEMP]\t...*/
			outfile.write("[s]");
			for(MachineComponent mc : mclist) {
				for (IOContainer input : mc.getComponent().getInputs()) {
					outfile.write("\t" + "[" + input.getUnit() + "]");
				}
				for (IOContainer output : mc.getComponent().getOutputs()) {
					outfile.write("\t" + "[" + output.getUnit() + "]");
				}
			}
			for(ASimulationControl sc : simlist) {
				outfile.write("\t" + "       ");
				outfile.write("\t" + "[" + sc.getOutput().getUnit() + "]");
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
				for (IOContainer input : mc.getComponent().getInputs())
					outfile.write("\t" + input.getValue());
				for (IOContainer output : mc.getComponent().getOutputs())
					outfile.write("\t" + output.getValue());
			}
			for(ASimulationControl sc : simlist) {
				outfile.write("\t" + sc.getState().toString());
				outfile.write("\t" + sc.getOutput().getValue());
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
