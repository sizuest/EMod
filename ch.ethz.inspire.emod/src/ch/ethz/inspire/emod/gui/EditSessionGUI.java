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


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.gui.utils.ShowButtons;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

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
		
		// PropertiesHandler.setProperty("app.MachineDataPathPrefix", "Machines");

		// Define content layout
		getContent().setLayout(new GridLayout(3, false));

		// get machineName and machineConfigName from app.config file
		machineName       = EModSession.getMachineName();
		machineConfigName = EModSession.getMachineConfig();
		simConfigName     = EModSession.getSimulationConfig();
		procName          = EModSession.getProcessName();

		// text load machine config
		Label textLoadMachConfig = new Label(getContent(), SWT.TRANSPARENT);
		textLoadMachConfig.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		textLoadMachConfig.setText(LocalizationHandler.getItem("app.gui.session.machinename"));

		// combo for the user to select the desired MachConfig
		textMachineName = new Text(getContent(), SWT.BORDER);
		textMachineName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		textMachineName.setText(machineName);

		// text load machine config
		Label textLoadMachineConfig = new Label(getContent(), SWT.TRANSPARENT);
		textLoadMachineConfig.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		textLoadMachineConfig.setText(LocalizationHandler.getItem("app.gui.session.machineconfigname"));

		// combo for the user to select the desired SimConfig
		comboMachineConfigName = new Combo(getContent(), SWT.NONE);
		comboMachineConfigName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		// possible items of the combo are all SimConfig that match to the
		// selected MachConfig
		updatecomboMachineConfigName();
		// prefill the last used SimConfig as default value into the combo
		comboMachineConfigName.setText(machineConfigName);
		
		Image imageDelete = new Image(Display.getDefault(), "src/resources/Delete16.gif");
		Button buttonDelete = new Button(getContent(), SWT.PUSH);
		buttonDelete.setImage(imageDelete);
		buttonDelete.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		buttonDelete.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				EModSession.removeMachineConfig(comboMachineConfigName.getText());
				updatecomboMachineConfigName();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		// text load simulation config
		Label textLoadSimConfig = new Label(getContent(), SWT.TRANSPARENT);
		textLoadSimConfig.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		textLoadSimConfig.setText(LocalizationHandler.getItem("app.gui.session.simulationconfigname"));

		// combo for the user to select the desired SimConfig
		comboSimConfigName = new Combo(getContent(), SWT.NONE);
		comboSimConfigName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		// prefill the last used SimConfig as default value into the combo
		comboSimConfigName.setText(simConfigName);

		// add selection listener to the combo
		comboSimConfigName.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// disable comboMachineConfigName to prevent argument null for
				// updatecomboMachineConfigName
				comboProcName.setEnabled(false);

				String stringSimConfig = comboSimConfigName.getText();
				// update comboMachineConfigName according to the Selection of
				// MachineConfig
				updatecomboProcName(stringSimConfig);

				// enable comboMachineConfigName after update
				comboProcName.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				// Not used
			}
		});
		
		buttonDelete = new Button(getContent(), SWT.PUSH);
		buttonDelete.setImage(imageDelete);
		buttonDelete.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		buttonDelete.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				EModSession.removeSimulationConfig(comboSimConfigName.getText());
				updatecomboSimConfigName();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		// text load process config
		Label textLoadProcConfig = new Label(getContent(), SWT.TRANSPARENT);
		textLoadProcConfig.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		textLoadProcConfig.setText(LocalizationHandler.getItem("app.gui.session.processconfigname"));

		// combo for the user to select the desired process
		comboProcName = new Combo(getContent(), SWT.NONE);
		comboProcName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		// prefill the last used process as default value into the combo
		comboProcName.setText(procName);
		
		buttonDelete = new Button(getContent(), SWT.PUSH);
		buttonDelete.setImage(imageDelete);
		buttonDelete.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		buttonDelete.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				EModSession.removeProcess(comboProcName.getText());
				updatecomboProcName(comboSimConfigName.getText());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		// text comment
		Label textLoadComment = new Label(getContent(), SWT.TRANSPARENT);
		textLoadComment.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		textLoadComment.setText(LocalizationHandler.getItem("app.gui.session.comment"));

		textComment = new Text(getContent(), SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		textComment.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		textComment.setText(EModSession.getNotes());
		textComment.setEditable(false);
						
		getContent().pack();
		
		updatecomboSimConfigName();

	}

	/**
	 * update the comboMachineConfigName according to the selection of
	 * comboMachineName
	 * 
	 * @param stringMachConfig
	 *            update the selection of possible machine conifgurations
	 */
	protected static void updatecomboMachineConfigName() {
		String[] items = EModSession.getMachineConfigs();
				
		comboMachineConfigName.setItems(items);
		comboMachineConfigName.select(0);
		
		for(int i=1; i<items.length; i++)
			if(items[i].equals(EModSession.getMachineConfig())){
				comboMachineConfigName.select(i);
				break;
			}
	}

	// update the comboSimConfigName according to the selection of
	// comboMachineName
	protected static void updatecomboSimConfigName() {
		String[] items = EModSession.getSimulationConfigs();
		
		comboSimConfigName.setItems(items);
		comboSimConfigName.select(0);
		
		for(int i=1; i<items.length; i++)
			if(items[i].equals(EModSession.getSimulationConfig())){
				comboSimConfigName.select(i);
				break;
			}
		
		updatecomboProcName(comboSimConfigName.getText());
	}

	// update the comboProcName according to the selection of comboMachineName
	protected static void updatecomboProcName(String stringSimConfig) {
		String[] items = EModSession.getProcessNames(stringSimConfig);
		
		comboProcName.setItems(items);
		comboProcName.select(0);
		
		for(int i=1; i<items.length; i++)
			if(items[i].equals(EModSession.getProcessName())){
				comboProcName.select(i);
				break;
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
