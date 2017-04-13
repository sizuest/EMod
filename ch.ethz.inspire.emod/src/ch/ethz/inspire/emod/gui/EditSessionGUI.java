/***********************************
 * $Id: EModStartupGUI.java 255 2017-03-18 12:01:34Z sizuest $
 *
 * $URL: https://icvrdevil.ethz.ch/svn/EMod/trunk/ch.ethz.inspire.emod/src/ch/ethz/inspire/emod/gui/EModStartupGUI.java $
 * $Author: sizuest $
 * $Date: 2017-03-18 13:01:34 +0100 (Sam, 18. Mär 2017) $
 * $Rev: 255 $
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.gui;

import java.io.File;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.gui.utils.ShowButtons;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * Selection of the machine to be displayed
 * 
 * @author sizuest
 *
 */
public class EditSessionGUI extends AConfigGUI {

	// Text to let the user change the MachineConfig
	private static Text textMachineName;
	// name of the MachineConfig from the last use of EMod from app.config
	private static String machineName;
	
	// Text to let the user add some comments
	private static Text textComment;

	// Combo to let the user select the SimConfig
	private static Combo comboMachineConfigName;

	private static Combo comboSimConfigName;

	private static Combo comboProcName;
	// name of the SimConfig from the last use of EMod from app.config
	private static String machineConfigName;

	private static String simConfigName;

	private static String procName;
	
	/**
	 * @param parent
	 * @param style
	 */
	public EditSessionGUI(Composite parent, int style){
		super(parent, style, ShowButtons.ALL, true);
		
		init();
	}
	
