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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.eclipse.swt.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.simulation.EModSimulationRun;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;
import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.Machine;

/**
 * main gui class for emod application
 * 
 * @author dhampl
 *
 */
public class EModGUI {

	private static Logger logger = Logger.getLogger(EModGUI.class.getName());
	
	//TODO manick: had to turn Shell into static for EModGUI.shellPosition() to work, any problem?
	protected static Shell shell;
	protected Display disp;
	
	protected Composite model;
	protected Composite sim;
	protected Composite analysis;
		
	public EModGUI(Display display) {
		disp = display;
		shell = new Shell(display);
		
		shell.setText(LocalizationHandler.getItem("app.gui.title"));
		if(display.getBounds().width >= 1024)
			shell.setSize(1024, 768);
		else
			shell.setSize(display.getBounds().width, display.getBounds().height);
		
		shell.setLayout(new FillLayout());
		
		//init menu bar
		logger.log(LogLevel.DEBUG, "init menu");
		initMenu();
		
		//init tabs
		logger.log(LogLevel.DEBUG, "init tabs");
		initTabs();

		shell.open();
				
		//manick: Startup GUI for Settings of machine/sim confg
		EModStartupGUI startup =new EModStartupGUI();
		startup.loadMachineGUI();
		
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	
	/**
	 * Initializes the main menu bar.
	 */
	private void initMenu() {
		//create menu bar
		Menu menuBar = new Menu(shell, SWT.BAR);
		
		//create "File" tab and items
		MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText(LocalizationHandler.getItem("app.gui.menu.file"));
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);
			MenuItem fileNewItem = new MenuItem(fileMenu, SWT.PUSH);
			fileNewItem.setText(LocalizationHandler.getItem("app.gui.menu.file.new"));
			MenuItem fileOpenItem = new MenuItem(fileMenu, SWT.PUSH);
			fileOpenItem.setText(LocalizationHandler.getItem("app.gui.menu.file.open"));
			MenuItem fileSaveItem = new MenuItem(fileMenu, SWT.PUSH);
			fileSaveItem.setText(LocalizationHandler.getItem("app.gui.menu.file.save"));
			//MenuItem fileSaveAsItem = new MenuItem(fileMenu, SWT.PUSH);
			//fileSaveAsItem.setText(LocalizationHandler.getItem("app.gui.menu.file.saveas"));			
			MenuItem filePropertiesItem = new MenuItem(fileMenu, SWT.PUSH);
			filePropertiesItem.setText(LocalizationHandler.getItem("app.gui.menu.file.properties"));
			MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
			fileExitItem.setText(LocalizationHandler.getItem("app.gui.menu.file.exit"));
		
		//create "Database Components" tab and items
		MenuItem compDBMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		compDBMenuHeader.setText(LocalizationHandler.getItem("app.gui.menu.compDB"));
		Menu compDBMenu = new Menu(shell, SWT.DROP_DOWN);
		compDBMenuHeader.setMenu(compDBMenu);
			MenuItem compDBNewItem = new MenuItem(compDBMenu, SWT.PUSH);
			compDBNewItem.setText(LocalizationHandler.getItem("app.gui.menu.compDB.new"));
			MenuItem compDBOpenItem = new MenuItem(compDBMenu, SWT.PUSH);
			compDBOpenItem.setText(LocalizationHandler.getItem("app.gui.menu.compDB.open"));
			//MenuItem compDBImportItem = new MenuItem(compDBMenu, SWT.PUSH);
			//compDBImportItem.setText(LocalizationHandler.getItem("app.gui.menu.compDB.import"));
		
		//create "Help" tab and items
		MenuItem helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText(LocalizationHandler.getItem("app.gui.menu.help"));
		Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuHeader.setMenu(helpMenu);
			//MenuItem helpContentItem = new MenuItem(helpMenu, SWT.PUSH);
			//helpContentItem.setText(LocalizationHandler.getItem("app.gui.menu.help.content"));
			MenuItem helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
			helpAboutItem.setText(LocalizationHandler.getItem("app.gui.menu.help.about"));
		
		//add listeners
		fileNewItem.addSelectionListener(new fileNewItemListener());
		fileOpenItem.addSelectionListener(new fileOpenItemListener());
		fileSaveItem.addSelectionListener(new fileSaveItemListener());
		//fileSaveAsItem.addSelectionListener(new fileSaveAsItemListener());
		filePropertiesItem.addSelectionListener(new filePropertiesItemListener());
		fileExitItem.addSelectionListener(new fileExitItemListener());
		
		compDBNewItem.addSelectionListener(new compDBNewItemListener());
		compDBOpenItem.addSelectionListener(new compDBOpenItemListener());
		
		helpAboutItem.addSelectionListener(new helpAboutItemListener());
		
		
		
		
		shell.setMenuBar(menuBar);
	}
	
