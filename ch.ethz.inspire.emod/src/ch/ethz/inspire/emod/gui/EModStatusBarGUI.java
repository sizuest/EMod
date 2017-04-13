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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.gui.utils.ProgressbarGUI;
import ch.ethz.inspire.emod.gui.utils.ConfigStatusGUI;

/**
 * Status bar for the EModGUI
 * Displays 
 * - the current model loaded
 * - the configuration status
 * - a progress bar (optional)
 * 
 * IMPORTANT: Only one status bar is allowed!
 * @author sizuest
 *
 */
public class EModStatusBarGUI {

	private Label labelMachine, labelMachineCfg, labelSimulationCgf,
			labelProcessName;
	private StatusBarContainer cMachine, cStatus;
	private Composite container;

	private ConfigStatusGUI configStatus;
	private ProgressbarGUI progress;

	private static EModStatusBarGUI instance = null;

	/**
	 * Create the status bar for the stated object
	 * 
	 * @param parent
	 */
	public static void create(Composite parent) {
		if (instance != null)
			instance.dispose();

		instance = new EModStatusBarGUI(parent);

		try {
			init(parent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void dispose() {
		try {
			getInstance().container.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private EModStatusBarGUI(Composite parent) {/* Not used */}

	/**
	 * Returns the current instance
	 * @return
	 * @throws Exception
	 */
	public static EModStatusBarGUI getInstance() throws Exception {
		if (instance == null)
			throw new Exception("StatusBarGUI: Access before initialization");

		return instance;
	}

	private static void init(Composite parent) throws Exception {

		getInstance().container = new Composite(parent, SWT.NO_TRIM);
		getInstance().container.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 1, 1));
		getInstance().container.setLayout(new GridLayout(3, false));

		/* Containers for different sections */
		getInstance().cMachine = new StatusBarContainer(
				getInstance().container, SWT.NONE);
		getInstance().cMachine.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1));

		getInstance().cStatus = new StatusBarContainer(getInstance().container,
				SWT.NONE);
		getInstance().cStatus.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1));

		/* Machine and config names */
		getInstance().labelMachine = new Label(getInstance().cMachine,
				SWT.TRANSPARENT | SWT.NONE);
		getInstance().labelMachine.setLayoutData(new RowData());
		getInstance().labelMachineCfg = new Label(getInstance().cMachine,
				SWT.TRANSPARENT | SWT.NONE);
		getInstance().labelMachineCfg.setLayoutData(new RowData());
		getInstance().labelSimulationCgf = new Label(getInstance().cMachine,
				SWT.TRANSPARENT | SWT.NONE);
		getInstance().labelSimulationCgf.setLayoutData(new RowData());
		getInstance().labelProcessName = new Label(getInstance().cMachine,
				SWT.TRANSPARENT | SWT.NONE);
		getInstance().labelProcessName.setLayoutData(new RowData());

		FontData fontData = getInstance().labelMachine.getFont().getFontData()[0];
		Font font = new Font(
				getInstance().labelMachine.getDisplay(),
				new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD));
		getInstance().labelMachine.setFont(font);

		/* Status bar */
		getInstance().configStatus = new ConfigStatusGUI(getInstance().cStatus);

		/* Progress bar */
		getInstance().progress = new ProgressbarGUI(getInstance().container, "");
		getInstance().progress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));

		/* Update everything */
		updateMachineInfo();

		getInstance().container.layout();
	}

	/**
	 * Update the machine infomration
	 */
	public static void updateMachineInfo() {
		try {
			getInstance().container.update();

			/* Machine and config names */
			getInstance().labelMachine.setText(EModSession.getMachineName());
			getInstance().labelMachineCfg.setText("M: " + EModSession.getMachineConfig());
			getInstance().labelSimulationCgf.setText("S: " + EModSession.getSimulationConfig());
			getInstance().labelProcessName.setText("P: " + EModSession.getProcessName());

			// getInstance().container.layout();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return the ProgressBar object If the ProgressBar can not be fetched, a
	 * new ProgressBar will be created.
	 * 
	 * @return {@link ProgressbarGUI}
	 */
	public static ProgressbarGUI getProgressBar() {
		try {
			return getInstance().progress;
		} catch (Exception e) {
			return ProgressbarGUI.newProgressbarGUI("");
		}
	}

	/**
	 * Returns the ShowConfigStatus object If the object can't be fetched, a new
	 * one will be created
	 * 
	 * @return
	 */
	public static ConfigStatusGUI getConfigStatus() {
		try {
			return getInstance().configStatus;
		} catch (Exception e) {
			return ConfigStatusGUI.newConfigStatusDisplay();
		}
	}

}
