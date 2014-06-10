package ch.ethz.inspire.emod.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

public class ComponentEditGUI {

    private Shell shell;

    public ComponentEditGUI(){
    	
	    }

    //Component Edit GUI for creating a new Component
    public void newComponentEditGUI(){
    	shell = new Shell(Display.getCurrent());
        shell.setText(LocalizationHandler.getItem("app.gui.compdb.newcomp"));
    	shell.setLayout(new GridLayout(2, false));

		//Text "Type" of the Component
		Text textCompType = new Text(shell, SWT.READ_ONLY);
		textCompType.setText(LocalizationHandler.getItem("app.gui.model.type") + ":");
		textCompType.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, 1));
		
		//Combo to select Value of "Type" of the Component
		final Combo comboCompTypeValue = new Combo(shell, SWT.NONE);
		
		//according to the given component, get the path for the parameter sets
		final String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/";
		File subdir = new File(path);
    	
    	//check if the directory exists, then show possible parameter sets to select
    	if(subdir.exists()){
    		String[] subitems = subdir.list();
    		comboCompTypeValue.setItems(subitems);
    	}
		
		comboCompTypeValue.setText(LocalizationHandler.getItem("app.gui.compdb.selecttype"));
		comboCompTypeValue.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, 1));
		
		//Text "Parameter-Set" of the Component
		Text textCompParam = new Text(shell, SWT.READ_ONLY);
		textCompParam.setText(LocalizationHandler.getItem("app.gui.compdb.compname"));
		textCompParam.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 1, 1));
		
		//Combo to let the user select the desired Parameter-set of the Component
		final Text textCompParamValue = new Text(shell, SWT.NONE);
		textCompParamValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

    	//button to continue
		Button buttonContinue = new Button(shell, SWT.NONE);
		buttonContinue.setText(LocalizationHandler.getItem("app.gui.continue"));
		//selection Listener for the button, actions when button is pressed
		buttonContinue.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent event){
	    		String stringCompTypeValue = comboCompTypeValue.getText();
	    		String stringCompParamValue = textCompParamValue.getText();
	    		
	    		//copy the example type of the selected component and create a copy
	    		//source http://www.javapractices.com/topic/TopicAction.do?Id=246
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
	    		editComponentEditGUI(stringCompTypeValue, stringCompParamValue);
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
    
    //Component Edit GUI for editing a existing Component of the Component DB
    public void editComponentEditGUI(String type, String parameter){
    	shell = new Shell(Display.getCurrent());
        shell.setText(LocalizationHandler.getItem("app.gui.compdb.editcomp"));
    	shell.setLayout(new GridLayout(2, false));
				
		//Text "Type" of the Component
		Text textCompType = new Text(shell, SWT.READ_ONLY);
		textCompType.setText(LocalizationHandler.getItem("app.gui.model.type") + ":");
		textCompType.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, true, 1, 1));
		
		//Text to show Value of "Type" of the Component
		Text textCompTypeValue = new Text(shell, SWT.READ_ONLY);
		textCompTypeValue.setText(type);
		textCompTypeValue.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
    	
 		//Text "Parameter-Set" of the Component
		Text textCompParam = new Text(shell, SWT.READ_ONLY);
		textCompParam.setText(LocalizationHandler.getItem("app.gui.model.param") + ":");
		textCompParam.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, true, 1, 1));
		
		//Combo to let the user select the desired Parameter-set of the Component
		Text textCompParamValue = new Text(shell, SWT.READ_ONLY);
		textCompParamValue.setText(parameter);
		textCompParamValue.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, true, 1, 1));
		
		//path + filename -> link to the .xml file
		String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/" + type + "/";
		String filename = type + "_" + parameter + ".xml";
		final File file = new File(path + filename);
		
		//open Inputstream of the selected file
		InputStream iostream = null;
		try {
			iostream = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		//load properties from the file
		final Properties props = new Properties();
		try {
			props.loadFromXML(iostream);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		//prepare text and styledtext widgets to show the key and values of the properties
		int length = props.size();
		Text[] textKey = new Text[length];
		final Text[] textValue = new Text[length];

		//iterate over all the objects of the properties
		//SOURCE http://www.avajava.com/tutorials/lessons/how-do-i-read-properties-from-an-xml-file.html
		int i = 0;
		Enumeration<Object> enuKeys = props.keys();
		while (enuKeys.hasMoreElements()) {
			//get the key of the current element and write it to the text widget
			String key = (String) enuKeys.nextElement();
			textKey[i] = new Text(shell, SWT.READ_ONLY);
			textKey[i].setText(key);
			textKey[i].setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));

			//get the value of the current key and write it to the styledtext widget
			String value = props.getProperty(key);
			textValue[i] = new Text(shell, SWT.MULTI);
			textValue[i].setText(value);
			textValue[i].setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));

			//pack the text Value, if size is bigger than (400, 200), then resize to max (400,200)
			textValue[i].pack();
			if(textValue[i].getSize().x > 400){
				textValue[i].setLayoutData(new GridData(400, textValue[i].getSize().y));
			}
			if(textValue[i].getSize().y > 200){
				textValue[i].setLayoutData(new GridData(400, 200));
			}
			
			i++;
		}
		
    	//button to save
		Button buttonSave = new Button(shell, SWT.NONE);
		buttonSave.setText(LocalizationHandler.getItem("app.gui.save"));
		//selection Listener for the button, actions when button is pressed
		buttonSave.addSelectionListener(new SelectionListener(){
	    	public void widgetSelected(SelectionEvent event){
	    		
	    		//iterate over all the objects of the properties
	    		int i = 0;
	    		Enumeration<Object> enuKeys = props.keys();
	    		while (enuKeys.hasMoreElements()) {
	    			//get the value of the current key and set it to the property
	    			String key = (String) enuKeys.nextElement();
    				props.setProperty(key, textValue[i].getText());
	    			i++;
	    		}
	    		
	    		//write properties to file
	    		FileOutputStream fos = null;
	    		try {
	    			fos = new FileOutputStream(file);
	        		//TODO sizuest: comment from original file is lost
			         props.storeToXML(fos, "File changed by user: " + System.getProperty("user.name"));
	    		} catch (FileNotFoundException e1) {
	    			e1.printStackTrace();
	    		} catch (IOException e) {
					e.printStackTrace();
				}
	    		
	    		//update the component DB on the right hand side of the model gui tabel
	    		ModelGUI.updateTabCompDB();
	    		
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
    
    //open Component Edit GUI for Component, that already exists in the machine configuration    
	public void openComponentEditGUI(final MachineComponent mc, final TableItem item){
        	shell = new Shell(Display.getCurrent());
	        shell.setText(LocalizationHandler.getItem("app.gui.compdb.editcomp"));
	    	shell.setLayout(new GridLayout(2, false));

	    	//Text "Name" of the Component
			Text textCompName = new Text(shell, SWT.READ_ONLY);
			textCompName.setText(LocalizationHandler.getItem("app.gui.model.name") + ":");
			textCompName.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, true, 1, 1));;
			
			//Textfield to enter the Name of the Component
			final Text textCompNameValue = new Text(shell, SWT.NONE);
			textCompNameValue.setText(mc.getName());
			textCompNameValue.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
			
			//Text "Type" of the Component
			Text textCompType = new Text(shell, SWT.READ_ONLY);
			textCompType.setText(LocalizationHandler.getItem("app.gui.model.type") + ":");
			textCompType.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, true, 1, 1));
			
			//Text Value of "Type" of the Component
			Text textCompTypeValue = new Text(shell, SWT.READ_ONLY);
			textCompTypeValue.setText(mc.getComponent().getClass().toString().replace("class ch.ethz.inspire.emod.model.",""));
			textCompTypeValue.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, true, 1, 1));
			
			//Text "Parameter-Set" of the Component
			Text textCompParam = new Text(shell, SWT.READ_ONLY);
			textCompParam.setText(LocalizationHandler.getItem("app.gui.model.param") + ":");
			textCompParam.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, true, 1, 1));
			
			//Combo to let the user select the desired Parameter-set of the Component
			final Combo comboCompParamValue = new Combo(shell, SWT.NONE);
			comboCompParamValue.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
			
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
	    		comboCompParamValue.setItems(subitems);
	    		comboCompParamValue.setText(mc.getComponent().getType());
	    	}
	    				
	    	//button to save the selection
			Button buttonSave = new Button(shell, SWT.NONE);
			buttonSave.setText(LocalizationHandler.getItem("app.gui.save"));
			//selection Listener for the button, actions when button is pressed
			buttonSave.addSelectionListener(new SelectionListener(){
		    	public void widgetSelected(SelectionEvent event){
		    		//get the entered name in the textfield and set it to the MachineComponents name
		    		mc.setName(textCompNameValue.getText());
		    		//set the Name into the table item
		    		item.setText(0, mc.getName());
		    		
		    		//get the selected name of the parameter in the combo and set it to the MachineComponent
		    		mc.getComponent().setType(comboCompParamValue.getText());
		    		//set the name into the table item
		    		item.setText(2, mc.getComponent().getType());

		    		//update the table in the modelGUI
		    		ModelGUI.updateTable();
		    		
		    		//close the Component Edit GUI
		    		closeComponentEditGUI();
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
	
    public void closeComponentEditGUI(){
    	shell.close();
    }
}
