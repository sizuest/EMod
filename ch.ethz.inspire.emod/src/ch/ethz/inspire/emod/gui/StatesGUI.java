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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.ethz.inspire.emod.States;
import ch.ethz.inspire.emod.gui.utils.ShowButtons;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.simulation.MachineState;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * GUI for state sequence editing
 * 
 * @author sizuest
 * 
 */
public class StatesGUI extends AConfigGUI {

	private Table tableStateSequence;

	// fields used in the Table for the State Sequences
	private String[] stateList;

	/**
	 * New state editor in the given parent
	 * @param parent
	 * @param style
	 */
	public StatesGUI(Composite parent, int style) {
		super(parent, style, ShowButtons.RESET | ShowButtons.OK, false);

		tableStateSequence = new Table(this.getContent(), SWT.BORDER
				| SWT.MULTI | SWT.V_SCROLL);
		tableStateSequence.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
		tableStateSequence.setLinesVisible(true);
		tableStateSequence.setHeaderVisible(true);

		String[] bTitles = {
				"    ",
				LocalizationHandler
						.getItem("app.gui.sim.machinestatesequence.time"),
				LocalizationHandler
						.getItem("app.gui.sim.machinestatesequence.duration"),
				LocalizationHandler
						.getItem("app.gui.sim.machinestatesequence.state"),
				"       ", "       " };
		for (int i = 0; i < bTitles.length; i++) {
			TableColumn column = new TableColumn(tableStateSequence, SWT.NULL);
			column.setText(bTitles[i]);
		}

		try {
			TableUtils.addCellEditor(tableStateSequence,
					AConfigGUI.class.getDeclaredMethod("wasEdited"), this,
					new int[] { 2 });
		} catch (Exception e) {
			e.printStackTrace();
		}

		// workaround for the combo to select the state
		stateList = new String[States.getStateCount()];
		for (int i = 0; i < States.getStateCount(); i++) {
			stateList[i] = States.getState(i).toString();
		}

		for (int i = 0; i < States.getStateCount(); i++) {
			addStateSequenceItem(i);
		}

	}

	private void addStateSequenceItem(int index) {
		tableStateSequence.setRedraw(false);

		// create new table item in the tableModelView
		final TableItem item = new TableItem(tableStateSequence, SWT.NONE,
				index);

		// create combo for drop-down selection of the state
		final CCombo comboEditState = new CCombo(tableStateSequence, SWT.PUSH);
		// create button to append a new state
		final Button buttonAddState = new Button(tableStateSequence, SWT.PUSH);
		// create button to delete the last state of the list
		final Button buttonDeleteState = new Button(tableStateSequence,
				SWT.PUSH);

		// write id, time, duration and state to table
		// first colum: id
		item.setText(0, String.valueOf(index));

		// second column: time (if first item, time = 0.00)
		if (index == 0) {
			item.setText(1, "0.00");
		} else {
			Double startTime = // States.getTime(index-1) +
								// States.getDuration(index-1);
			Double.parseDouble(tableStateSequence.getItem(index - 1).getText(1))
					+ Double.parseDouble(tableStateSequence.getItem(index - 1)
							.getText(2));
			item.setText(1, String.valueOf(startTime));
		}

		// third column: duration
		item.setText(2, States.getDuration(index).toString());
		// fourth column: state
		item.setText(3, States.getState(index).toString());

		// get the values for the drop-down combo
		String[] comboItems = new String[MachineState.values().length];
		for (MachineState ms : MachineState.values()) {
			comboItems[ms.ordinal()] = ms.name();
		}
		comboEditState.setItems(comboItems);

		// prefill the combo with the current state
		final int id = index;
		comboEditState.setText(States.getState(id).toString());
		comboEditState.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// set the selected value into the cell behind the combo
				item.setText(3, comboEditState.getText());
				wasEdited();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				// Not used
			}
		});

		// pack the combo and set it into the cell
		comboEditState.pack();
		final TableEditor editor = new TableEditor(tableStateSequence);
		editor.minimumWidth = comboEditState.getSize().x;
		int widthColumnFour = comboEditState.getSize().x;
		editor.grabHorizontal = true;
		editor.horizontalAlignment = SWT.LEFT;
		editor.setEditor(comboEditState, item, 3);

		// the last entry of the list has a delete/add button
		if (index == States.getStateCount() - 1) {

			// create button to add a new row
			Image imageAdd = new Image(Display.getDefault(),
					"src/resources/Add16.gif");
			buttonAddState.setImage(imageAdd);
			buttonAddState.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					// append a state with duration 0 and state OFF
					States.appendState(0, MachineState.OFF);
					// get rid of the buttons, refresh table
					buttonAddState.dispose();
					buttonDeleteState.dispose();
					update();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
					// Not used
				}
			});
			// pack the button and set it into the cell
			buttonAddState.pack();
			TableEditor editor2 = new TableEditor(tableStateSequence);
			editor2.minimumWidth = buttonAddState.getSize().x;
			editor2.horizontalAlignment = SWT.LEFT;
			editor2.setEditor(buttonAddState, item, 5);

			// create button to delete last row
			Image imageDelete = new Image(Display.getDefault(),
					"src/resources/Delete16.gif");
			buttonDeleteState.setImage(imageDelete);
			buttonDeleteState.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					// delete the cell, remove the state from the statesList,
					// refresh table
					item.dispose();
					States.getStateMap().remove(id);
					update();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
					// Not used
				}
			});
			// pack the button and set it into the cell
			buttonDeleteState.pack();
			TableEditor editor3 = new TableEditor(tableStateSequence);
			editor3.minimumWidth = buttonDeleteState.getSize().x;
			editor3.horizontalAlignment = SWT.LEFT;
			editor3.setEditor(buttonDeleteState, item, 4);
		}

		TableColumn[] columns = tableStateSequence.getColumns();
		for (int i = 0; i < columns.length; i++) {
			columns[i].pack();
		}
		tableStateSequence.getColumn(3).setWidth(widthColumnFour);

		// if a cell gets deleted, make shure that the combo and buttons get
		// deleted too!
		item.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				comboEditState.dispose();
				buttonAddState.dispose();
				buttonDeleteState.dispose();
			}
		});
		tableStateSequence.setRedraw(true);
	}

	@Override
	public void reset() {
		// Load state Data
		States.readStates(PropertiesHandler.getProperty("sim.MachineName"),
				PropertiesHandler.getProperty("sim.SimulationConfigName"));

		update();
	}

	@Override
	public void update() {
		// delete the content of the table
		tableStateSequence.setRedraw(false);
		tableStateSequence.clearAll();
		tableStateSequence.setItemCount(0);

		// fill the table with the values form States
		for (int i = 0; i < States.getStateCount(); i++) {
			addStateSequenceItem(i);
		}

		// show new Table
		tableStateSequence.setRedraw(true);
		getContent().redraw();
		redraw();
	}

	@Override
	public void save() {
		States.removeAllStates();

		for (TableItem ti : tableStateSequence.getItems())
			States.appendState(Double.valueOf(ti.getText(2)),
					MachineState.valueOf(ti.getText(3)));

		update();

		States.saveStates(PropertiesHandler.getProperty("sim.MachineName"),
				PropertiesHandler.getProperty("sim.SimulationConfigName"));
	}
}