	/**
	 * Opens a new shell to edit the Emod Session
	 * 
	 * @param parent
	 * @return
	 */
	public static Shell openEditSessionGUI(final Shell parent){
		final Shell shell = new Shell(parent, SWT.TITLE | SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.MAX | SWT.RESIZE);
		shell.setText("Session properties");
		shell.setLayout(new GridLayout(1, true));
		shell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final EditSessionGUI gui = new EditSessionGUI(shell, SWT.NONE);
		
		shell.pack();

		shell.layout();
		shell.redraw();
		shell.open();

		shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		shell.addControlListener(new ControlListener() {

			@Override
			public void controlResized(ControlEvent e) {
				gui.layout();
			}

			@Override
			public void controlMoved(ControlEvent e) {
				gui.layout();
			}
		});

		shell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				parent.setEnabled(true);
			}
		});
		
		gui.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				shell.dispose();
			}
		});
		
		return shell;
	}

	/**
	 * window to load existing machine configuration
	 */
	protected void init() {
		
		PropertiesHandler.setProperty("app.MachineDataPathPrefix", "Machines");

		// Define content layout
		getContent().setLayout(new GridLayout(2, false));

		// get machineName and machineConfigName from app.config file
		machineName       = EModSession.getMachineName();
		machineConfigName = EModSession.getMachineConfig();
		simConfigName     = EModSession.getSimulationConfig();
		procName          = EModSession.getProcessName();

		// text load machine config
		Label textLoadMachConfig = new Label(getContent(), SWT.TRANSPARENT);
		textLoadMachConfig.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		textLoadMachConfig.setText(LocalizationHandler.getItem("app.gui.startup.machinename"));

		// combo for the user to select the desired MachConfig
		textMachineName = new Text(getContent(), SWT.BORDER);
		textMachineName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textMachineName.setText(machineName);

		// text load machine config
		Label textLoadMachineConfig = new Label(getContent(), SWT.TRANSPARENT);
		textLoadMachineConfig.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		textLoadMachineConfig.setText(LocalizationHandler.getItem("app.gui.startup.machineconfigname"));

		// combo for the user to select the desired SimConfig
		comboMachineConfigName = new Combo(getContent(), SWT.NONE);
		comboMachineConfigName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		// possible items of the combo are all SimConfig that match to the
		// selected MachConfig
		updatecomboMachineConfigName(machineName);
		// prefill the last used SimConfig as default value into the combo
		comboMachineConfigName.setText(machineConfigName);

		// text load simulation config
		Label textLoadSimConfig = new Label(getContent(), SWT.TRANSPARENT);
		textLoadSimConfig.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		textLoadSimConfig.setText(LocalizationHandler.getItem("app.gui.startup.simulationconfigname"));

		// combo for the user to select the desired SimConfig
		comboSimConfigName = new Combo(getContent(), SWT.NONE);
		comboSimConfigName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		// possible items of the combo are all SimConfig that match to the
		// selected MachConfig
		updatecomboSimConfigName(machineName);
		// prefill the last used SimConfig as default value into the combo
		comboSimConfigName.setText(simConfigName);

		// add selection listener to the combo
		comboSimConfigName.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// disable comboMachineConfigName to prevent argument null for
				// updatecomboMachineConfigName
				comboProcName.setEnabled(false);

				// get Text of chosen MachineConfig
				String stringMachConfig = textMachineName.getText();
				String stringSimConfig = comboSimConfigName.getText();
				// update comboMachineConfigName according to the Selection of
				// MachineConfig
				updatecomboProcName(stringMachConfig, stringSimConfig);

				// enable comboMachineConfigName after update
				comboProcName.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				// Not used
			}
		});

		// text load process config
		Label textLoadProcConfig = new Label(getContent(), SWT.TRANSPARENT);
		textLoadProcConfig.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		textLoadProcConfig.setText(LocalizationHandler.getItem("app.gui.startup.processconfigname"));

		// combo for the user to select the desired process
		comboProcName = new Combo(getContent(), SWT.NONE);
		comboProcName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		// possible items of the combo are all process that match to the
		// selected MachConfig and SimConfig
		updatecomboProcName(machineName, simConfigName);
		// prefill the last used process as default value into the combo
		comboProcName.setText(procName);
		
		// text comment
		Label textLoadComment = new Label(getContent(), SWT.TRANSPARENT);
		textLoadComment.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		textLoadComment.setText(LocalizationHandler.getItem("app.gui.startup.comment"));

		textComment = new Text(getContent(), SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		textComment.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		textComment.setText(EModSession.getNotes());
						
		getContent().pack();

	}

	/**
	 * update the comboMachineConfigName according to the selection of
	 * comboMachineName
	 * 
	 * @param stringMachConfig
	 *            update the selection of possible machine conifgurations
	 */
	protected static void updatecomboMachineConfigName(String stringMachConfig) {
		String path = PropertiesHandler
				.getProperty("app.MachineDataPathPrefix")
				+ "/"
				+ stringMachConfig + "/MachineConfig/";
		File subdir = new File(path);

		// check if subdirectory exists, then show possible configurations to
		// select
		if (subdir.exists()) {
			String[] subitems = subdir.list();
			Arrays.sort(subitems);
			comboMachineConfigName.setItems(subitems);
			if (1 == subitems.length)
				comboMachineConfigName.setText(subitems[0]);
			else
				comboMachineConfigName.setText(LocalizationHandler
						.getItem("app.gui.startup.selectmachineconfigname"));
		}
		// otherwise inform the user to create a new SimConfig
		else {
			comboMachineConfigName.removeAll();
			comboMachineConfigName.setText(LocalizationHandler
					.getItem("app.gui.startup.newmachineconfigname"));
		}
	}

	// update the comboSimConfigName according to the selection of
	// comboMachineName
	protected static void updatecomboSimConfigName(String stringMachConfig) {
		String path = PropertiesHandler
				.getProperty("app.MachineDataPathPrefix")
				+ "/"
				+ stringMachConfig + "/SimulationConfig/";
		File subdir = new File(path);

		// check if subdirectory exists, then show possible configurations to
		// select
		if (subdir.exists()) {
			String[] subitems = subdir.list();
			Arrays.sort(subitems);
			comboSimConfigName.setItems(subitems);
			if (1 == subitems.length)
				comboSimConfigName.setText(subitems[0]);
			else
				comboSimConfigName.setText(LocalizationHandler
						.getItem("app.gui.startup.selectmachineconfigname"));
		}
		// otherwise inform the user to create a new SimConfig
		else {
			comboSimConfigName.removeAll();
			comboSimConfigName.setText(LocalizationHandler
					.getItem("app.gui.startup.newmachineconfigname"));
		}
	}

	// update the comboProcName according to the selection of comboMachineName
	protected static void updatecomboProcName(String stringMachConfig,
			String stringSimConfig) {
		String path = PropertiesHandler
				.getProperty("app.MachineDataPathPrefix")
				+ "/"
				+ stringMachConfig + "/SimulationConfig/" + stringSimConfig;
		File files = new File(path);

		// check if subdirectory exists, then show possible configurations to
		// select
		if (files.exists()) {
			comboProcName.removeAll();
			for (File f : files.listFiles()) {
				if (f.getName().startsWith("process_")) {
					comboProcName.add(f.getName().substring(8,
							f.getName().length() - 4));
				}
			}
			

			if (1 == comboProcName.getItems().length)
				comboProcName.setText(comboProcName.getItems()[0]);
			else
				comboProcName.setText(LocalizationHandler
						.getItem("app.gui.startup.selectmachineconfigname"));
		}
		// otherwise inform the user to create a new SimConfig
		else {
			comboProcName.removeAll();
			comboProcName.setText(LocalizationHandler.getItem("app.gui.startup.newmachineconfigname"));
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.gui.AConfigGUI#save()
	 */
	@Override
	public void save() {
		String machine    = textMachineName.getText();
		String machConfig = comboMachineConfigName.getText();
		String simConfig  = comboSimConfigName.getText();
		String procName   = comboProcName.getText();				
		
		EModSession.setMachineName(machine);
		EModSession.setMachineConfig(machConfig);
		EModSession.setSimulationConfig(simConfig);
		EModSession.setProcessName(procName);
		
		EModSession.setNotes(textComment.getText());
		
		EModSession.save();
		
		EModStatusBarGUI.updateMachineInfo();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.inspire.emod.gui.AConfigGUI#reset()
	 */
	@Override
	public void reset() {
		textMachineName.setText(EModSession.getMachineName());
		comboMachineConfigName.setText(EModSession.getMachineConfig());
		comboSimConfigName.setText(EModSession.getSimulationConfig());
		comboProcName.setText(EModSession.getProcessName());
		
	}

}
