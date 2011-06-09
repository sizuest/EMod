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

import org.eclipse.swt.*;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import ch.ethz.inspire.emod.LocalizationHandler;
import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.MessageHandler;

/**
 * main gui class for emod application
 * 
 * @author dhampl
 *
 */
public class EModGUI {

	protected Shell shell;
	protected Display disp;
	
	public EModGUI(Display display) {
		disp = display;
		shell = new Shell(display);
		
		shell.setText(LocalizationHandler.getItem("app.gui.title"));
		if(display.getBounds().width >= 1024)
			shell.setSize(1024, 768);
		else
			shell.setSize(display.getBounds().width, display.getBounds().height);
		
		//init menu bar
		MessageHandler.logMessage(LogLevel.DEBUG, "init menu");
		initMenu();
		
		//init tabs
		MessageHandler.logMessage(LogLevel.DEBUG, "init tabs");
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
		final TabFolder tabFolder = new TabFolder(shell, SWT.BORDER);
		TabItem tabModelItem = new TabItem(tabFolder, SWT.NULL);
		tabModelItem.setText("Modell");
	}
	
	/**
	 * menu item action listener for save item
	 * 
	 * @author dhampl
	 *
	 */
	class fileSaveItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			MessageHandler.logMessage(LogLevel.DEBUG, "menu save item selected");
			//TODO call control for save.
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			// TODO Auto-generated method stub
			
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
			MessageHandler.logMessage(LogLevel.DEBUG, "menu load item selected");
			//TODO call control for load
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			// TODO Auto-generated method stub
			
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
			MessageHandler.logMessage(LogLevel.DEBUG, "menu exit item selected");
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
