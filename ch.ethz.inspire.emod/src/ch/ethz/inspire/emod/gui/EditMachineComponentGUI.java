package ch.ethz.inspire.emod.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.gui.dd.DuctDesignGUI;
import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.utils.ComponentConfigReader;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * General GUI to Edit Machine Components
 * 
 * @author sizuest
 *
 */
public class EditMachineComponentGUI {

    private Shell shell;
    private static Table tableComponent;

    /**
     * EditMachineComponentGUI
     */
    public EditMachineComponentGUI(){}

 	/**
	 * Component Edit GUI for creating a new Component
	 */
    public void newMachineComponentGUI(){
    	shell = new Shell(Display.getCurrent());
        shell.setText(LocalizationHandler.getItem("app.gui.compdb.newcomp"));
    	shell.setLayout(new GridLayout(2, false));

		//Text "Type" of the Component
		Text textComponentType = new Text(shell, SWT.READ_ONLY);
		textComponentType.setText(LocalizationHandler.getItem("app.gui.model.type") + ":");
		textComponentType.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, 1));
		
		//Combo to select Value of "Type" of the Component
		final Combo comboComponentTypeValue = new Combo(shell, SWT.NONE);
		
		//according to the given component, get the path for the parameter sets
		final String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/";
		File subdir = new File(path);
    	
    	//check if the directory exists, then show possible parameter sets to select
    	if(subdir.exists()){
    		String[] subitems = subdir.list();
    		comboComponentTypeValue.setItems(subitems);
    	}
		
		comboComponentTypeValue.setText(LocalizationHandler.getItem("app.gui.compdb.selecttype"));
		comboComponentTypeValue.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, 1));
		
		//Text "Model Type" of the Component
		Text textComponentModelType = new Text(shell, SWT.READ_ONLY);
		textComponentModelType.setText(LocalizationHandler.getItem("app.gui.compdb.compname"));
		textComponentModelType.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, 1));
		
		//Combo to let the user select the desired model type of the Component
		final Text textComponentModelTypeValue = new Text(shell, SWT.NONE);
		textComponentModelTypeValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

    	//button to continue
		Button buttonContinue = new Button(shell, SWT.NONE);
		buttonContinue.setText(LocalizationHandler.getItem("app.gui.continue"));
		//selection Listener for the button, actions when button is pressed
		buttonContinue.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent event){
	    		String stringCompTypeValue = comboComponentTypeValue.getText();
	    		String stringCompParamValue = textComponentModelTypeValue.getText();
	    		
	    		//copy the example type of the selected component and create a copy
	    		//SOURCE for the file copy: http://www.javapractices.com/topic/TopicAction.do?Id=246 
	    		Path from = Paths.get(path + stringCompTypeValue + "/" + stringCompTypeValue + "_Example.xml");
	    	    Path to = Paths.get(path + stringCompTypeValue + "/" + stringCompTypeValue + "_" + stringCompParamValue + ".xml");
	    	    //overwrite existing file, if exists
	    	    CopyOption[] options = new CopyOption[]{
	    	      StandardCopyOption.REPLACE_EXISTING,
	    	      StandardCopyOption.COPY_ATTRIBUTES
	    	    }; 
	    	    try {
					Files.copy(from, to, options);
				} catch (IOException e) {
					e.printStackTrace();
				}
	    		
	    		shell.close();
	    		
	    		//open the edit ComponentEditGUI with the newly created component file
	    		editMachineComponentGUI(stringCompTypeValue, stringCompParamValue);
	    	}
	    	public void widgetDefaultSelected(SelectionEvent event){
	    		
	    	}
	    });
		buttonContinue.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true, 2, 1));
		
		shell.pack();

		//width and height of the shell
		Rectangle rect = shell.getBounds();
		int[] size = {0, 0};
		size[0] = rect.width;
		size[1] = rect.height;
		
		//position the shell into the middle of the last window
        int[] position;
        position = EModGUI.shellPosition();
        shell.setLocation(position[0]-size[0]/2, position[1]-size[1]/2);
		
        //open the new shell
		shell.open();
    }
   
    
 	/**
	 * Component Edit GUI for editing a existing Component of the Component DB
 	 * @param type 
 	 * @param parameter 
	 */
    public void editMachineComponentGUI(final String type, final String parameter){
    	shell = new Shell(Display.getCurrent());
        shell.setText(LocalizationHandler.getItem("app.gui.compdb.editcomp"));
    	shell.setLayout(new GridLayout(1, false));
    	
    	tableComponent = new Table(shell, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
    	tableComponent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    	tableComponent.setLinesVisible(true);
    	tableComponent.setHeaderVisible(true);
    	
    	String[] titles =  {LocalizationHandler.getItem("app.gui.compdb.property"),
    						LocalizationHandler.getItem("app.gui.compdb.value"),
    						LocalizationHandler.getItem("app.gui.compdb.unit"),
							LocalizationHandler.getItem("app.gui.compdb.description")};
		for(int i=0; i < titles.length; i++){
			TableColumn column = new TableColumn(tableComponent, SWT.NULL);
			column.setText(titles[i]);
		}
		
		//SOURCE http://www.tutorials.de/threads/in-editierbarer-swt-tabelle-ohne-eingabe-von-enter-werte-aendern.299858/
	    //create a TableCursor to navigate around the table
	    final TableCursor cursor = new TableCursor(tableComponent, SWT.NONE);
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
		                text.append(String.valueOf(e.character));
		                text.addKeyListener(new KeyAdapter() {
		                    public void keyPressed(KeyEvent e) {
		                        // close the text editor and copy the data over
		                        // when the user hits "ENTER"
		                        if (e.character == SWT.CR) {
		                        	TableItem row = cursor.getRow();
		                            int column = cursor.getColumn();
		                        	switch(column){
		                        	case 1:
		                        		row.setText(column, text.getText());
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
		
		TableItem itemType = new TableItem(tableComponent, SWT.NONE, 0);
		itemType.setText(0, LocalizationHandler.getItem("app.gui.model.type") + ":");
		itemType.setText(1, type);
		
		TableItem itemName = new TableItem(tableComponent, SWT.NONE, 1);
		itemName.setText(0, LocalizationHandler.getItem("app.gui.model.param") + ":");
		itemName.setText(1, parameter);
		
		
		/* Create Component reader */
		final ComponentConfigReader props;
		try {
			props = new ComponentConfigReader(type, parameter);
		} catch (Exception e) {
			System.err.println("Failed to open Parameter set'"+type+":"+parameter+"'");
			e.printStackTrace();
			return;
		}
		/*
		try {
			props.loadFromXML(iostream);
		}
		catch (Exception e) {
			e.printStackTrace();
		}*/
		
		//prepare text and styledtext widgets to show the key and values of the properties
		
	
		for(String key: props.getKeys()){
			
			try{
				String value = props.getString(key);
				
				int i                    = tableComponent.getItemCount();
				final TableItem itemProp = new TableItem(tableComponent, SWT.NONE, i);
				TableEditor editorButton = new TableEditor(tableComponent);
				
				itemProp.setText(0, key);
				itemProp.setText(1, value);
				
				/* SPECIAL CASE: Material */
				if(key.matches("[a-zA-Z]*Material")){
					final Button selectMaterialButton = new Button(tableComponent, SWT.PUSH);
					selectMaterialButton.setText("...");
					selectMaterialButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
					selectMaterialButton.addSelectionListener(new SelectionListener(){
			        	public void widgetSelected(SelectionEvent event){
			        		SelectMaterialGUI matGUI = new SelectMaterialGUI();
			        		try{
			        			matGUI.getSelectionToTable(itemProp.getClass().getDeclaredMethod("setText", String.class), itemProp);
			        		}
			        		catch (Exception e){
			        			e.printStackTrace();
			        		}
			        	}
			        	public void widgetDefaultSelected(SelectionEvent event){
			        		
			        	}
			        });
					selectMaterialButton.pack();
					editorButton.minimumWidth = selectMaterialButton.getSize().x;
					editorButton.horizontalAlignment = SWT.LEFT;
			        editorButton.setEditor(selectMaterialButton, itemProp, 2);
				}
				/* SPECIAL CASE: Model */
				else if(key.matches("[a-zA-Z]+Type")){
					final String mdlType = key.replace("Type", "");
					final Button selectMaterialButton = new Button(tableComponent, SWT.PUSH);
					selectMaterialButton.setText("...");
					selectMaterialButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
					selectMaterialButton.addSelectionListener(new SelectionListener(){
			        	public void widgetSelected(SelectionEvent event){
			        		openModelSelectGUI(type, itemProp);
			        	}
			        	public void widgetDefaultSelected(SelectionEvent event){
			        		
			        	}
			        });
					selectMaterialButton.pack();
					editorButton.minimumWidth = selectMaterialButton.getSize().x;
					editorButton.horizontalAlignment = SWT.LEFT;
			        editorButton.setEditor(selectMaterialButton, itemProp, 2);
				}
				
				/* SPECIAL CASE: Duct */
				else if(key.matches("[a-zA-Z]+Duct")){
					final String name = value;
					final Button editDuctButton = new Button(tableComponent, SWT.PUSH);
					editDuctButton.setText("...");
					editDuctButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
					editDuctButton.addSelectionListener(new SelectionListener(){
			        	public void widgetSelected(SelectionEvent event){
			        		DuctDesignGUI ductGUI= new DuctDesignGUI();
			        		ductGUI.editDuctGUI(type, parameter,  name);			        		
			        	}
			        	public void widgetDefaultSelected(SelectionEvent event){
			        		
			        	}
			        });
					editDuctButton.pack();
					editorButton.minimumWidth = editDuctButton.getSize().x;
					editorButton.horizontalAlignment = SWT.LEFT;
			        editorButton.setEditor(editDuctButton, itemProp, 2);
				}
			}
			catch(Exception e){
				System.err.println("Failed to load Property '"+key+"' from '"+type+":"+parameter+"'");
				e.printStackTrace();
			}
		}
		
		TableColumn[] columns = tableComponent.getColumns();
        for (int j = 0; j < columns.length; j++) {
          columns[j].pack();
        }
		
    	//button to save
		Button buttonSave = new Button(shell, SWT.NONE);
		buttonSave.setText(LocalizationHandler.getItem("app.gui.save"));
		//selection Listener for the button, actions when button is pressed
		buttonSave.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent event){
	    		
	    		TableItem[] columns = tableComponent.getItems();
	    		
	    		try {
		    		for(int i=2; i<columns.length; i++){
		    			props.setValue( columns[i].getText(0), columns[i].getText(1));
		    		}
	    		}
	    		catch(Exception e){
	    			System.err.println("Failed to write parameter file");
	    		}
	    		
	    		//update the component DB on the right hand side of the model gui tabel
	    		ModelGUI.updateTabCompDB();
	    		
	    		props.Close();
	    		
	    		shell.close();
	    	}
	    	public void widgetDefaultSelected(SelectionEvent event){
	    		
	    	}
	    });
		buttonSave.setLayoutData(new GridData(SWT.END, SWT.TOP, true, true, 2, 1));
		
		shell.pack();

		//width and height of the shell
		Rectangle rect = shell.getBounds();
		int[] size = {0, 0};
		size[0] = rect.width;
		size[1] = rect.height;
		
		//position the shell into the middle of the last window
        int[] position;
        position = EModGUI.shellPosition();
        shell.setLocation(position[0]-size[0]/2, position[1]-size[1]/2);
		
        //open the new shell
		shell.open();
    }
    
 	/**
	 * open Component Edit GUI for Component, that already exists in the machine configuration 
	 * 
	 * @param mc	Machine component which should be edited
	 * @param item	table item in which the machine component is stored
	 */
    public void openMachineComponentGUI(final MachineComponent mc, final TableItem item){
        	shell = new Shell(Display.getCurrent());
	        shell.setText(LocalizationHandler.getItem("app.gui.compdb.editcomp"));
	    	shell.setLayout(new GridLayout(2, false));

	    	//Text "Name" of the Component
			Text textComponentName = new Text(shell, SWT.READ_ONLY);
			textComponentName.setText(LocalizationHandler.getItem("app.gui.model.name") + ":");
			textComponentName.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, true, 1, 1));;
			
			//Textfield to enter the Name of the Component
			final Text textComponentNameValue = new Text(shell, SWT.NONE);
			textComponentNameValue.setText(mc.getName());
			textComponentNameValue.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
			
			//Text "Type" of the Component
			Text textComponentType = new Text(shell, SWT.READ_ONLY);
			textComponentType.setText(LocalizationHandler.getItem("app.gui.model.type") + ":");
			textComponentType.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, true, 1, 1));
			
			//Text Value of "Type" of the Component
			Text textCompTypeValue = new Text(shell, SWT.READ_ONLY);
			textCompTypeValue.setText(mc.getComponent().getClass().toString().replace("class ch.ethz.inspire.emod.model.",""));
			textCompTypeValue.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, true, 1, 1));
			
			//Text model type of the Component
			Text textComponentModelType = new Text(shell, SWT.READ_ONLY);
			textComponentModelType.setText(LocalizationHandler.getItem("app.gui.model.param") + ":");
			textComponentModelType.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, true, 1, 1));
			
			//Combo to let the user select the desired Parameter-set of the Component
			final Combo comboComponentModelTypeValue = new Combo(shell, SWT.NONE);
			comboComponentModelTypeValue.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
			
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
	    		
	    		//set the possible parameter sets to the combo
	    		comboComponentModelTypeValue.setItems(subitems);
	    		comboComponentModelTypeValue.setText(mc.getComponent().getType());
	    	}
	    				
	    	//button to save the selection
			Button buttonSave = new Button(shell, SWT.NONE);
			buttonSave.setText(LocalizationHandler.getItem("app.gui.save"));
			//selection Listener for the button, actions when button is pressed
			buttonSave.addSelectionListener(new SelectionListener(){
		    	public void widgetSelected(SelectionEvent event){
		    		//get the entered name in the textfield and set it to the MachineComponents name
		    		mc.setName(textComponentNameValue.getText());
		    		//set the Name into the table item
		    		item.setText(0, mc.getName());
		    		
		    		//get the selected name of the parameter in the combo and set it to the MachineComponent
		    		mc.getComponent().setType(comboComponentModelTypeValue.getText());
		    		//set the name into the table item
		    		item.setText(2, mc.getComponent().getType());

		    		//update the table in the modelGUI
		    		ModelGUI.updateTable();
		    		
		    		//close the Component Edit GUI
		    		closeMachineComponentGUI();
		    	}
		    	public void widgetDefaultSelected(SelectionEvent event){
		    		
		    	}
		    });
			buttonSave.setLayoutData(new GridData(SWT.END, SWT.TOP, true, true, 2, 1));
			
			shell.pack();

			//width and height of the shell
			Rectangle rect = shell.getBounds();
			int[] size = {0, 0};
			size[0] = rect.width;
			size[1] = rect.height;
			
			//position the shell into the middle of the last window
	        int[] position;
	        position = EModGUI.shellPosition();
	        shell.setLocation(position[0]-size[0]/2, position[1]-size[1]/2);
			
	        //open the new shell
			shell.open();
	}
    
    public void openModelSelectGUI(String type, TableItem item){
    	SelectMachineComponentGUI compGUI= new SelectMachineComponentGUI();		        		
		try {
			compGUI.getSelectionToTable(type, this.getClass().getDeclaredMethod("setModelType", String.class, TableItem.class, boolean.class), this, item);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void setModelType(String type, TableItem item){
    	if(item.getText().matches(""))
			item.setText(1, type);
		else
			item.setText(1, item.getText(1)+", "+type);
    }
    
 	/**
	 * closes the MachineComponentGUI
	 */	
    public void closeMachineComponentGUI(){
    	shell.close();
    }
    
}
