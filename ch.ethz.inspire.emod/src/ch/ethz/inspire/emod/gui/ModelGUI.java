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
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.gui.utils.MachineComponentHandler;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.model.units.SiUnitDefinition;
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * @author manick
 *
 */

public class ModelGUI extends AGUITab {
	
	private static Table tableModelView;
	private TabFolder tabFolder;
	private static Tree treeComponentDBView, treeInputsDBView, treeMathDBView;
	private Button buttonEditLinking;
	private static int[] columnWidthTableModelView;
	
	/**
	 * @param parent
	 */
	public ModelGUI(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(3, true));
		init();
	}

 	/**
	 * initialize the layout of the Model GUI
	 */ 	
	private void initLayout() {
		
		//set table on the left side of the tab model for the machine config
		tableModelView = new Table(this, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		tableModelView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tableModelView.setLinesVisible(true);
		tableModelView.setHeaderVisible(true);
		
		initTable(tableModelView);

		//set tabfolder on the right side of the tab model for the component DB and for the inputs
		tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		initTabCompDB(tabFolder);
		initTabInputs(tabFolder);
		initTabMath(tabFolder);
		
		//set button to edit linking on the buttom of the tab
		buttonEditLinking = new Button(this, SWT.NONE);
		buttonEditLinking.setText(LocalizationHandler.getItem("app.gui.model.editlink"));
		buttonEditLinking.pack();
		buttonEditLinking.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false, 2, 1));
        buttonEditLinking.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		//open window to set the IO linking
        		LinkingGUI.openLinkingGUI();
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		// Not used
        	}
        });
     }

 	/**
	 * initialize the table of the Model GUI
	 * @param tableModelView	the table to initialize with the columns to later add machine components
	 */ 	
	private void initTable(Table tableModelView){
		//set the titles of the columns of the table
		String[] titles =  {LocalizationHandler.getItem("app.gui.model.name"),
							LocalizationHandler.getItem("app.gui.model.type"),
							LocalizationHandler.getItem("app.gui.model.param"),
							"        ",
							"        ",
							"        "};
		for(int i=0; i < titles.length; i++){
			TableColumn column = new TableColumn(tableModelView, SWT.NULL);
			column.setText(titles[i]);
		}
		
        //initialize table with columns
        TableColumn[] columns = tableModelView.getColumns();
		columnWidthTableModelView = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
          columns[i].pack();
          columnWidthTableModelView[i] = 0;
        }
        
        //SOURCE http://www.tutorials.de/threads/in-editierbarer-swt-tabelle-ohne-eingabe-von-enter-werte-aendern.299858/
	    //create a TableCursor to navigate around the table
	    final TableCursor cursor = new TableCursor(tableModelView, SWT.NONE);
	    // create an editor to edit the cell when the user hits "ENTER"
	    // while over a cell in the table
	    final ControlEditor editor = new ControlEditor(cursor);
	    editor.grabHorizontal = true;
	    editor.grabVertical = true;
	   
	    cursor.addKeyListener(new KeyAdapter() {
	        public void keyPressed(KeyEvent e) {
	            switch(e.keyCode) {
		            case SWT.ARROW_UP:
		            case SWT.ARROW_RIGHT:
		            case SWT.ARROW_DOWN:
		            case SWT.ARROW_LEFT:
		            //an dieser stelle fehlen auch noch alle anderen tasten die
		            //ignoriert werden sollen...wie F1-12, esc,bsp,....
		                //System.out.println("Taste ignorieren...");
		                break;
		               
		            default:
		                //System.out.println("hier jetzt text editieren");
		                final Text text = new Text(cursor, SWT.NONE);
		                TableItem row = cursor.getRow();
		                int column = cursor.getColumn();
		                final String oldName = row.getText(column);
		                text.append(String.valueOf(e.character));
		                text.addKeyListener(new KeyAdapter() {
		                    public void keyPressed(KeyEvent e) {
		                        // close the text editor and copy the data over
		                        // when the user hits "ENTER"
		                        if (e.character == SWT.CR) {
		                        	TableItem row = cursor.getRow();
		                            int column = cursor.getColumn();
		                        	switch(column){
		                        	case 0:
		                        		row.setText(column, text.getText());
		                        		if(row.getText(1).contains("Input")){ //when handling a simulator
		                        			//change name of the simulator in the machine
		                        			Machine.renameInputObject(oldName, text.getText());
		                        			
		                        			//change the name of file that belongs to the given simulator
		                        			String prefix = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" +
		                            					    PropertiesHandler.getProperty("sim.MachineName") + "/" +
		                            					    "MachineConfig/" +
		                            					    PropertiesHandler.getProperty("sim.MachineConfigName") +
		                            					    "/";
		                        			String type   = row.getText(1).replace("Input ", "");
		                		    	    Path source = Paths.get(prefix, type + "_" + oldName + ".xml");
		                		    	    
		                		    	    //overwrite existing file, if exists
		                		    	    CopyOption[] options = new CopyOption[]{
		                		    	    	StandardCopyOption.REPLACE_EXISTING,
		                		    	    }; 
		                		    	    //try to rename the existing xml-file of the simulator to the new name
		                		    	    try {
		                		    	    	Files.move(source, source.resolveSibling(type + "_" + text.getText() + ".xml"), options);
		                					} catch (IOException ee) {
		                						ee.printStackTrace();
		                					}

		                        		} else { //when handling a machine component
		                        			Machine.renameMachineComponent(oldName, text.getText());
		                        		}
			                        	break;
		                        	}
			                        text.dispose();
		                        }
		                        // close the text editor when the user hits "ESC"
		                        if (e.character == SWT.ESC) {
		                            text.dispose();
		                        }
		                    }
		                });
		                editor.setEditor(text);
		                text.setFocus();
		                    break;
	            }  
	        }
	    });
	}        

 	/**
	 * initialize the tab folder on the right side of the Model GUI
	 * @param tabFolder	the tab folder to initialize
	 */ 	
	private void initTabCompDB(TabFolder tabFolder){
		treeComponentDBView = new Tree(tabFolder, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);

		//fill tree with the components existing in the directory
		MachineComponentHandler.fillMachineComponentTree(treeComponentDBView);
		
		//tab for tree item
		TabItem tabCompDBItem = new TabItem(tabFolder, SWT.NONE);
		tabCompDBItem.setText(LocalizationHandler.getItem("app.gui.model.comp"));
		tabCompDBItem.setToolTipText(LocalizationHandler.getItem("app.gui.model.comptooltip"));
		tabCompDBItem.setControl(treeComponentDBView);        
	}

 	/**
	 * update the component db in the tab folder on the right side of the model gui
	 */ 	
	public static void updateTabCompDB(){
		treeComponentDBView.removeAll();
		MachineComponentHandler.fillMachineComponentTree(treeComponentDBView);
	}
	
 	/**
	 * initialize the tab inputs in the tab folder on the right side of the Model GUI
	 * @param tabFolder	the tab folder to initialize
	 */ 	
	private void initTabInputs(TabFolder tabFolder){


		
		//TODO sizuest: create content of tabFolder Inputs
		treeInputsDBView = new Tree(tabFolder, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);

		MachineComponentHandler.fillInputsTree(treeInputsDBView);
		
		// Inputs
		//String[] items = {"ProcessSimulationControl", "StaticSimulationControl", "GeometricKienzleSimulationControl"};
		//for (String name:items){
		//	TreeItem child = new TreeItem(treeInputsDBView, SWT.NONE);
		//	child.setText(name);
		//}
		
		//tab for simulation config
		TabItem tabInputsItem = new TabItem(tabFolder, SWT.NONE);
		tabInputsItem.setText(LocalizationHandler.getItem("app.gui.model.inputs"));
		tabInputsItem.setToolTipText(LocalizationHandler.getItem("app.gui.model.inputstooltip"));
		tabInputsItem.setControl(treeInputsDBView);
	}
	
	private void initTabMath(TabFolder tabFolder){
		treeMathDBView = new Tree(tabFolder, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		
		
		
		TabItem tabMathItem = new TabItem(tabFolder, SWT.NONE);
		tabMathItem.setText(LocalizationHandler.getItem("app.gui.model.math"));
		tabMathItem.setToolTipText(LocalizationHandler.getItem("app.gui.model.mathtooltip"));
		tabMathItem.setControl(treeMathDBView);
	}
	
	/**
	 * initialize the component tree as drag source
	 * @param treeComponentDBView	the tree to set as drag source
	 */ 
	private void initInputDragSource(final Tree treeInputsDBView){
		//set tree as dragsource for the DnD of the components
		int operations = DND.DROP_COPY;
		final DragSource source = new DragSource(treeInputsDBView, operations);
		
		//SOURCE for drag source:
		//http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html
		
		//DnD shall transfer text of the selected element
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		source.setTransfer(types);
		
		//create draglistener to transfer text of selected tree element
		source.addDragListener(new DragSourceListener() {
			private TreeItem[] selection = null;
			
			//at drag start, get the selected tree element
			public void dragStart(DragSourceEvent event){
				selection = treeInputsDBView.getSelection();
			}
			
			//set the text of the selected tree element as event data
			public void dragSetData(DragSourceEvent event){
				String text = "";
				for(TreeItem item:selection){
					text += (String)item.getText();	
				}
				event.data = text;
			}
			
			//nothing needs to be done at the end of the drag
			public void dragFinished(DragSourceEvent event) {
				// Not used
			}
		});
	}
	
	
 	/**
	 * initialize the component tree as drag source
	 * @param treeComponentDBView	the tree to set as drag source
	 */ 
	private void initComponentDragSource(final Tree treeComponentDBView){
		//set tree as dragsource for the DnD of the components
		int operations = DND.DROP_COPY;
		final DragSource source = new DragSource(treeComponentDBView, operations);
		
		//SOURCE for drag source:
		//http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html
		
		//DnD shall transfer text of the selected element
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		source.setTransfer(types);
		
		//create draglistener to transfer text of selected tree element
		source.addDragListener(new DragSourceListener() {
			private TreeItem[] selection = null;
			
			//at drag start, get the selected tree element
			public void dragStart(DragSourceEvent event){
				selection = treeComponentDBView.getSelection();
				
				//if a category of components was selected (i.e. a parent of the tree), don't do anything
				for(TreeItem item:selection){
					if(item.getParentItem() == null){
						event.doit = false;
					}
				}
				
			}
			
			//set the text of the selected tree element as event data
			public void dragSetData(DragSourceEvent event){
				String text = "";
				for(TreeItem item:selection){
					
					text += (String)item.getParentItem().getText()+"_"+(String)item.getText();	
				}
				event.data = text;
			}
			
			//nothing needs to be done at the end of the drag
			public void dragFinished(DragSourceEvent event) {
				// Not used
			}
		});
	}

 	/**
	 * initialize the machine table as drop target
	 * @param tableModelView	the table to set as drop target
	 */ 		
	private void initDropTarget(final Table tableModelView){
		//set table as drop target
		int operations = DND.DROP_COPY;
		DropTarget target = new DropTarget(tableModelView, operations);
		
		//SOURCE for drop target:
		//http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html
		
		//only accept texttransfer
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] types = new Transfer[] {textTransfer};
		target.setTransfer(types);
		
		//add drop listener to the target
		target.addDropListener(new DropTargetListener(){
			//show copy icon at mouse pointer
			public void dragEnter(DropTargetEvent event){
				event.detail = DND.DROP_COPY;
			}
			public void dragOver(DropTargetEvent event){
				// Not used
			}
			public void dragLeave(DropTargetEvent event){
				// Not used
			}
			public void dragOperationChanged(DropTargetEvent event) {
				// Not used
			}
			public void dropAccept(DropTargetEvent event){
				// Not used
			}
			//only action is required when element is dropped
			public void drop(DropTargetEvent event){
				//collect string of drag
				String string = null;
		        string = (String) event.data;
		        
		        //get position of the drop		        
				Point p = event.display.map(null, tableModelView, event.x, event.y);
				TableItem dropItem = tableModelView.getItem(p);
				int index = dropItem == null ? tableModelView.getItemCount() : tableModelView.indexOf(dropItem);
		        
		        if (string.contains("SimulationControl")){
					//create new simulation control
		        	System.out.println("simulationcontrol " + string);
		        	
		    		final String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/";
		    		Path source = Paths.get(path + "/SimulationControl/" + string + "_Example.xml");
		    	    Path target = Paths.get(PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" +
                            				PropertiesHandler.getProperty("sim.MachineName") + "/" +
                            				"MachineConfig/" +
                            				PropertiesHandler.getProperty("sim.MachineConfigName") +
                            				"/", string + "_" + string + ".xml");
		    	    //overwrite existing file, if exists
		    	    CopyOption[] options = new CopyOption[]{
		    	    	StandardCopyOption.REPLACE_EXISTING,
		    	    	StandardCopyOption.COPY_ATTRIBUTES
		    	    }; 
		    	    try {
		    	    	Files.copy(source, target, options);
					} catch (IOException e) {
						e.printStackTrace();
					}
		        	
		        	
					final ASimulationControl sc = Machine.addNewInputObject(string, new SiUnit(Unit.NONE));
					
					//add the machine component to the table
					addTableItem(sc, index);
		        }
				
				else if(string.contains("ThermalTest")){
					return;
				}
				else if(string.contains("SimulationControl")){
					return;
				}
				
		        else{	        	
		        	final String[] split = string.split("_",2);
			        split[1] = split[1].replace(".xml","");
			        
			        //create new machine component out of component and type
			        final MachineComponent mc = Machine.addNewMachineComponent(split[0], split[1]);
			        
			        if(null == mc)
			        	return;
			        
			        //add the machine component to the table
			        addTableItem(mc, index);
		        }


			}
		});	
	}
	
	/**
	 * add new simulation control to the machine table
	 * @param sc	simulation control to add to the machine table
	 * @param index	position, where the component should be added to the table
	 */ 	
	public static void addTableItem(final ASimulationControl sc, int index){
		tableModelView.setRedraw(false);

        //create new tableitem in the tableModelView
        final TableItem item = new TableItem(tableModelView, SWT.NONE, index);
        
        //write Name, Type and Parameter to table
        item.setText(0, sc.getName());
        item.setText(1, "Input " + sc.getClass().getName().replace("ch.ethz.inspire.emod.simulation.", ""));
        for(int i=0; i<=1; i++){
            if(columnWidthTableModelView[i] < item.getBounds(i).x){
            	columnWidthTableModelView[i] = item.getBounds(i).x;
            }
        }
        
        //create combo to edit unit
        TableEditor editor = new TableEditor(tableModelView);
        final Combo comboEditInputUnit = new Combo(tableModelView, SWT.DROP_DOWN | SWT.SIMPLE);
        comboEditInputUnit.setLayoutData(new GridData(SWT.CENTER, SWT.RIGHT, true, true));
        
        String[] items = new String[SiUnitDefinition.getConversionMap().keySet().size()]; 
        SiUnitDefinition.getConversionMap().keySet().toArray(items);
        Arrays.sort(items);
        
        comboEditInputUnit.setItems(items);
        comboEditInputUnit.setText(sc.getUnit().toString());
        comboEditInputUnit.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent event){
				//disable comboMachineConfigName to prevent argument null for updatecomboMachineConfigName
				comboEditInputUnit.setEnabled(false);
    		
				sc.setUnit(new SiUnit(comboEditInputUnit.getText()));
    			//enable comboMachineConfigName after update
				comboEditInputUnit.setEnabled(true);
    		}
    		public void widgetDefaultSelected(SelectionEvent event){
    			// Not used
    		}
    	});
        
        comboEditInputUnit.addListener(SWT.MouseWheel, new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.doit = false;
				tableModelView.getVerticalBar().setSelection( tableModelView.getVerticalBar().getSelection() - 
															  (int) Math.signum(event.count)*tableModelView.getVerticalBar().getIncrement() );
			}
		});
        
        comboEditInputUnit.pack();
        editor.minimumWidth = comboEditInputUnit.getSize().x;
        if(columnWidthTableModelView[2] < comboEditInputUnit.getSize().x){
        	columnWidthTableModelView[2] = comboEditInputUnit.getSize().x;
        }
        editor.grabHorizontal = true;
        editor.horizontalAlignment = SWT.RIGHT;
        editor.setEditor(comboEditInputUnit, item, 2);
        
        //System.out.println("********** " + editor.getColumn());
        
        if(tableModelView.getColumn(editor.getColumn()).getWidth() < comboEditInputUnit.getSize().x){
        	tableModelView.getColumn(editor.getColumn()).setWidth(comboEditInputUnit.getSize().x);
        }
        
        //create button to edit component
        editor = new TableEditor(tableModelView);
        final Button buttonEditComponent = new Button(tableModelView, SWT.PUSH);
        Image imageEdit = new Image(Display.getDefault(), "src/resources/Edit16.gif");
        buttonEditComponent.setImage(imageEdit);
        buttonEditComponent.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		//open tab Simulation --> inputs
        		//EModGUI.tabFolder.setSelection(1);
        		//SimGUI.tabFolder.setSelection(3);
        		
        		String type = item.getText(1).replace("Input ", "");
        		String name = item.getText(0);
        		//open window editComponentEditGUI with the selected component
				EditInputGUI.editInputGUI(type, name);
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		// Not used
        	}
        });
        buttonEditComponent.pack();
        editor.minimumWidth = buttonEditComponent.getSize().x;
        editor.horizontalAlignment = SWT.RIGHT;
        editor.setEditor(buttonEditComponent, item, 3);
        
        
        //create button to delete component in last column
        editor = new TableEditor(tableModelView);
        final Button buttonDeleteComponent = new Button(tableModelView, SWT.PUSH);
        
        Image imageDelete = new Image(Display.getDefault(), "src/resources/Delete16.gif");
        buttonDeleteComponent.setImage(imageDelete);
        //buttonDeleteComponent.setText(LocalizationHandler.getItem("app.gui.model.delcomp"));
 
        buttonDeleteComponent.addSelectionListener(new SelectionListener(){	
        	//action when button delete component is pressed
        	public void widgetSelected(SelectionEvent event){
        		tableModelView.setRedraw(false);
	    		
        		
        		//remove component from machine
        		if(Machine.removeInputObject(sc.getName())){
	        		//delete the according file
        			String prefix = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" +
    					    PropertiesHandler.getProperty("sim.MachineName") + "/" +
    					    "MachineConfig/" +
    					    PropertiesHandler.getProperty("sim.MachineConfigName") +
    					    "/";
        			String type   = item.getText(1).replace("Input ", "");
        			Path file = Paths.get(prefix, type + "_" + item.getText(0) + ".xml");
        			
        			try {
						Files.delete(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
        			//dispose the item
	        		item.dispose();
        		}
        		
        		//resize columns of table
        		updateTable();
	    		tableModelView.setRedraw(true);
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		// Not used
        	}
        });
        buttonDeleteComponent.pack();
        editor.minimumWidth = buttonDeleteComponent.getSize().x;
        editor.horizontalAlignment = SWT.RIGHT;
        editor.setEditor(buttonDeleteComponent, item, 4);		        
        
        //if item is disposed, remove buttons delete/edit and combo
        item.addDisposeListener(new DisposeListener(){
        	public void widgetDisposed(DisposeEvent e) {
        		buttonDeleteComponent.dispose();
        		buttonEditComponent.dispose();
        		comboEditInputUnit.dispose();
        	}	
        });
        
        //write table and resize columns
        //updateTable();
        tableModelView.setRedraw(true);
	}
	
 	/**
	 * add new machine component to the machine table
	 * @param mc	Machine Component to add to the machine table
	 * @param index	position, where the component should be added to the table
	 */ 	
	public static void addTableItem(final MachineComponent mc, int index){
		tableModelView.setRedraw(false);

        //create new tableitem in the tableModelView
        final TableItem item = new TableItem(tableModelView, SWT.NONE, index);
        
        //write Name, Type and Parameter to table
        item.setText(0, mc.getName());
        item.setText(1, mc.getComponent().getModelType());
        item.setText(2, mc.getComponent().getType());
        for(int i=0; i<=2; i++){
            if(columnWidthTableModelView[i] < item.getBounds(i).x){
            	columnWidthTableModelView[i] = item.getBounds(i).x;
            }
        }
        
        //set combo to let the user choose the parameter type of the mc
        TableEditor editor = new TableEditor(tableModelView);
        final Combo comboComponentType = new Combo(tableModelView, SWT.DROP_DOWN | SWT.SIMPLE);
        comboComponentType.setLayoutData(new GridData(SWT.CENTER, SWT.RIGHT, true, true));
        
		//according to the given component, get the path for the parameter sets
		String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/" + mc.getComponent().getModelType() + "/";
		File subdir = new File(path);
    	
    	//check if the directory exists, then show possible parameter sets to select
    	if(subdir.exists()){
    		String[] subitems = subdir.list();
    		
    		//remove the "Type_" and the ".xml" part of the filename
    		for(int i=0; i < subitems.length; i++){
    			subitems[i] = subitems[i].replace(mc.getComponent().getModelType() + "_", "");
    			subitems[i] = subitems[i].replace(".xml", "");
    		}   
    		
    		//sort by name
    		Arrays.sort(subitems);
    		
    		//set the possible parameter sets to the combo
    		comboComponentType.setItems(subitems);
    		comboComponentType.setText(mc.getComponent().getType());
    	}
    	
    	comboComponentType.addSelectionListener(new SelectionListener(){
    		public void widgetSelected(SelectionEvent event){
				//disable comboMachineConfigName to prevent argument null for comboComponentType
    			comboComponentType.setEnabled(false);
    		
    			//change the component to the new type, save machine
    			Machine.getMachineComponent(mc.getName()).getComponent().setType(comboComponentType.getText());
    			item.setText(2, comboComponentType.getText());
    			//Machine.saveMachine(PropertiesHandler.getProperty("sim.MachineName"), PropertiesHandler.getProperty("sim.MachineConfigName"));
    			comboComponentType.setEnabled(true);
    		}
    		public void widgetDefaultSelected(SelectionEvent event){
    			// Not used
    		}
    	});
    	
    	comboComponentType.addListener(SWT.MouseWheel, new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.doit = false;
				tableModelView.getVerticalBar().setSelection( tableModelView.getVerticalBar().getSelection() - 
						  (int) Math.signum(event.count)*tableModelView.getVerticalBar().getIncrement() );
			}
		});
    	
    	comboComponentType.pack();
        editor.minimumWidth = comboComponentType.getSize().x;
        if(columnWidthTableModelView[2] < comboComponentType.getSize().x){
        	columnWidthTableModelView[2] = comboComponentType.getSize().x;
        }
        editor.grabHorizontal = true;
        editor.horizontalAlignment = SWT.RIGHT;
        editor.setEditor(comboComponentType, item, 2);
        
        //create button to edit component
        final Shell parent = tableModelView.getShell();
        editor = new TableEditor(tableModelView);
        final Button buttonEditComponent = new Button(tableModelView, SWT.PUSH);
        Image imageEdit = new Image(Display.getDefault(), "src/resources/Edit16.gif");
        buttonEditComponent.setImage(imageEdit);
        buttonEditComponent.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){      		
        		String model = item.getText(1);
        		String type  = item.getText(2);
        		//open window editComponentEditGUI with the selected component
        		EditMachineComponentGUI.editMachineComponentGUI(parent, model, type);
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		// Not used
        	}
        });
        buttonEditComponent.pack();
        editor.minimumWidth = buttonEditComponent.getSize().x;
        editor.horizontalAlignment = SWT.RIGHT;
        editor.setEditor(buttonEditComponent, item, 3);
        
        //create button to delete component in last column
        editor = new TableEditor(tableModelView);
        final Button buttonDeleteComponent = new Button(tableModelView, SWT.PUSH);
        
        //Image imageDelete = Display.getDefault().getSystemImage(SWT.ICON_ERROR);
        Image imageDelete = new Image(Display.getDefault(), "src/resources/Delete16.gif");
        buttonDeleteComponent.setImage(imageDelete);
        //buttonDeleteComponent.setText(LocalizationHandler.getItem("app.gui.model.delcomp"));
        
        buttonDeleteComponent.addSelectionListener(new SelectionListener(){	
        	//action when button delete component is pressed
        	public void widgetSelected(SelectionEvent event){
        		tableModelView.setRedraw(false);
	    		
        		//remove component from machine
        		if(Machine.removeMachineComponent(mc.getName())){

	        		//dispose the buttons for edit and delete, and dispose the item
        			comboComponentType.dispose();
	        		buttonDeleteComponent.dispose();
	        		buttonEditComponent.dispose();
	        		item.dispose();
        		}
        		
        		//resize columns of table
        		updateTable();
	    		tableModelView.setRedraw(true);
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		// Not used
        	}
        });
        buttonDeleteComponent.pack();
        editor.minimumWidth = buttonDeleteComponent.getSize().x;
        if(columnWidthTableModelView[4] < buttonDeleteComponent.getSize().x){
        	columnWidthTableModelView[4] = buttonDeleteComponent.getSize().x;
        }
        editor.horizontalAlignment = SWT.RIGHT;
        editor.setEditor(buttonDeleteComponent, item, 4);		        
        
        //if item is disposed, remove button delete and button edit
        item.addDisposeListener(new DisposeListener(){
        	public void widgetDisposed(DisposeEvent e) {
        		comboComponentType.dispose();
        		buttonDeleteComponent.dispose();
        		//buttonEditComponent.dispose();
        	}	
        });
        
        //write table and resize columns
        updateTable();
        tableModelView.setRedraw(true);
	}

 	/**
	 * reset the width of all columns of the machine table
	 * needed after deleting all items
	 */ 	
	public static void updateTable(){
		tableModelView.setRedraw(false);
        TableColumn[] columns = tableModelView.getColumns();
        for (int i = 0; i < columns.length; i++) {
          columns[i].pack();
        }
        
        //workaround: width is not correct for the TableEditors when .pack();
        for(int i = 2; i <= 4; i++){
        	if(columnWidthTableModelView[i] > 0){
        		tableModelView.getColumn(i).setWidth(columnWidthTableModelView[i]+20);
        	}
        }
        
        tableModelView.setRedraw(true);
	}

 	/**
	 * clear all items of the machine table
	 */ 	
	public static void clearTable() {
		tableModelView.setRedraw(false);

		//remove all components from machine and table
		tableModelView.removeAll();
		Machine.clearMachine();
		
		//resize columns of table
		updateTable();
		tableModelView.setRedraw(true);
	}

	@Override
	public void init() {
		initLayout();
		initComponentDragSource(treeComponentDBView);
		initInputDragSource(treeInputsDBView);
		initDropTarget(tableModelView);		
	}

	@Override
	public void update() {
		updateTabCompDB();	
		initTabCompDB(tabFolder);
	}
}