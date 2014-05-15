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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.*;
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
import ch.ethz.inspire.emod.gui.utils.ComponentHandler;
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
	private StyledText bText;
	private Canvas aCanvas;
	private Tree aTree;
	
	/**
	 * @param parent
	 */
	public ModelGUI(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(2, false));
		init();
	}

	
	public void init() {
		//Überschrift des Fensters Maschinenmodell
		aText = new Text(this, SWT.MULTI);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;
		aText.setLayoutData(gridData);
		aText.setText(LocalizationHandler.getItem("app.gui.tabs.machtooltip"));
				
		//Zeichnungsfläche für Maschinenmodell initieren
		aCanvas = new Canvas(this, SWT.MULTI | SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, true);
		gridData.horizontalSpan = 1;
		gridData.widthHint = 700;
		gridData.heightHint = 600;
		aCanvas.setLayoutData(gridData); 
		
		
		//TODO manick: Canvas ersetzen mit Table -> 4 Spaltige Tabelle
		/* Tabellenspalten:
		 * 1: ID der Komponente
		 * 2: Typ der Komponente (Moto, Pump, etc.)
		 * 3: Parametersatz (Siemens_XYZ)
		 * 4: Button: Edit Linking
		 * 	-> Button öffnet Tabelle mit allen I/O, mit Dropdown können dann Verlinkungen gemacht werden....
		 */
		
		
		//TODO manick: Wo wird aus dem String, der in den Tree geladen wird eine Komponente?
		/* Machin.getMachineComponent(); nutzen?
		 * 
		 */
		
		
		//Quellframe linke Seite für Maschinenkomponenten. Realisiert als Tree
		aTree = new Tree(this, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		gridData = new GridData(GridData.FILL, GridData.CENTER, false, true);
		gridData.horizontalSpan = 1;
		gridData.heightHint = 600;
		aTree.setLayoutData(gridData);
		
		//Tree füllen mit aktuellen Werten aus dem Verzeichnis der DB
		ComponentHandler.fillTree(aTree);
		
		//Tree als Drag Source festlegen
		int operations = DND.DROP_COPY;
		final DragSource source = new DragSource(aTree, operations);
		
		//Provide data in Text format
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		source.setTransfer(types);
		
		final TreeItem[] dragSourceItem = new TreeItem[1];
		source.addDragListener(new DragSourceListener() {
			
			//Quelle von dragStart/dragSetData:http://www.tutorials.de/swing-java2d-3d-swt-jface/376583-drag-drop-und-eigener-transfertype-ja-oder-nein.html
			private TreeItem[] selection = null;
			
			public void dragStart(DragSourceEvent event){
				selection = aTree.getSelection();
			}
			
			public void dragSetData(DragSourceEvent event){
				String text = "";
				for(TreeItem item:selection){
					text += (String)item.getText();	
				}
				event.data = text;
			}
			
			public void dragFinished(DragSourceEvent event) {
				}
			});
		
		//Überschrift des Fensters Maschinenmodell
		
		
		//TODO manick: Versuch mit einem StyledText-Widget für den DND
		/*
		 * 
		 */
		
		//neues Styled Text Widget erstellen
		bText = new StyledText(this, SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;
		bText.setLayoutData(gridData);
		
		
		// operations für den DND festlegen
		operations = DND.DROP_COPY;		
		//DropTarget ist das Styled Text Widget
		DropTarget target = new DropTarget(bText, operations);
		
		// typen für den transfer festlegen
		final TextTransfer textTransfer = TextTransfer.getInstance();
		types = new Transfer[] {textTransfer};
		target.setTransfer(types);
		
		
		// dropListener für DND, 
		target.addDropListener(new DropTargetListener(){
			public void dragEnter(DropTargetEvent event){
				event.detail = DND.DROP_COPY;
			}
			public void dragOver(DropTargetEvent event){
				
			}
			public void dragOperationChanged(DropTargetEvent event){
				
			}
			public void dragLeave(DropTargetEvent event){
				
			}
			public void dropAccept(DropTargetEvent event){
				bText.insert((String) event.data);
			}
			public void drop(DropTargetEvent event){
				//String text = (String)event.data;
				//TextItem item = new TextItem(bText, SWT.NONE);
				//System.out.println(text);
				bText.insert((String) event.data);
				//bText.insert(text);
			}
		});
		
		
		
		}
	}