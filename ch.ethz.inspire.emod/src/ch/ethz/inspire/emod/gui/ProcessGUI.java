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
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.Process;
import ch.ethz.inspire.emod.gui.utils.ShowButtons;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * GUI for process editing
 * 
 * @author sizuest
 * 
 */

public class ProcessGUI extends AConfigGUI {

	//private ProcessManageGUI processManageGUI;
	private Table tableProcessParam;
	ArrayList<String> scNames;
	ArrayList<SiUnit> scUnits;
	private boolean updatethreadRunning = false;

	/**
	 * ProcessGUI
	 * @param parent
	 * @param style
	 */
	public ProcessGUI(Composite parent, int style) {
		super(parent, style, ShowButtons.RESET | ShowButtons.OK, false);

		Process.loadProcess(EModSession.getProcessName());
		
		// Auswahl Prozess
		//processManageGUI = new ProcessManageGUI(this, SWT.NONE);

		// Tabelle fuer Prozess initieren
		tableProcessParam = new Table(this.getContent(), SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		tableProcessParam.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableProcessParam.setLinesVisible(true);
		tableProcessParam.setHeaderVisible(true);

		try {
			TableUtils.addCellEditor(tableProcessParam, this, null);
			TableUtils.addCopyToClipboard(tableProcessParam);
			TableUtils.addPastFromClipboard(tableProcessParam, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update() {
		tableProcessParam.setEnabled(false);
		tableProcessParam.clearAll();
		tableProcessParam.setItemCount(0);
		//processManageGUI.update();

		threadedUpdate();
	}

	@Override
	public void save() {
		// write back data

		// Remove all entries with non numeric time
		for (int i = tableProcessParam.getItemCount() - 1; i >= 0; i--)
			try {
				Double.valueOf(tableProcessParam.getItem(i).getText(0));
			} catch (Exception e) {
				tableProcessParam.remove(i);
			}

		// Sort time vector
		// TODO

		// write back time
		double[] time = new double[tableProcessParam.getItemCount()];
		for (int j = 0; j < time.length; j++) {
			time[j] = Double.valueOf(tableProcessParam.getItem(j).getText(0));
		}

		try {
			Process.setTimeVector(time);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Write back values
		double[] data = new double[tableProcessParam.getItemCount()];
		for (int i = 0; i < scNames.size(); i++) {
			for (int j = 0; j < data.length; j++) {
				try {
					data[j] = Double.valueOf(tableProcessParam.getItem(j)
							.getText(i + 1));
				} catch (Exception e) {
					data[j] = 0;
				}
			}

			try {
				Process.setProcessVariable(scNames.get(i), data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			Process.getInstance().saveValues();
		} catch (IOException e) {
			e.printStackTrace();
		}

		update();
	}
	
	@Override
	public void wasEdited(){
		super.wasEdited();
		
		addEmptyLine();
	}
	
	/**
	 * Checks if the last line is empty, if not, an empty
	 * line is added
	 */
	private void addEmptyLine(){
		
		if(0>=tableProcessParam.getItemCount()){
			new TableItem(tableProcessParam, SWT.NONE);
			return;
		}
		
		TableItem lastItem = tableProcessParam.getItem(tableProcessParam.getItemCount()-1);
		
		for(int i=0; i<tableProcessParam.getColumnCount(); i++){
			if(!lastItem.getText().equals("")){
				new TableItem(tableProcessParam, SWT.NONE);
				return;
			}
		}
	}

	@Override
	public void reset() {
		Process.loadProcess(EModSession.getProcessName());
		update();
	}

	private void threadedUpdate() {
		if (updatethreadRunning)
			return;

		updatethreadRunning = true;

		Thread updateThread = new Thread() {
			@Override
			public void run() {
				getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {

						TableItem[] item;

						for (TableColumn tc : tableProcessParam.getColumns())
							tc.dispose();

						TableColumn column = new TableColumn(tableProcessParam, SWT.NULL);
						column.setText(LocalizationHandler.getItem("app.gui.sim.inputs.time")+" [s]");

						/*
						 * Fill the table We have two sources - Process file:
						 * Keys and values exist - New Simulators: Keys and
						 * values must be added
						 */

						// Process File

						// Simulators
						scNames = new ArrayList<String>();
						scUnits = new ArrayList<SiUnit>();
						for (ASimulationControl sc : Machine.getInstance().getVariableInputObjectList()) {
							scNames.add(sc.getName());
							scUnits.add(sc.getUnit());
							if (!(Process.getVariableNames().contains(sc.getName())))
								try {
									Process.addProcessVariable(sc.getName());
								} catch (Exception e) {
									System.out.print("Added new variable to process file: " + sc.getName());
								}
						}

						// Write Heads
						for (String s : scNames) {
							column = new TableColumn(tableProcessParam, SWT.NULL);
							column.setText(s + " [" + scUnits.get(scNames.indexOf(s)).toString() + "]");
						}

						// Table items
						item = new TableItem[Process.getNumberOfTimeStamps() + 1];

						// Time Data
						for (int i = 0; i < Process.getNumberOfTimeStamps(); i++) {
							item[i] = new TableItem(tableProcessParam, SWT.NONE);
							item[i].setText(0, Double.toString(Process.getTime()[i]));
						}

						// Variable Data
						for (int j = 0; j < scNames.size(); j++) {
							double[] tmp = Process.getProcessVariable(scNames.get(j));
							for (int i = 0; i < Process.getNumberOfTimeStamps(); i++) {
								if (i < tmp.length)
									item[i].setText(j + 1, Double.toString(tmp[i]));
								else
									item[i].setText(j + 1, "0");
							}

						}

						// Add empty entry
						addEmptyLine();

						EModStatusBarGUI.getProgressBar().setText( "Loading process file ...");
						EModStatusBarGUI.getProgressBar().updateProgressbar(0, false);

						// Tabelle packen
						TableColumn[] columns = tableProcessParam.getColumns();
						for (int i = 0; i < columns.length; i++) {
							columns[i].pack();
							EModStatusBarGUI.getProgressBar()
									.updateProgressbar(
											i * 100 / columns.length, false);
						}

						EModStatusBarGUI.getProgressBar().reset();

						tableProcessParam.setEnabled(true);

						updatethreadRunning = false;
					}
				});
			}
		};
		// background thread
		updateThread.setDaemon(true);
		updateThread.start();
	}
}
