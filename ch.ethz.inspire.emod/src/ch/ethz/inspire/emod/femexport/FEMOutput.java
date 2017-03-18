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

package ch.ethz.inspire.emod.femexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.simulation.MachineState;

/**
 * 
 * @author andreas
 * 
 */
public class FEMOutput {

	private BufferedWriter outfile;
	private ArrayList<MachineComponent> mclist;

	private int flushcnt;
	private DecimalFormat format;

	private String separator = "\t";

	/**
	 * Create data logging file. Write file header.
	 * 
	 * @param filename
	 *            Name of file to write to simulation output to.
	 * @param list
	 *            List of model components.
	 */
	public FEMOutput(String filename, ArrayList<MachineComponent> list) {
		/* Init variables: */
		flushcnt = 0;
		mclist = list;
		format = new DecimalFormat("####0.00");

		try {
			/* Create output file: */
			File file = new File(filename);
			file.getParentFile().mkdirs();
			outfile = new BufferedWriter(new FileWriter(file));

			/* ****** Make file header: ******* */
			/* 1st Line: Time\tMcName1 BC 1\tMcName1 BC 2\t... */
			outfile.write("Time");
			outfile.write(separator+"State");
			for (MachineComponent mc : mclist) {
				for (int i = 0; i < mc.getComponent().getBoundaryConditions()
						.size(); i++) {
					outfile.write(separator + mc.getName() + "-BC-" + (i + 1));
				}
			}
			outfile.write("\n");
			/* 2nd line: Time\tMcName1.BCName1\tMcName1.BCName2\t... */
			outfile.write("   ");
			outfile.write(separator);
			for (MachineComponent mc : mclist) {
				for (BoundaryCondition bc : mc.getComponent()
						.getBoundaryConditions()) {
					outfile.write(separator + mc.getName() + "." + bc.getName());
				}
			}
			outfile.write("\n");
			/* 3rd line: [s]\t[WATT]\t[TEMP]\t... */
			outfile.write("[s]");
			outfile.write(separator);
			for (MachineComponent mc : mclist) {
				for (BoundaryCondition bc : mc.getComponent()
						.getBoundaryConditions()) {
					outfile.write(separator + "[" + bc.getUnit().toString()
							+ "]");
				}
			}
			outfile.write("\n");
			/* 4th line: */
			outfile.write("-");
			for (MachineComponent mc : mclist) {
				for (BoundaryCondition bc : mc.getComponent()
						.getBoundaryConditions()) {
					outfile.write(separator + bc.getType().toString());
				}
			}
			outfile.write("\n");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write samples to data logger.
	 * 
	 * @param time
	 *            Actual time in [s].
	 * @param state 
	 */
	public void logData(double time, MachineState state) {
		try {
			outfile.write(format.format(time));
			outfile.write(separator+state);
			for (MachineComponent mc : mclist) {
				for (BoundaryCondition bc : mc.getComponent()
						.getBoundaryConditions()) {
					outfile.write(separator + bc.getValue());
				}
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
	public void close() {
		try {
			outfile.flush();
			outfile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
