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

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.utils.ConfigReader;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * GUI to edit generic input control
 * @author sizuest
 *
 */
public class EditGenergicSimulationControlGUI extends AEditInputComposite {

	private Table tableInputProperties;
	protected ConfigReader input;

	/**
	 * @param parent
	 * @param style
	 * @param sc
	 */
	public EditGenergicSimulationControlGUI(Composite parent, int style,
			ASimulationControl sc) {
		super(parent, style, sc);

	}

	@Override
	public void init() {

		this.getContent().setLayout(new GridLayout(1, true));

		tableInputProperties = new Table(this.getContent(), SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		tableInputProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableInputProperties.setLinesVisible(true);
		tableInputProperties.setHeaderVisible(true);

		String[] titles = {
				LocalizationHandler.getItem("app.gui.compdb.property"),
				LocalizationHandler.getItem("app.gui.compdb.value") };
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(tableInputProperties, SWT.NULL);
			column.setText(titles[i]);
		}

		try {
			TableUtils.addCellEditor(tableInputProperties, this,
					new int[] { 1 });
		} catch (Exception e) {
			e.printStackTrace();
		}

		String path = EModSession.getRootPath()
				+ File.separator
				+ Defines.MACHINECONFIGDIR
				+ File.separator
				+ EModSession.getMachineConfig()
				+ File.separator
				+ sc.getType() + "_" + sc.getName() + ".xml";

		try {
			input = new ConfigReader(path);
		} catch (Exception e) {
			e.printStackTrace();
		}

		update();
	}

	@Override
	public void update() {
		tableInputProperties.setItemCount(0);

		for (String key : input.getKeys()) {
			TableItem item = new TableItem(tableInputProperties, SWT.NONE);
			item.setText(0, key);
			try {
				item.setText(1, input.getString(key));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		TableColumn[] columns = tableInputProperties.getColumns();
		for (int j = 0; j < columns.length; j++) {
			columns[j].pack();
		}

	}

	@Override
	public void save() {
		for (TableItem ti : tableInputProperties.getItems())
			input.setValue(ti.getText(0), ti.getText(1));

		try {
			input.saveValues();
		} catch (IOException e) {
			e.printStackTrace();
		}

		sc.readConfig();

	}

	@Override
	public void reset() {
		try {
			input = new ConfigReader(input.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

		update();
	}

}
