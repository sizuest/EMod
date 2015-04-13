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

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import ch.ethz.inspire.emod.gui.utils.ProgressbarGUI;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.simulation.EModSimulationRun;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;
import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.States;

/**
 * main gui class for emod application
 * 
 * @author dhampl
 *
 */
public class EModGUI {

	private static Logger logger = Logger.getLogger(EModGUI.class.getName());
	
	protected static Shell shell;
	protected Display disp;
	
	protected static TabFolder tabFolder;

	
	protected Composite model;
	protected Composite sim;
	protected Composite analysis;
	protected StyledText console;
	
	public EModGUI(Display display) {
		disp = display;
		shell = new Shell(display);
		
		
		ProgressbarGUI pg = new ProgressbarGUI("app.gui.startup.progressbar");
		pg.updateProgressbar(0);
	
		
		shell.setText(LocalizationHandler.getItem("app.gui.title"));
		if(display.getBounds().width >= 1024)
			shell.setSize(1024, 768);
		else
			shell.setSize(display.getBounds().width, display.getBounds().height);
		
	    Monitor primary = display.getPrimaryMonitor();
	    Rectangle bounds = primary.getBounds();
	    Rectangle rect = shell.getBounds();
	    
	    int x = bounds.x + (bounds.width - rect.width) / 2;
	    int y = bounds.y + (bounds.height - rect.height) / 2;
	    
	    shell.setLocation(x, y);
		
		shell.setLayout(new FillLayout());
		
		pg.updateProgressbar(10);
		
		//init menu bar
		logger.log(LogLevel.DEBUG, "init menu");
		initMenu();
		
		pg.updateProgressbar(50);
		
		//init tabs
		logger.log(LogLevel.DEBUG, "init tabs");
		initTabs();

		pg.updateProgressbar(80);
		
		shell.open();
		
		pg.updateProgressbar(100);

		
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
			fileNewItem.setImage(new Image(Display.getDefault(), "src/resources/New16.gif"));
			fileNewItem.setText(LocalizationHandler.getItem("app.gui.menu.file.new"));
			MenuItem fileOpenItem = new MenuItem(fileMenu, SWT.PUSH);
			fileOpenItem.setImage(new Image(Display.getDefault(), "src/resources/Open16.gif"));			
			fileOpenItem.setText(LocalizationHandler.getItem("app.gui.menu.file.open"));
			MenuItem fileSaveItem = new MenuItem(fileMenu, SWT.PUSH);
			fileSaveItem.setImage(new Image(Display.getDefault(), "src/resources/Save16.gif"));
			fileSaveItem.setText(LocalizationHandler.getItem("app.gui.menu.file.save"));
			MenuItem fileSaveAsItem = new MenuItem(fileMenu, SWT.PUSH);
			fileSaveAsItem.setImage(new Image(Display.getDefault(), "src/resources/SaveAs16.gif"));
			fileSaveAsItem.setText(LocalizationHandler.getItem("app.gui.menu.file.saveas"));			
			MenuItem filePropertiesItem = new MenuItem(fileMenu, SWT.PUSH);
			filePropertiesItem.setImage(new Image(Display.getDefault(), "src/resources/Preferences16.gif"));
			filePropertiesItem.setText(LocalizationHandler.getItem("app.gui.menu.file.properties"));
			MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
			fileExitItem.setText(LocalizationHandler.getItem("app.gui.menu.file.exit"));
		
		//create "Database Components" tab and items
		MenuItem compDBMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		compDBMenuHeader.setText(LocalizationHandler.getItem("app.gui.menu.compDB"));
		Menu compDBMenu = new Menu(shell, SWT.DROP_DOWN);
		compDBMenuHeader.setMenu(compDBMenu);
			MenuItem compDBNewItem = new MenuItem(compDBMenu, SWT.PUSH);
			compDBNewItem.setImage(new Image(Display.getDefault(), "src/resources/New16.gif"));
			compDBNewItem.setText(LocalizationHandler.getItem("app.gui.menu.compDB.new"));
			MenuItem compDBOpenItem = new MenuItem(compDBMenu, SWT.PUSH);
			compDBOpenItem.setImage(new Image(Display.getDefault(), "src/resources/Open16.gif"));
			compDBOpenItem.setText(LocalizationHandler.getItem("app.gui.menu.compDB.open"));
			//MenuItem compDBImportItem = new MenuItem(compDBMenu, SWT.PUSH);
			//compDBImportItem.setText(LocalizationHandler.getItem("app.gui.menu.compDB.import"));
		
		//create "Database Material" tab and items
		MenuItem matDBMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		matDBMenuHeader.setText(LocalizationHandler.getItem("app.gui.menu.matDB"));
		Menu matDBMenu = new Menu(shell, SWT.DROP_DOWN);
		matDBMenuHeader.setMenu(matDBMenu);
			MenuItem matDBNewItem = new MenuItem(matDBMenu, SWT.PUSH);
			matDBNewItem.setImage(new Image(Display.getDefault(), "src/resources/New16.gif"));
			matDBNewItem.setText(LocalizationHandler.getItem("app.gui.menu.matDB.new"));
			MenuItem matDBOpenItem = new MenuItem(matDBMenu, SWT.PUSH);
			matDBOpenItem.setImage(new Image(Display.getDefault(), "src/resources/Open16.gif"));
			matDBOpenItem.setText(LocalizationHandler.getItem("app.gui.menu.matDB.open"));
			
		//create "Help" tab and items
		MenuItem helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText(LocalizationHandler.getItem("app.gui.menu.help"));
		Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuHeader.setMenu(helpMenu);
			//MenuItem helpContentItem = new MenuItem(helpMenu, SWT.PUSH);
			//helpContentItem.setText(LocalizationHandler.getItem("app.gui.menu.help.content"));
			MenuItem helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
	        helpAboutItem.setImage(new Image(Display.getDefault(), "src/resources/About16.gif"));
			helpAboutItem.setText(LocalizationHandler.getItem("app.gui.menu.help.about"));
		
		//add listeners
		fileNewItem.addSelectionListener(new fileNewItemListener());
		fileOpenItem.addSelectionListener(new fileOpenItemListener());
		fileSaveItem.addSelectionListener(new fileSaveItemListener());
		fileSaveAsItem.addSelectionListener(new fileSaveAsItemListener());
		filePropertiesItem.addSelectionListener(new filePropertiesItemListener());
		fileExitItem.addSelectionListener(new fileExitItemListener());
		
		compDBNewItem.addSelectionListener(new compDBNewItemListener());
		compDBOpenItem.addSelectionListener(new compDBOpenItemListener());
		
		matDBNewItem.addSelectionListener(new matDBNewItemListener());
		matDBOpenItem.addSelectionListener(new matDBOpenItemListener());
		
		helpAboutItem.addSelectionListener(new helpAboutItemListener());
		
		shell.setMenuBar(menuBar);
	}
	
