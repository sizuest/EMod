package ch.ethz.inspire.emod.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.utils.LocalizationHandler;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

public class EditInputGUI {
	
    private Shell shell;

    public EditInputGUI(){
    	
	    }
    
    public void editInputGUI(String type, String parameter){
    	shell = new Shell(Display.getCurrent());
        shell.setText(LocalizationHandler.getItem("app.gui.compdb.editcomp"));
    	shell.setLayout(new GridLayout(2, false));
				
		//Text "Type" of the Component
		Text textComponentType = new Text(shell, SWT.READ_ONLY);
		textComponentType.setText(LocalizationHandler.getItem("app.gui.model.type") + ":");
		textComponentType.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, true, 1, 1));
		
		//Text to show Value of "Type" of the Component
		Text textComponentTypeValue = new Text(shell, SWT.READ_ONLY);
		textComponentTypeValue.setText(type);
		textComponentTypeValue.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
    	
 		//Text model type of the Component
		Text textComponentModelType = new Text(shell, SWT.READ_ONLY);
		textComponentModelType.setText(LocalizationHandler.getItem("app.gui.model.param") + ":");
		textComponentModelType.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, true, 1, 1));
		
		//Combo to let the user select the desired Parameter-set of the Component
		Text textComponentModelTypeValue = new Text(shell, SWT.READ_ONLY);
		textComponentModelTypeValue.setText(parameter);
		textComponentModelTypeValue.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, true, 1, 1));
		
		//path + filename -> link to the .xml file
		String path = PropertiesHandler.getProperty("app.MachineDataPathPrefix") + "/" +
					  PropertiesHandler.getProperty("sim.MachineName") + "/" +
					  "MachineConfig/" +
					  PropertiesHandler.getProperty("sim.MachineConfigName") + "/";
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
		
		//SOURCE for loading properties from xml file:
		//http://www.avajava.com/tutorials/lessons/how-do-i-read-properties-from-an-xml-file.html
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
	    		// Not used
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
}
