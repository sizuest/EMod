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


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import ch.ethz.inspire.emod.model.units.Unit;
import ch.ethz.inspire.emod.simulation.ASimulationControl;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * @author manick
 *
 */

public class ModelGUI extends AGUITab {
	
	private Text textModelTitel;
	private static Table tableModelView;
	private TabFolder tabFolder;
	private static Tree treeComponentDBView, treeInputsDBView;
	private Button buttonEditLinking;
	
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
		//set title of the tab machine config
		textModelTitel = new Text(this, SWT.MULTI);
		textModelTitel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 3, 1));
		textModelTitel.setText(LocalizationHandler.getItem("app.gui.tabs.machtooltip"));
		
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
		
		//set button to edit linking on the buttom of the tab
		buttonEditLinking = new Button(this, SWT.NONE);
		buttonEditLinking.setText(LocalizationHandler.getItem("app.gui.model.editlink"));
		buttonEditLinking.pack();
		buttonEditLinking.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false, 2, 1));
        buttonEditLinking.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		//open window to set the IO linking
        		LinkingGUI linkingGUI = new LinkingGUI();
        		linkingGUI.openLinkingGUI();
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		
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
							LocalizationHandler.getItem("app.gui.model.editcomp"),
							//LocalizationHandler.getItem("app.gui.model.editlink"),
							LocalizationHandler.getItem("app.gui.model.delcomp")};
		for(int i=0; i < titles.length; i++){
			TableColumn column = new TableColumn(tableModelView, SWT.NULL);
			column.setText(titles[i]);
		}
		
        //initialize table with columns
        TableColumn[] columns = tableModelView.getColumns();
        for (int i = 0; i < columns.length; i++) {
          columns[i].pack();
        }
	}

 	/**
	 * initialize the tab folder on the right side of the Model GUI
	 * @param tabFolder	the tab folder to initialize
	 */ 	
	private void initTabCompDB(TabFolder tabFolder){
		treeComponentDBView = new Tree(tabFolder, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);

		//fill tree with the components existing in the directory
		MachineComponentHandler.fillTree(treeComponentDBView);
		
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
		MachineComponentHandler.fillTree(treeComponentDBView);
	}
	
 	/**
	 * initialize the tab inputs in the tab folder on the right side of the Model GUI
	 * @param tabFolder	the tab folder to initialize
	 */ 	
	private void initTabInputs(TabFolder tabFolder){
			
		//TODO sizuest: create content of tabFolder Inputs
		treeInputsDBView = new Tree(tabFolder, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		
		// Inputs
		String[] items = {"ProcessSimulationControl", "StaticSimulationControl", "GeometricKienzleSimulationControl"};
		for (String name:items){
			TreeItem child = new TreeItem(treeInputsDBView, SWT.NONE);
			child.setText(name);
		}
		
		//tab for simulation config
		TabItem tabInputsItem = new TabItem(tabFolder, SWT.NONE);
		tabInputsItem.setText(LocalizationHandler.getItem("app.gui.model.inputs"));
		tabInputsItem.setToolTipText(LocalizationHandler.getItem("app.gui.model.inputstooltip"));
		tabInputsItem.setControl(treeInputsDBView);
	}
	
	/**
	 * initialize the component tree as drag source
	 * @param treeComponentDBView	the tree to set as drag source
	 */ 
	private void initInputDragSource(final Tree treeDBView){
		//set tree as dragsource for the DnD of the components
		int operations = DND.DROP_COPY;
		final DragSource source = new DragSource(treeDBView, operations);
		
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
				selection = treeDBView.getSelection();
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
					text += (String)item.getText();	
				}
				event.data = text;
			}
			
			//nothing needs to be done at the end of the drag
			public void dragFinished(DragSourceEvent event) {
			
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

			}
			public void dragLeave(DropTargetEvent event){
				
			}
			public void dragOperationChanged(DropTargetEvent event) {
				
			}
			public void dropAccept(DropTargetEvent event){
				
			}
			//only action is required when element is dropped
			public void drop(DropTargetEvent event){
				//collect string of drag, split into type and parameter
				String string = null;
		        string = (String) event.data;
		        
		        //get position of the drop		        
				Point p = event.display.map(null, tableModelView, event.x, event.y);
				TableItem dropItem = tableModelView.getItem(p);
				int index = dropItem == null ? tableModelView.getItemCount() : tableModelView.indexOf(dropItem);
		        
		        //if (string.contains("SimulationControl")){
				if (string.contains("ProcessSimulationControl")){
					
					//create new machine component out of component and type
					final ASimulationControl sc = Machine.addNewInputObject(string, Unit.NONE);
					
					//add the machine component to the table
					addTableItem(sc, index);
		        }
				
				else if(string.contains("ThermalTest")){
					return;
				}
				else if(string.contains("StaticSimulation")){
					return;
				}
				else if(string.contains("GeometricKienzle")){
					return;
				}
				
		        else{	        	
		        	final String[] split = string.split("_",2);
			        split[1] = split[1].replace(".xml","");
			        
			        //create new machine component out of component and type
			        final MachineComponent mc = Machine.addNewMachineComponent(split[0], split[1]);
			        
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
        item.setText(1, "Input");
        item.setText(2, "");
        
        //create button to edit component
        TableEditor editor = new TableEditor(tableModelView);
        final CCombo comboEditImputUnit = new CCombo(tableModelView, SWT.PUSH);
        
        String[] items = new String[Unit.values().length];
        for(int i=0; i<items.length; i++){
        	items[i] = Unit.values()[i].toString();
        }
        comboEditImputUnit.setItems(items);
        comboEditImputUnit.setText(sc.getUnit().toString());
        comboEditImputUnit.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent event){
				//disable comboMachineConfigName to prevent argument null for updatecomboMachineConfigName
				comboEditImputUnit.setEnabled(false);
    		
				sc.setUnit(Unit.valueOf(comboEditImputUnit.getText()));
    		
    			//enable comboMachineConfigName after update
    			comboEditImputUnit.setEnabled(true);
    		}
    		public void widgetDefaultSelected(SelectionEvent event){
    		
    		}
    	});
        comboEditImputUnit.pack();
        editor.minimumWidth = comboEditImputUnit.getSize().x;
        editor.horizontalAlignment = SWT.LEFT;
        editor.setEditor(comboEditImputUnit, item, 3);
        
        //create button to delete component in last column
        editor = new TableEditor(tableModelView);
        final Button buttonDeleteComponent = new Button(tableModelView, SWT.PUSH);
        
        //TODO manick: edit image!
        //Image imageDelete = Display.getDefault().getSystemImage(SWT.ICON_ERROR);
        Image imageDelete = new Image(Display.getDefault(), "src/resources/Delete16.gif");
        buttonDeleteComponent.setImage(imageDelete);
        //buttonDeleteComponent.setText(LocalizationHandler.getItem("app.gui.model.delcomp"));
 
        buttonDeleteComponent.addSelectionListener(new SelectionListener(){	
        	//action when button delete component is pressed
        	public void widgetSelected(SelectionEvent event){
        		tableModelView.setRedraw(false);
	    		
        		
        		//remove component from machine
        		if(Machine.removeInputObject(sc.getName())){
	        		//dispose the buttons for edit and delete, and dispose the item
	        		buttonDeleteComponent.dispose();
	        		comboEditImputUnit.dispose();
	        		item.dispose();
        		}
        		
        		//resize columns of table
        		updateTable();
	    		tableModelView.setRedraw(true);
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		
        	}
        });
        buttonDeleteComponent.pack();
        editor.minimumWidth = buttonDeleteComponent.getSize().x;
        editor.horizontalAlignment = SWT.LEFT;
        editor.setEditor(buttonDeleteComponent, item, 4);		        
        
        //if item is disposed, remove button delete and button edit
        item.addDisposeListener(new DisposeListener(){
        	public void widgetDisposed(DisposeEvent e) {
        		buttonDeleteComponent.dispose();
        		comboEditImputUnit.dispose();
        	}	
        });
        
        //write table and resize columns
        updateTable();
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
        
        //create button to edit component
        TableEditor editor = new TableEditor(tableModelView);
        final Button buttonEditComponent = new Button(tableModelView, SWT.PUSH);
        
        //TODO manick: edit image!
        Image imageEdit = new Image(Display.getDefault(), "src/resources/Edit16.gif");
        buttonEditComponent.setImage(imageEdit);
        //buttonEditComponent.setText(LocalizationHandler.getItem("app.gui.model.editcomp"));
        
        buttonEditComponent.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		//open new componenteditgui with the current machine component
        		EditMachineComponentGUI componentEditGUI = new EditMachineComponentGUI();
        		componentEditGUI.openMachineComponentGUI(mc, item);
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		
        	}
        });
        buttonEditComponent.pack();
        editor.minimumWidth = buttonEditComponent.getSize().x;
        editor.horizontalAlignment = SWT.LEFT;
        editor.setEditor(buttonEditComponent, item, 3);
        
        //create button to delete component in last column
        editor = new TableEditor(tableModelView);
        final Button buttonDeleteComponent = new Button(tableModelView, SWT.PUSH);
        
        //TODO manick: edit image!
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
	        		buttonDeleteComponent.dispose();
	        		buttonEditComponent.dispose();
	        		item.dispose();
        		}
        		
        		//resize columns of table
        		updateTable();
	    		tableModelView.setRedraw(true);
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		
        	}
        });
        buttonDeleteComponent.pack();
        editor.minimumWidth = buttonDeleteComponent.getSize().x;
        editor.horizontalAlignment = SWT.LEFT;
        editor.setEditor(buttonDeleteComponent, item, 4);		        
        
        //if item is disposed, remove button delete and button edit
        item.addDisposeListener(new DisposeListener(){
        	public void widgetDisposed(DisposeEvent e) {
        		buttonDeleteComponent.dispose();
        		buttonEditComponent.dispose();
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
        TableColumn[] columns = tableModelView.getColumns();
        for (int i = 0; i < columns.length; i++) {
          columns[i].pack();
        }
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
		initDropTarget(tableModelView);
		initInputDragSource(treeInputsDBView);		
	}

	@Override
	public void update() {
		updateTabCompDB();	
	}
}