	private void initTabs(){
		//create the tab folder container
		//final TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder = new TabFolder(shell, SWT.NONE);
		
		//tab for machine model config
		final TabItem tabModelItem = new TabItem(tabFolder, SWT.NONE);
		tabModelItem.setText(LocalizationHandler.getItem("app.gui.tabs.mach"));
		tabModelItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.machtooltip"));
		tabModelItem.setControl(initModel(tabFolder));
		
		//tab for simulation config
		final TabItem tabSimItem = new TabItem(tabFolder, SWT.NONE);
		tabSimItem.setText(LocalizationHandler.getItem("app.gui.tabs.sim"));
		tabSimItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.simtooltip"));
		final Composite tabSimControl = initSim(tabFolder);
		tabSimItem.setControl(tabSimControl);
		
		//tab for thermal model config - not used at the moment
		//TabItem tabThermalItem = new TabItem(tabFolder, SWT.NONE);
		//tabThermalItem.setText(LocalizationHandler.getItem("app.gui.tabs.thermal"));
		//tabThermalItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.thermaltooltip"));

		//tab for analysis
		final TabItem tabAnalysisItem = new TabItem(tabFolder, SWT.NONE);
		tabAnalysisItem.setText(LocalizationHandler.getItem("app.gui.tabs.analysis"));
		tabAnalysisItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.analysistooltip"));
		final Composite tabAnalysisControl = initAnalysis(tabFolder);
		tabAnalysisItem.setControl(tabAnalysisControl);
		
		//tab for console
		final TabItem tabConsoleItem = new TabItem(tabFolder, SWT.NONE);
		tabConsoleItem.setText(LocalizationHandler.getItem("app.gui.tabs.console"));
		tabConsoleItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.consoletooltip"));
		tabConsoleItem.setControl(initConsole(tabFolder));
		
		tabFolder.setSelection(0);
		
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
				logger.log(LogLevel.DEBUG, "tab"+ tabFolder.getSelection()[0].getText()+" selected");
				
				System.out.println("tab " + tabFolder.getSelection()[0].getText() + " selected");
				
				if (tabFolder.getItem(tabFolder.getSelectionIndex()).equals(tabSimItem))
		        {
					tabSimControl.update();
					logger.log(LogLevel.DEBUG, "updating simulation configuration tabs");
		        }
					
				// manick: if tab Analysis is opened -> Run Simulation
				if (tabFolder.getItem(tabFolder.getSelectionIndex()).equals(tabAnalysisItem))
		        {
					System.out.println("Simulation start initialization: Saving Machine and IOLinking");
					// Save all
					Machine.saveMachine(PropertiesHandler.getProperty("sim.MachineName"), PropertiesHandler.getProperty("sim.MachineConfigName"));
					States.saveStates(PropertiesHandler.getProperty("sim.MachineName"), PropertiesHandler.getProperty("sim.SimulationConfigName"));
					// manick: EModSimRun contains all the necessary commands to run a simulation
					EModSimulationRun.EModSimRun();
					logger.log(LogLevel.DEBUG, "simulation run");
					tabAnalysisControl.update();
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
		analysis = new AnalysisGUI("simulation_output.dat", tabFolder);
		// TODO: input file config
		return analysis;
	}
	
	private StyledText initConsole(TabFolder tabFolder) {
		console = new StyledText(tabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
		console.setEditable(false);
		console.setFont(new Font(disp, "Mono", 8, SWT.NORMAL ));
		
		// Pipe Console
		final PrintStream backupSystemOutStream = System.out;
		final PrintStream backupSystemErrStream = System.err;
	    System.setOut(new PrintStream(backupSystemOutStream) {
	      public void print(final String s) {
	    	  disp.asyncExec(new Runnable() {
	          public void run() {
	        	  String msg = "["+(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")).format((new Date()))+"] INFO:  "+s+"\n";
	        	  console.append(msg);
	        	  
	        	  StyleRange[] sr = new StyleRange[1];
	        	  sr[0] = new StyleRange(console.getCharCount()-msg.length(), msg.length(), disp.getSystemColor(SWT.COLOR_BLACK), disp.getSystemColor(SWT.COLOR_WHITE));
	        	  console.replaceStyleRanges(sr[0].start, sr[0].length, sr);
	          }
	        });
	        super.print(s);
	      }
	    });
	    System.setErr(new PrintStream(backupSystemErrStream) {
	      public void print(final String s) {
	    	  disp.asyncExec(new Runnable() {
	          public void run() {
	        	  String msg = "["+(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")).format((new Date()))+"] ERROR: "+s+"\n";
	        	  console.append(msg);
	        	  
	        	  StyleRange[] sr = new StyleRange[1];
	        	  sr[0] = new StyleRange(console.getCharCount()-msg.length(), msg.length(), disp.getSystemColor(SWT.COLOR_RED), disp.getSystemColor(SWT.COLOR_WHITE));
	        	  console.replaceStyleRanges(sr[0].start, sr[0].length, sr);
	          }
	        });
	        super.print(s);
	      }
	    });
		
		// TODO: input file config
		return console;
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
	 * @author sizuest
	 *
	 */
	class fileSaveItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			logger.log(LogLevel.DEBUG, "menu save item selected");
			Machine.saveMachine(PropertiesHandler.getProperty("sim.MachineName"), PropertiesHandler.getProperty("sim.MachineConfigName"));
			Machine.saveInitialConditions();
			States.saveStates(PropertiesHandler.getProperty("sim.MachineName"), PropertiesHandler.getProperty("sim.SimulationConfigName"));
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			
		}
	}
	
	/**
	 * menu item action listener for save as item
	 * 
	 * @author dhampl
	 *
	 */
	class fileSaveAsItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			logger.log(LogLevel.DEBUG, "menu save as item selected");
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
	 * menu item action listener for comp DB new item
	 * 
	 * @author manick
	 *
	 */
	class compDBNewItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event){
			EditMachineComponentGUI componentEditGUI = new EditMachineComponentGUI();
			componentEditGUI.newMachineComponentGUI();
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
			new MachineComponentDBGUI();
		}
		public void widgetDefaultSelected(SelectionEvent event){

		}
	}
	
	/**
	 * menu item action listener for material DB new item
	 * 
	 * @author manick
	 *
	 */
	class matDBNewItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event){
			EditMaterialGUI materialEditGUI = new EditMaterialGUI();
			materialEditGUI.newMaterialGUI();
		}
		public void widgetDefaultSelected(SelectionEvent event){

		}
	}
	
	
	/**
	 * menu item action listener for material DB open item
	 * 
	 * @author manick
	 *
	 */
	class matDBOpenItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event){
			new MaterialDBGUI();
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
