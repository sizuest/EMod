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

import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
//import java.awt.Desktop;
import java.io.File;
import java.io.FileFilter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
//import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.gui.AnalysisGUI.MachineComponentComposite;
import ch.ethz.inspire.emod.gui.utils.BarChart;
import ch.ethz.inspire.emod.gui.utils.ConsumerData;
import ch.ethz.inspire.emod.gui.utils.LineChart;
import ch.ethz.inspire.emod.gui.utils.StackedAreaChart;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * @author manick
 *
 */

public class ModelGUI extends Composite {
	
	private Text aText;
	private Button aButton;
	private Button bButton;
	private Canvas aCanvas;
	private Tree aTree;
	
	/**
	 * @param parent
	 */
	public ModelGUI(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(3, false));
		init();
	}

	
	public void init() {
		//Überschrift des Fensters Maschinenmodell
		aText = new Text(this, SWT.MULTI);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 1;
		aText.setLayoutData(gridData);
		aText.setText(LocalizationHandler.getItem("app.gui.tabs.machtooltip"));
		
		//Button zur Anzeige von Machine.xml
		aButton = new Button(this, SWT.NONE);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 1;
		gridData.widthHint = 150;
		aButton.setLayoutData(gridData);
		aButton.setText(LocalizationHandler.getItem("app.gui.tabs.showmachxml"));
		aButton.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		        System.out.println("Called Button A!");
				
		        
		        /*String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" + PropertiesHandler.getProperty("app.MachineName") + "/MachineConfig/" + PropertiesHandler.getProperty("MachineConfigName") + "/Machine.xml";
				File machineXML = new File(path);
				System.out.println(path);
				//Desktop.getDesktop().open(File machineXML);	*/			
		    }
		}); 
		
		//Button zur Anzeige von IOLinking.xml
		bButton = new Button(this, SWT.NONE);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 1;
		gridData.widthHint = 150;
		bButton.setLayoutData(gridData);
		bButton.setText(LocalizationHandler.getItem("app.gui.tabs.showiolinkingtxt"));
		bButton.addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		        System.out.println("Called Button B!");
		    }
		}); 
		
		//Zeichnungsfläche für Maschinenmodell initieren
		aCanvas = new Canvas(this, SWT.MULTI | SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, true);
		gridData.horizontalSpan = 2;
		gridData.widthHint = 700;
		gridData.heightHint = 600;
		aCanvas.setLayoutData(gridData); 
		
		//Quellframe linke Seite für Maschinenkomponenten. Realisiert als Tree
		aTree = new Tree(this, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, true);
		gridData.horizontalSpan = 1;
		gridData.heightHint = 600;
		aTree.setLayoutData(gridData);
		
		//Maschinenkomponenten-Ordner aus Pfad auslesen
		String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/";
		File dir = new File(path);		
		File[] subDirs = dir.listFiles();
		Arrays.sort(subDirs);
		
		//Maschinenkomponenten-Kategorien als TreeItems schreiben
		for (int i = 0; i < subDirs.length; i++){
			TreeItem child = new TreeItem(aTree, SWT.NONE);
			child.setText(subDirs[i].getName());
			
			//Einzelne Konfigurationen der Komponenten-Kategorie aus Pfad auslesen
			String subpath = path + subDirs[i].getName() + "/";
			dir = new File(subpath);
			File[] subDirsComponents = dir.listFiles();
			Arrays.sort(subDirsComponents);
			
			//Einzelne Konfigurationen der Komponenten Kategorie unter entsprechender Kategorie ausgeben
			for(int j = 0; j < subDirsComponents.length; j++){
				TreeItem grandChild = new TreeItem(child, SWT.NONE);
				grandChild.setText(subDirsComponents[j].getName());
			}
		} 
	}
}
