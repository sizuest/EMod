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
package ch.ethz.inspire.emod.dd.gui;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.dd.Duct;
import ch.ethz.inspire.emod.gui.EditMachineComponentGUI;
import ch.ethz.inspire.emod.gui.EditMaterialGUI;
import ch.ethz.inspire.emod.gui.MachineComponentDBGUI;
import ch.ethz.inspire.emod.gui.MaterialDBGUI;
import ch.ethz.inspire.emod.gui.utils.ProgressbarGUI;
import ch.ethz.inspire.emod.gui.utils.ShowButtons;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * main gui class for emod application
 * 
 * @author dhampl
 *
 */
public class DuctDesinerGUI {

	private static Logger logger = Logger.getLogger(DuctDesinerGUI.class.getName());
	
	protected static Shell shell;
	protected Display disp;
		
	protected Duct duct;
	protected String path = "";
	
	private MenuItem editRedo, editUndo;

	
	protected DuctConfigGUI ductDesigner;
	protected StyledText console;
	
	public DuctDesinerGUI(Display display) {
		disp = display;
		shell = new Shell(display);
		
		
		ProgressbarGUI pg = new ProgressbarGUI("app.gui.startup.progressbar");
		pg.updateProgressbar(0);
	
		
		shell.setText(LocalizationHandler.getItem("app.dd.gui.title"));
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
		
		pg.updateProgressbar(20);
		
		duct = new Duct("Duct");
		
		pg.updateProgressbar(50);
		
		//init tabs
		logger.log(LogLevel.DEBUG, "init tabs");
		initTabs();

		pg.updateProgressbar(80);
		
		shell.open();
		
		pg.updateProgressbar(100);
	
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
			MenuItem fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
			fileExitItem.setText(LocalizationHandler.getItem("app.gui.menu.file.exit"));
			
		//create "Edit" tab and items
		MenuItem editMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		editMenuHeader.setText(LocalizationHandler.getItem("app.gui.menu.edit"));
		Menu editMenu = new Menu(shell, SWT.DROP_DOWN);
		editMenuHeader.setMenu(editMenu);
			editUndo = new MenuItem(editMenu, SWT.PUSH);
			editUndo.setText(LocalizationHandler.getItem("app.gui.menu.edit.undo"));
			editUndo.setEnabled(false);
			editRedo = new MenuItem(editMenu, SWT.PUSH);
			editRedo.setText(LocalizationHandler.getItem("app.gui.menu.edit.redo"));
			editRedo.setEnabled(false);
				
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
		fileExitItem.addSelectionListener(new fileExitItemListener());
		
		editMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuShown(MenuEvent e) {
				setUndoRedoAvailability();
			}
			
			@Override
			public void menuHidden(MenuEvent e) {
				// Not used
			}
		});
		
		editRedo.addSelectionListener(new editRedoItemListener());
		editUndo.addSelectionListener(new editUndoItemListener());
		
		compDBNewItem.addSelectionListener(new compDBNewItemListener());
		compDBOpenItem.addSelectionListener(new compDBOpenItemListener());
		
		matDBNewItem.addSelectionListener(new matDBNewItemListener());
		matDBOpenItem.addSelectionListener(new matDBOpenItemListener());
		
		helpAboutItem.addSelectionListener(new helpAboutItemListener());
		
		//ductDesignTestItem.addSelectionListener(new ductDesignTestItemListener());
		
		shell.setMenuBar(menuBar);
	}
	
	private void initTabs(){
		
		ductDesigner = new DuctConfigGUI(shell, SWT.NONE, this.duct, ShowButtons.NONE);
		
		//tab for console
		final TabItem tabConsoleItem = new TabItem(ductDesigner.getTabFolder(), SWT.NONE);
		tabConsoleItem.setText(LocalizationHandler.getItem("app.gui.tabs.console"));
		tabConsoleItem.setToolTipText(LocalizationHandler.getItem("app.gui.tabs.consoletooltip"));
		tabConsoleItem.setControl(initConsole(ductDesigner.getTabFolder()));
		
		ductDesigner.getTabFolder().setSelection(0);
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
			logger.log(LogLevel.DEBUG, "menu new item selected");
			newDuct();
		}
		public void widgetDefaultSelected(SelectionEvent event) {
			// Not used
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
			saveDuct();
		}
		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			// Not used
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
			saveDuctAs();
		}
		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			// Not used
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
			openDuct();
		}
		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			// Not used
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
	
	class editUndoItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			undo();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// Not used
		}
	}
	
	class editRedoItemListener implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			redo();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// Not used
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
			EditMachineComponentGUI.newMachineComponentGUI(shell);
		}
		public void widgetDefaultSelected(SelectionEvent event){
			// Not used
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
			// Not used
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
			EditMaterialGUI.newMaterialGUI(shell);
		}
		public void widgetDefaultSelected(SelectionEvent event){
			// Not used
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
			// Not used
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
			// Not used
		}
	}


	/**
	 * returns the position of the shell (used to center new windows on current position)
	 * 
	 * @author manick
	 * @return shell position
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
	
	private void newDuct(){
		this.path = "";
		this.duct.clear();
		this.ductDesigner.setDuct(duct);
		this.ductDesigner.update();
	}
	
	private void saveDuct(){
		if(""==this.path)
			saveDuctAs();
		else
			this.duct.saveToFile(this.path);
	}
	
	private void saveDuctAs(){
		String path = getFilePath(LocalizationHandler.getItem("app.gui.file.saveas"));
		if(null==path)
			return;
		
		this.path = path;
		this.duct.saveToFile(this.path);
	}
	
	private void openDuct(){
		String path = getFilePath(LocalizationHandler.getItem("app.gui.file.open"));
		if(null==path)
			return;
		
		this.path = path;
		this.duct = Duct.buildFromFile(this.path);
		this.ductDesigner.setDuct(duct);
		this.ductDesigner.update();
	}
	
	
	private String getFilePath(String titel){
		FileDialog fd = new FileDialog(shell, SWT.SAVE);
        fd.setText(titel);
        fd.setFilterPath("C:/");
        fd.setFilterExtensions(new String[] { "*.duct", "*.*" });
        String selected = fd.open();
        
        return selected;
	}
	
	private void undo(){
		this.duct.undo();
		this.ductDesigner.update();
		setUndoRedoAvailability();
	}
	
	private void redo(){
		this.duct.redo();
		this.ductDesigner.update();
		setUndoRedoAvailability();
	}
	
	private void setUndoRedoAvailability(){
		editUndo.setEnabled(this.duct.undoPossible());
		editRedo.setEnabled(this.duct.redoPossible());
		
		editUndo.setText(LocalizationHandler.getItem("app.gui.menu.edit.undo")+" "+LocalizationHandler.getItem(this.duct.getUndoComment()));
		editRedo.setText(LocalizationHandler.getItem("app.gui.menu.edit.redo")+" "+LocalizationHandler.getItem(this.duct.getRedoComment()));
	}
}
