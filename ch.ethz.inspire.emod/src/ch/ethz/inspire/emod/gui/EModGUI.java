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

import java.util.logging.Logger;

import org.eclipse.swt.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import ch.ethz.inspire.emod.utils.LocalizationHandler;
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
	protected Shell shell;
	protected Display disp;
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
		MenuItem fileSaveItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveItem.setText(LocalizationHandler.getItem("app.gui.menu.save"));
		MenuItem fileLoadItem = new MenuItem(fileMenu, SWT.PUSH);
		fileLoadItem.setText(LocalizationHandler.getItem("app.gui.menu.load"));
		MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
		fileExitItem.setText(LocalizationHandler.getItem("app.gui.menu.exit"));
		
		//add listeners
		fileSaveItem.addSelectionListener(new fileSaveItemListener());
		fileLoadItem.addSelectionListener(new fileLoadItemListener());
		fileExitItem.addSelectionListener(new fileExitItemListener());
		
		shell.setMenuBar(menuBar);
	}
	
	private void initTabs(){
		//create the tab folder container
		final TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		
		//tab for machine model config
		TabItem tabModelItem = new TabItem(tabFolder, SWT.NONE);
		tabModelItem.setText(LocalizationHandler.getItem("app.gui.tabs.mach"));
		tabModelItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.machtooltip"));
		
		//tab for simulation config
		TabItem tabSimItem = new TabItem(tabFolder, SWT.NONE);
		tabSimItem.setText(LocalizationHandler.getItem("app.gui.tabs.sim"));
		tabSimItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.simtooltip"));
		
		//tab for thermal model config
		TabItem tabThermalItem = new TabItem(tabFolder, SWT.NONE);
		tabThermalItem.setText(LocalizationHandler.getItem("app.gui.tabs.thermal"));
		tabThermalItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.thermaltooltip"));

		//tab for analysis
		TabItem tabAnalysisItem = new TabItem(tabFolder, SWT.NONE);
		tabAnalysisItem.setText(LocalizationHandler.getItem("app.gui.tabs.analysis"));
		tabAnalysisItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.analysistooltip"));
		tabAnalysisItem.setControl(initAnalysis(tabFolder));
		
		tabFolder.setSelection(0);
		
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
				logger.log(LogLevel.DEBUG, "tab"+ tabFolder.getSelection()[0].getText()+" selected");
			}
		});
	}
	
	private Composite initAnalysis(TabFolder tabFolder) {
		analysis = new AnalysisGUI("simulation_output.dat", tabFolder);
		
		
		
		return analysis;
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
	        fd.setText("Save");
	        fd.setFilterPath("C:/");
	        String[] filterExt = { "*.xml", "*.*" };
	        fd.setFilterExtensions(filterExt);
	        String selected = fd.open();
	        logger.log(LogLevel.DEBUG, "File to save to: "+selected);
	        Machine.saveMachineToFile(selected);
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
	class fileLoadItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			logger.log(LogLevel.DEBUG, "menu load item selected");
			FileDialog fd = new FileDialog(shell, SWT.OPEN);
	        fd.setText("Open");
	        fd.setFilterPath("C:/");
	        String[] filterExt = { "*.xml", "*.*" };
	        fd.setFilterExtensions(filterExt);
	        String selected = fd.open();
	        logger.log(LogLevel.DEBUG, "Load file: "+selected);
	        Machine.initMachineFromFile(selected);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
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
}
