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
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.gui.AnalysisGUI.MachineComponentComposite;
import ch.ethz.inspire.emod.gui.utils.BarChart;
import ch.ethz.inspire.emod.gui.utils.ComponentHandler;
import ch.ethz.inspire.emod.gui.utils.ConsumerData;
import ch.ethz.inspire.emod.gui.utils.LineChart;
import ch.ethz.inspire.emod.gui.utils.StackedAreaChart;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * @author manick
 *
 */

public class ModelGUI extends Composite {
	
	private Text textModelTitel;
	private Table tableModelView;
	private Tree treeComponentDBView;
	
	/**
	 * @param parent
	 */
	public ModelGUI(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(3, true));
		init();
	}

	
	public void init() {
		//Überschrift des Fensters Maschinenmodell
		textModelTitel = new Text(this, SWT.MULTI);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
		gridData.horizontalSpan = 3;
		textModelTitel.setLayoutData(gridData);
		textModelTitel.setText(LocalizationHandler.getItem("app.gui.tabs.machtooltip"));
		
		//Tabelle links für Maschinenmodell initieren
		tableModelView = new Table(this, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.horizontalSpan = 2;
		//gridData.widthHint = 600;
		//gridData.heightHint = 600;
		tableModelView.setLayoutData(gridData);
		tableModelView.setLinesVisible(true);
		tableModelView.setHeaderVisible(true);
		
		//Titel der Spalten setzen
		//TODO manick: Werte in Languagepack übernehmen
		String[] titles =  {"ID", "Type", "Parameter", "edit Component", "edit Linking", "delete Component"};
		for(int i=0; i < titles.length; i++){
			TableColumn column = new TableColumn(tableModelView, SWT.NULL);
			column.setText(titles[i]);
		}
		
        //Tabelle schreiben
        TableColumn[] columns = tableModelView.getColumns();
        for (int i = 0; i < columns.length; i++) {
          columns[i].pack();
        }
					
		//Quellframe rechte Seite für Maschinenkomponenten. Realisiert als Tree
		treeComponentDBView = new Tree(this, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.horizontalSpan = 1;
		//gridData.heightHint = 600;
		treeComponentDBView.setLayoutData(gridData);
		
		//Tree füllen mit aktuellen Werten aus dem Verzeichnis der DB
		ComponentHandler.fillTree(treeComponentDBView);
		
		System.out.println("ModelGUI called ComponentHandler.fillTree");
		
		//Tree als Drag Source festlegen
		int operations = DND.DROP_COPY;
		final DragSource source = new DragSource(treeComponentDBView, operations);
		
		//Provide data in Text format
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		source.setTransfer(types);
		
		source.addDragListener(new DragSourceListener() {
			
			//SOURCE von dragStart/dragSetData:http://www.tutorials.de/swing-java2d-3d-swt-jface/376583-drag-drop-und-eigener-transfertype-ja-oder-nein.html
			private TreeItem[] selection = null;
			
			public void dragStart(DragSourceEvent event){
				selection = treeComponentDBView.getSelection();
				System.out.println("Drag started with " + selection.toString() + " Component");
			}
			
			public void dragSetData(DragSourceEvent event){
				String text = "";
				for(TreeItem item:selection){
					text += (String)item.getText();	
				}
				event.data = text;

				System.out.println("Drag data set to " + text);
			}
			
			public void dragFinished(DragSourceEvent event) {
				}
			});
		
		//Table als Drop Target festlegen
		operations = DND.DROP_COPY;
		DropTarget target = new DropTarget(tableModelView, operations);
		
		//Texttransfer akzeptieren
		final TextTransfer textTransfer = TextTransfer.getInstance();
		types = new Transfer[] {textTransfer};
		target.setTransfer(types);
		
		
		target.addDropListener(new DropTargetListener(){
			public void dragEnter(DropTargetEvent event){
				event.detail = DND.DROP_COPY;
			}
			public void dragOver(DropTargetEvent event){

			}
			public void dragLeave(DropTargetEvent event){
				
			}
			public void dragOperationChanged(DropTargetEvent event) {
				
			}
			public void dropAccept(DropTargetEvent event){
				
			}
			public void drop(DropTargetEvent event){
				tableModelView.setRedraw(false);
				
				//String des DnD entgegennehmen, und aufsplitten in type und parameter
				String string = null;
		        string = (String) event.data;
		        final String[] split = string.split("_",2);
		        split[1] = split[1].replace(".xml","");
		        
		        System.out.println("New Component " + split[1] + " added to Machine");
		        
		        //TODO manick: funktioniert das wirklich wie gedacht?
		        //Position des Drops ermitteln		        
		        Point p = event.display.map(null, tableModelView, event.x, event.y);
		        TableItem dropItem = tableModelView.getItem(p);
		        int index = dropItem == null ? tableModelView.getItemCount() : tableModelView.indexOf(dropItem);
		        
		        //Tabelleninhalte füllen
		        final TableItem item = new TableItem(tableModelView, SWT.NONE, index);
		        
		        // aus split[0] und split[1] eine Komponente erstellen
		        final MachineComponent mc = Machine.addNewMachineComponent(split[0],split[1]); 
		        Machine.addMachineComponent(mc);
		        System.out.println("New Component " + mc.getName() + " created");
		        
		        
		        //TODO manick: ID Vergabe organisieren
		        item.setText(0, mc.getName());
		        
		        //TODO manick: Vorgehen ändern --> zuerst Componente erzeugen und ID erstellen --> Werte von Komponente in Tabelle schreiben!
		        
		        //Type und Parameter in Tabelle schreiben
		        item.setText(1, split[0]);
		        item.setText(2, split[1]);
		        
		        //Button für edit Component erstellen
		        TableEditor editor = new TableEditor(tableModelView);
		        final Button buttonEditComponent = new Button(tableModelView, SWT.PUSH);
		        buttonEditComponent.setText("edit Component");
		        buttonEditComponent.addSelectionListener(new SelectionListener(){
		        	public void widgetSelected(SelectionEvent event){
		        		
		        			        		
		        		System.out.println("Button edit Component of component " + split[1]);
		        	}
		        	public void widgetDefaultSelected(SelectionEvent event){
		        		
		        	}
		        });
		        buttonEditComponent.pack();
		        editor.minimumWidth = buttonEditComponent.getSize().x;
		        editor.horizontalAlignment = SWT.LEFT;
		        editor.setEditor(buttonEditComponent, item, 3);
		        
		        //Edit Linking Button in zweitletzter Spalte erstellen
		        //SOURCE http://www.java2s.com/Tutorial/Java/0280__SWT/TableCellEditorComboTextandButton.htm
		        editor = new TableEditor(tableModelView);
		        final Button buttonEditLinking = new Button(tableModelView, SWT.PUSH);
		        buttonEditLinking.setText("edit Linking");
		        buttonEditLinking.addSelectionListener(new SelectionListener(){
		        	public void widgetSelected(SelectionEvent event){
		        		
		        		//Fenster fürs IO Linking öffnen
		        		LinkingGUI linkingGUI = new LinkingGUI();
		        		linkingGUI.openLinkingGUI(split[1]);
		        		
		        		System.out.println("Button edit Linking of component " + split[1]);
		        	}
		        	public void widgetDefaultSelected(SelectionEvent event){
		        		
		        	}
		        });
		        buttonEditLinking.pack();
		        editor.minimumWidth = buttonEditLinking.getSize().x;
		        editor.horizontalAlignment = SWT.LEFT;
		        editor.setEditor(buttonEditLinking, item, 4);
		        
		        //Delete Component Button in letzter Spalte erstellen
		        editor = new TableEditor(tableModelView);
		        final Button buttonDeleteComponent = new Button(tableModelView, SWT.PUSH);
		        buttonDeleteComponent.setText("delete Component");
		        buttonDeleteComponent.addSelectionListener(new SelectionListener(){
		        	public void widgetSelected(SelectionEvent event){
		        		
			    		tableModelView.setRedraw(false);
			    		
		        		//TODO manick: Delete Component
		        		Machine.removeMachineComponent(mc);

		        		buttonEditLinking.dispose();
		        		buttonDeleteComponent.dispose();
		        		buttonEditComponent.dispose();
		        		item.dispose();
		        		
				        TableColumn[] columns = tableModelView.getColumns();
				        for (int i = 0; i < columns.length; i++) {
				          columns[i].pack();
				        }
		        		
		        		System.out.println("Button delete Component " + split[1]);
		        		
			    		tableModelView.setRedraw(true);	

		        	}
		        	public void widgetDefaultSelected(SelectionEvent event){
		        		
		        	}
		        });
		        buttonDeleteComponent.pack();
		        editor.minimumWidth = buttonDeleteComponent.getSize().x;
		        editor.horizontalAlignment = SWT.LEFT;
		        editor.setEditor(buttonDeleteComponent, item, 5);		        
		        

		        
		        //Tabelle schreiben
		        TableColumn[] columns = tableModelView.getColumns();
		        for (int i = 0; i < columns.length; i++) {
		          columns[i].pack();
		        }
		        tableModelView.setRedraw(true);
			}
		});	
		}
	}