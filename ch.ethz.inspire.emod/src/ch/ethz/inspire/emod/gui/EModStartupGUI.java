package ch.ethz.inspire.emod.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

public class EModStartupGUI {
	private static Shell shell;
	
	//Combo to let the user select the MachineConfig
	private Combo comboMachineName;
	//name of the MachineConfig from the last use of EMod from app.config
	private String machineName;
	
	//Combo to let the user select the SimConfig
	private Combo comboMachineConfigName, comboSimConfigName, comboProcName;
	//name of the SimConfig from the last use of EMod from app.config
	private String machineConfigName, simConfigName, procName;
	
	//start the StartupGUI and initialize all needed content
	public EModStartupGUI(){

	}
	
 	/**
	 * window to load existing machine configuration
	 */
	protected void loadMachineGUI(){
		
		//new shell in style modal to prevent user from skipping window
		shell = new Shell(Display.getCurrent(),SWT.APPLICATION_MODAL);
		shell.setText("EMod startup");
		shell.setLayout(new GridLayout(2, true));
		
		//get machineName and machineConfigName from app.config file
		machineName = PropertiesHandler.getProperty("sim.MachineName");
		machineConfigName = PropertiesHandler.getProperty("sim.MachineConfigName");
		simConfigName     = PropertiesHandler.getProperty("sim.SimulationConfigName");
		procName          = PropertiesHandler.getProperty("sim.ProcessName");
		
		//text load machine config
		Text textLoadMachConfig = new Text(shell, SWT.READ_ONLY | SWT.LEFT);
		textLoadMachConfig.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textLoadMachConfig.setText(LocalizationHandler.getItem("app.gui.startup.machinename"));
	
		//combo for the user to select the desired MachConfig
		comboMachineName = new Combo(shell, SWT.NONE);
		comboMachineName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	
		//possible items of the combo are all Machines present in the folder Machines
		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/";
		File dir = new File(path);
		String[] items = dir.list();
		comboMachineName.setItems(items);
		
		//prefill the last used MachineConfig as default value into the combo
		comboMachineName.setText(machineName);
	
		//add selection listener to the combo
		//the possible selection of the comboMachineConfigName has to change according to the selected value
		comboMachineName.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent event){
				//disable comboMachineConfigName to prevent argument null for updatecomboMachineConfigName
				comboMachineConfigName.setEnabled(false);
				comboSimConfigName.setEnabled(false);
				comboProcName.setEnabled(false);
    		
				//get Text of chosen MachineConfig
    			String stringMachConfig = comboMachineName.getText();
    			String stringSimConfig  = comboSimConfigName.getText();
    			//update comboMachineConfigName according to the Selection of MachineConfig
    			updatecomboMachineConfigName(stringMachConfig);
    			updatecomboSimConfigName(stringMachConfig);
    			updatecomboProcName(stringMachConfig, stringSimConfig);
    		
    			//enable comboMachineConfigName after update
    			comboMachineConfigName.setEnabled(true);
				comboSimConfigName.setEnabled(true);
				comboProcName.setEnabled(true);
    		}
    		public void widgetDefaultSelected(SelectionEvent event){
    		
    		}
    	});

		//text load machine config
		Text textLoadMachineConfig = new Text(shell, SWT.READ_ONLY | SWT.LEFT);
		textLoadMachineConfig.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textLoadMachineConfig.setText(LocalizationHandler.getItem("app.gui.startup.machineconfigname"));

		//combo for the user to select the desired SimConfig
		comboMachineConfigName = new Combo(shell, SWT.NONE);
		comboMachineConfigName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//possible items of the combo are all SimConfig that match to the selected MachConfig
		updatecomboMachineConfigName(machineName);
		//prefill the last used SimConfig as default value into the combo
		comboMachineConfigName.setText(machineConfigName);
		
		//text load simulation config
		Text textLoadSimConfig = new Text(shell, SWT.READ_ONLY | SWT.LEFT);
		textLoadSimConfig.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textLoadSimConfig.setText(LocalizationHandler.getItem("app.gui.startup.simulationconfigname"));

		//combo for the user to select the desired SimConfig
		comboSimConfigName = new Combo(shell, SWT.NONE);
		comboSimConfigName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//possible items of the combo are all SimConfig that match to the selected MachConfig
		updatecomboSimConfigName(machineName);
		//prefill the last used SimConfig as default value into the combo
		comboSimConfigName.setText(simConfigName);
		
		//add selection listener to the combo
		comboSimConfigName.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent event){
				//disable comboMachineConfigName to prevent argument null for updatecomboMachineConfigName
				comboProcName.setEnabled(false);
    		
				//get Text of chosen MachineConfig
    			String stringMachConfig = comboMachineName.getText();
    			String stringSimConfig  = comboSimConfigName.getText();
    			//update comboMachineConfigName according to the Selection of MachineConfig
    			updatecomboProcName(stringMachConfig, stringSimConfig);
    		
    			//enable comboMachineConfigName after update
    			comboProcName.setEnabled(true);
    		}
    		public void widgetDefaultSelected(SelectionEvent event){
    		
    		}
		});
		
		//text load process config
		Text textLoadProcConfig = new Text(shell, SWT.READ_ONLY | SWT.LEFT);
		textLoadProcConfig.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textLoadProcConfig.setText(LocalizationHandler.getItem("app.gui.startup.processconfigname"));

		//combo for the user to select the desired process
		comboProcName = new Combo(shell, SWT.NONE);
		comboProcName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		//possible items of the combo are all process that match to the selected MachConfig and SimConfig
		updatecomboProcName(machineName, simConfigName);
		//prefill the last used process as default value into the combo
		comboProcName.setText(procName);

		//Button to continue (exit the window, load the selected configuration)
		Button buttonContinue = new Button(shell, SWT.NONE);
		buttonContinue.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false, 2, 1));
		buttonContinue.setText(LocalizationHandler.getItem("app.gui.continue"));
    	buttonContinue.addSelectionListener(new SelectionListener(){
    	public void widgetSelected(SelectionEvent event){
    			//get the values for the Machine Name and the Machine Config Name
				String machine    = comboMachineName.getText();
				String machConfig = comboMachineConfigName.getText();
				String simConfig  = comboSimConfigName.getText();
				String procName   = comboProcName.getText();

				//check if a machine config was selected, otherwise stop
				if(machConfig.equals(LocalizationHandler.getItem("app.gui.startup.selectmachineconfigname"))){
					System.out.println("------******------ keine Maschkonfig ausgew�hlt");
					return;
				}
				
				//set the machinename and conifgname to app.config, to restore at next run
				PropertiesHandler.setProperty("sim.MachineName", machine);
				PropertiesHandler.setProperty("sim.MachineConfigName", machConfig);
				PropertiesHandler.setProperty("sim.SimulationConfigName", simConfig);
				PropertiesHandler.setProperty("sim.ProcessName", procName);
				
				//build machine and add components to the table of the model gui tab
				loadMachineModelConfig(machine, machConfig);
				
				
				shell.close();
			}
    		public void widgetDefaultSelected(SelectionEvent event){
    		
    		}
    	});
	
    	//Button to create a new machine
    	Button buttonNew = new Button(shell, SWT.NONE);
		buttonNew.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false, 2, 1));
		buttonNew.setText(LocalizationHandler.getItem("app.gui.startup.newmachine"));
    	buttonNew.addSelectionListener(new SelectionListener(){
    	public void widgetSelected(SelectionEvent event){
    			//open new window to input the desired machine name and config name
    			createNewMachineGUI();
    		
    			shell.close();
    			System.out.println("New Simulation started");

    		}
    		public void widgetDefaultSelected(SelectionEvent event){
   		
    		}
    	});
    	
		shell.pack();
	
		Rectangle rect = shell.getBounds();
		
		//width and height of the shell
		int[] size = {0, 0};
		size[0] = rect.width;
		size[1] = rect.height;
	
		//position the shell into the middle of the last window
    	int[] position;
    	position = EModGUI.shellPosition();
    	shell.setLocation(position[0]-size[0]/2, position[1]-size[1]/2);
	
    	//open the new shell
		shell.open();
	}
	
 	/**
	 * window to create a new machine configuration
	 */
	protected void createNewMachineGUI() {
		final Shell shellCreateMachine = new Shell(Display.getCurrent(),SWT.APPLICATION_MODAL);
		shellCreateMachine.setText("EMod startup");
		shellCreateMachine.setLayout(new GridLayout(2, true));
		
		//text load machine config
		Text textMachineName = new Text(shellCreateMachine, SWT.READ_ONLY | SWT.LEFT);
		textMachineName.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		textMachineName.setText(LocalizationHandler.getItem("app.gui.startup.newmachinename"));
	
		//text for the user to enter the desired Machine Name
		final Text textMachineNameValue = new Text(shellCreateMachine, SWT.NONE);
		textMachineNameValue.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		
		//text load machine config
		Text textMachineConfigName = new Text(shellCreateMachine, SWT.READ_ONLY | SWT.LEFT);
		textMachineConfigName.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		textMachineConfigName.setText(LocalizationHandler.getItem("app.gui.startup.newmachineconfigname"));
	
		//text for the user to enter the desired Machine Name
		final Text textMachineConfigNameValue = new Text(shellCreateMachine, SWT.NONE);
		textMachineConfigNameValue.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		
		//Button to continue (exit the window, load the selected configuration)
		Button buttonContinue = new Button(shellCreateMachine, SWT.NONE);
		GridData gridData = new GridData(GridData.END, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;
		buttonContinue.setLayoutData(gridData);
		buttonContinue.setText(LocalizationHandler.getItem("app.gui.continue"));
    	buttonContinue.addSelectionListener(new SelectionListener(){
    	public void widgetSelected(SelectionEvent event){
				//get the values for the Machine Name and the Machine Config Name
				String machine = textMachineNameValue.getText();
				String config = textMachineConfigNameValue.getText();

				//create the new machine
				createNewMachine(machine, config);
				
				//close the shell
				shellCreateMachine.close();
    		}
    		public void widgetDefaultSelected(SelectionEvent event){
    		
    		}
    	});
		
		shellCreateMachine.pack();
		
		Rectangle rect = shellCreateMachine.getBounds();
		
		//width and height of the shell
		int[] size = {0, 0};
		size[0] = rect.width;
		size[1] = rect.height;
	
		//position the shell into the middle of the last window
    	int[] position;
    	position = EModGUI.shellPosition();
    	shellCreateMachine.setLocation(position[0]-size[0]/2, position[1]-size[1]/2);
	
    	//open the new shell
    	shellCreateMachine.open();
	}

 	/**
	 * method to create files and folders of a new machine
	 * @param machine	name of the machine to add
	 * @param config	name of the machine conifguration to add
	 */
	protected void createNewMachine(String machine, String config) {
		//clear machine and table
		ModelGUI.clearTable();		
		
		//create the according folders and files (machine.xml, iolinking.txt)
		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/";
		File machinexml = new File(path + machine + "/MachineConfig/" + config + "/Machine.xml");
		File iolinking = new File(path + machine + "/MachineConfig/" + config + "/IOLinking.txt");
		try {
			machinexml.getParentFile().mkdirs();
			machinexml.createNewFile();
			iolinking.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		//set machinename and machinconfigname to app.config file to restore at next run
		PropertiesHandler.setProperty("sim.MachineName", machine);
		PropertiesHandler.setProperty("sim.MachineConfigName", config);		
	}

 	/**
	 * update the comboMachineConfigName according to the selection of comboMachineName
	 * @param stringMachConfig	update the selection of possible machine conifgurations
	 */
    protected void updatecomboMachineConfigName(String stringMachConfig){
    	String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" + stringMachConfig + "/MachineConfig/";
    	File subdir = new File(path);
    	
    	//check if subdirectory exists, then show possible configurations to select
    	if(subdir.exists()){
    		String[] subitems = subdir.list();
        	comboMachineConfigName.setItems(subitems);
        	comboMachineConfigName.setText(LocalizationHandler.getItem("app.gui.startup.selectmachineconfigname"));
    	}
    	//otherwise inform the user to create a new SimConfig
    	else{
    		comboMachineConfigName.removeAll();
    		comboMachineConfigName.setText(LocalizationHandler.getItem("app.gui.startup.newmachineconfigname"));
    	}
	}
    
   //update the comboSimConfigName according to the selection of comboMachineName
    protected void updatecomboSimConfigName(String stringMachConfig){
    	String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" + stringMachConfig + "/SimulationConfig/";
    	File subdir = new File(path);
    	
    	//check if subdirectory exists, then show possible configurations to select
    	if(subdir.exists()){
    		String[] subitems = subdir.list();
        	comboSimConfigName.setItems(subitems);
        	comboSimConfigName.setText(LocalizationHandler.getItem("app.gui.startup.selectmachineconfigname"));
    	}
    	//otherwise inform the user to create a new SimConfig
    	else{
    		comboSimConfigName.removeAll();
    		comboSimConfigName.setText(LocalizationHandler.getItem("app.gui.startup.newmachineconfigname"));
    	}
	}
    
    //update the comboProcName according to the selection of comboMachineName
    protected void updatecomboProcName(String stringMachConfig, String stringSimConfig){
    	String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" + stringMachConfig + "/SimulationConfig/" + stringSimConfig;
    	File files = new File(path);
    	
    	//check if subdirectory exists, then show possible configurations to select
    	if(files.exists()){
    		comboProcName.removeAll();
    		for(File f : files.listFiles()){
    			if(f.getName().startsWith("process_")){
    				comboProcName.add(f.getName().substring(8, f.getName().length()-4));
    			}
    		}
    		comboProcName.setText(LocalizationHandler.getItem("app.gui.startup.selectmachineconfigname"));
    	}
    	//otherwise inform the user to create a new SimConfig
    	else{
    		comboProcName.removeAll();
    		comboProcName.setText(LocalizationHandler.getItem("app.gui.startup.newmachineconfigname"));
    	}
	}

 	/**
	 * load a machine configuration from DB and write it to the machine table
	 * @param machine	name of the machine to load
	 * @param config	name of the configuration to load
	 */   
    public void loadMachineModelConfig(String machine, String config){
		// Build machine: Read and check machine configuration
		Machine.buildMachine(machine, config);
		ArrayList<MachineComponent> mclist = Machine.getInstance().getMachineComponentList();

		//add the components to the table in the model gui tab
		int i = 0;
		for(MachineComponent mc:mclist){
			ModelGUI.addTableItem(mc, i);
			i++;
		}
    }
}