	private void initTabs(){
		//create the tab folder container
		final TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		
		//tab for machine model config
		final TabItem tabModelItem = new TabItem(tabFolder, SWT.NONE);
		tabModelItem.setText(LocalizationHandler.getItem("app.gui.tabs.mach"));
		tabModelItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.machtooltip"));
		tabModelItem.setControl(initModel(tabFolder));
		
		//tab for simulation config
		TabItem tabSimItem = new TabItem(tabFolder, SWT.NONE);
		tabSimItem.setText(LocalizationHandler.getItem("app.gui.tabs.sim"));
		tabSimItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.simtooltip"));
		tabSimItem.setControl(initSim(tabFolder));
		
		//tab for thermal model config - not used at the moment
		//TabItem tabThermalItem = new TabItem(tabFolder, SWT.NONE);
		//tabThermalItem.setText(LocalizationHandler.getItem("app.gui.tabs.thermal"));
		//tabThermalItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.thermaltooltip"));

		//tab for analysis
		final TabItem tabAnalysisItem = new TabItem(tabFolder, SWT.NONE);
		tabAnalysisItem.setText(LocalizationHandler.getItem("app.gui.tabs.analysis"));
		tabAnalysisItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.analysistooltip"));
		tabAnalysisItem.setControl(initAnalysis(tabFolder));
		
		tabFolder.setSelection(0);
		
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
				logger.log(LogLevel.DEBUG, "tab"+ tabFolder.getSelection()[0].getText()+" selected");
				
				System.out.println("tab " + tabFolder.getSelection()[0].getText() + " selected");
				
				// manick: if tab Model is closed -> save machine to .xml
				if(!(tabFolder.getItem(tabFolder.getSelectionIndex()).equals(tabModelItem))){
					System.out.println("Model tab deselected");
		    		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" +
	    					  PropertiesHandler.getProperty("sim.MachineName") + "/MachineConfig/" +
	    				      PropertiesHandler.getProperty("sim.MachineConfigName") + "/";

					Machine.saveMachineToFile(path);
				}	
				
				// manick: if tab Analysis is opened -> Run Simulation
				if (tabFolder.getItem(tabFolder.getSelectionIndex()).equals(tabAnalysisItem))
		        {
					// manick: EModSimRun contains all the necessary commands to run a simulation
					EModSimulationRun.EModSimRun();
					logger.log(LogLevel.DEBUG, "simulation run");
		        }
			}
		});
	}
	
	//manick: open ModelGUI in tab
	private Composite initModel(TabFolder tabFolder){
		model = new ModelGUI(tabFolder);
		return model;
	}
	
	//manick: open SimGUI in tab
	private Composite initSim(TabFolder tabFolder){
		sim = new SimGUI(tabFolder);
		return sim;
	}
	
	private Composite initAnalysis(TabFolder tabFolder) {
		// TODO: manick add selectionListener??
		analysis = new AnalysisGUI("simulation_output.dat", tabFolder);
		// TODO: input file config
		return analysis;
	}

	/**
	 * menu item action listener for save item
	 * 
	 * @author dhampl
	 *
	 */
	class fileNewItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			EModStartupGUI startup = new EModStartupGUI();
			startup.createNewMachineGUI();
		}
		public void widgetDefaultSelected(SelectionEvent event) {
			
		}
	}
	
	/**
	 * menu item action listener for save item
	 * 
	 * @author dhampl
	 *
	 */
	class fileSaveItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			logger.log(LogLevel.DEBUG, "menu save item selected");
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
	        fd.setText(LocalizationHandler.getItem("app.gui.save"));
	        fd.setFilterPath("C:/");
	        String[] filterExt = { "*.xml", "*.*" };
	        fd.setFilterExtensions(filterExt);
	        String selected = fd.open();
	        if(selected == null) {
		        logger.log(LogLevel.DEBUG, "no file specified, closed");
	        	return;
	        }
	        logger.log(LogLevel.DEBUG, "File to save to: "+selected);
	        Machine.saveMachineToNewFile(selected);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			
		}
	}
	
	/**
	 * menu item action listener for load item
	 * 
	 * @author dhampl
	 *
	 */
	class fileOpenItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			logger.log(LogLevel.DEBUG, "menu open item selected");
			FileDialog fd = new FileDialog(shell, SWT.OPEN);
	        fd.setText(LocalizationHandler.getItem("app.gui.open"));
	        fd.setFilterPath("C:/");
	        String[] filterExt = { "*.xml", "*.*" };
	        fd.setFilterExtensions(filterExt);
	        String selected = fd.open();
	        if(selected == null) {
	        	logger.log(LogLevel.DEBUG, "no file selected");
	        	return;
	        }
	        logger.log(LogLevel.DEBUG, "Open file: "+selected);
	        Machine.initMachineFromFile(selected);
	        
			ArrayList<MachineComponent> mclist = Machine.getInstance().getMachineComponentList();

			int i = 0;
			for(MachineComponent mc:mclist){
				ModelGUI.addTableItem(mc, i);
				i++;
			}
	        
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			
		}
	}

	/**
	 * menu item action listener for properties item
	 * 
	 * @author manick
	 *
	 */
	class filePropertiesItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			logger.log(LogLevel.DEBUG, "menu properties item selected");
			new PropertiesGUI();
		}
		public void widgetDefaultSelected(SelectionEvent event) {
			
		}
	}
	
	/**
	 * menu item action listener for exit item
	 * 
	 * @author dhampl
	 *
	 */
	class fileExitItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			logger.log(LogLevel.DEBUG, "menu exit item selected");
			shell.close();
			disp.dispose();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			shell.close();
			disp.dispose();
		}
	}
	
	/**
	 * menu item action listener for comp DB open item
	 * 
	 * @author manick
	 *
	 */
	class compDBNewItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event){
    		//TODO manick implement for new component: i.e. create copy of example component, open it, offer options to edit
			ComponentEditGUI componentEditGUI = new ComponentEditGUI();
			componentEditGUI.newComponentEditGUI();
		}
		public void widgetDefaultSelected(SelectionEvent event){

		}
	}
	
	
	/**
	 * menu item action listener for comp DB open item
	 * 
	 * @author manick
	 *
	 */
	class compDBOpenItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event){
			new ComponentDBGUI();
		}
		public void widgetDefaultSelected(SelectionEvent event){

		}
	}
	
	/**
	 * menu item action listener for help about item
	 * 
	 * @author manick
	 *
	 */
	class helpAboutItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event){
			logger.log(LogLevel.DEBUG, "help about item selected");
			MessageBox messageBox = new MessageBox(shell);
			messageBox.setText(LocalizationHandler.getItem("app.gui.menu.help.about"));
			messageBox.setMessage(LocalizationHandler.getItem("app.gui.menu.help.about.message"));
			messageBox.open();
		}
		
		public void widgetDefaultSelected(SelectionEvent event){

		}
	}

	/**
	 * returns the position of the shell (used to center new windows on current position)
	 * 
	 * @author manick
	 *
	 */

	public static int[] shellPosition(){
		//get postion of current shell
		Rectangle rect = shell.getBounds();
		
		//find the middle of the shell and return two dimensional array
		//position[0]: middle of the shell in horizontal direction
		//position[1]: middle of the shell in vertical direction
		int[] position = {0, 0};
		position[0] = rect.x + rect.width / 2;
		position[1] = rect.y + rect.height / 2;
		
		//return array
		return position;
	}
	
}
