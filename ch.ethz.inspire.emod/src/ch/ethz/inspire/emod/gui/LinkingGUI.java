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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
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

import ch.ethz.inspire.emod.model.MachineComponent;
import ch.ethz.inspire.emod.utils.IOContainer;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

public class LinkingGUI {

    private Shell shell;

    public LinkingGUI(){
	        shell = new Shell(Display.getCurrent());
	    }

	public void openLinkingGUI(MachineComponent mc){
	        System.out.println("LinkingGUI opened");
	        
	        shell.setText(LocalizationHandler.getItem("app.gui.linking.title"));

	    	shell.setLayout(new GridLayout(1, false));
	    	
	    	Text aText = new Text(shell, SWT.MULTI);
			GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
			gridData.horizontalSpan = 1;
			aText.setLayoutData(gridData);
			aText.setText(LocalizationHandler.getItem("app.gui.linking.title") + ": " + mc.getName());
	    	
			List<IOContainer> inputList = mc.getComponent().getInputs();
			System.out.println("The Component has x inputs: " + inputList.size());
			
	    	//SOURCE http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTTableSimpleDemo.htm Imported for function control
	    	Table tableLinkingView = new Table(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    	gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
			gridData.horizontalSpan = 1;
	    	tableLinkingView.setLayoutData(gridData);
			tableLinkingView.setHeaderVisible(true);
	    		    //TODO manick: language file!
	    		    String[] titles = {LocalizationHandler.getItem("app.gui.linking.input"),
	    		    				   LocalizationHandler.getItem("app.gui.linking.linkto")};

	    		    for (int i=0; i < titles.length; i++) {
	    		      TableColumn column = new TableColumn(tableLinkingView, SWT.NULL);
	    		      column.setText(titles[i]);
	    		    }
	    		    
	    		    for (int i=0; i < inputList.size(); i++){
	    		    	TableItem item = new TableItem(tableLinkingView, SWT.MULTI);
	    		    	item.setText(0, inputList.get(i).getName());

	  		        	TableEditor editor = new TableEditor(tableLinkingView);
	  		        	Combo comboOutputs = new Combo(tableLinkingView, SWT.DROP_DOWN);
	  		        	String items[] = {"Output 1", "Output 2", "Output 3", "and so on"};
	  		        	comboOutputs.setItems(items);
	  		        	
	  		        	comboOutputs.addSelectionListener(new SelectionListener(){
	  		        		public void widgetSelected(SelectionEvent event){
			        		
	  		        			String string = (String) event.data;
	  		        			System.out.println("Ouput selected: " + string);
	  		        		}
	  		        		public void widgetDefaultSelected(SelectionEvent event){
			        		
	  		        		}
	  		        	});
	  		        	
	  		        	comboOutputs.pack();
	  		        	editor.minimumWidth = comboOutputs.getSize().x;
	  		        	editor.horizontalAlignment = SWT.LEFT;
	  		        	editor.setEditor(comboOutputs, item, 1);
	    		    }
	    		    
	    		    
	    		    
			        //Tabelle schreiben
			        TableColumn[] columns = tableLinkingView.getColumns();
			        for (int i = 0; i < columns.length; i++) {
			          columns[i].pack();
			        }

			        //SOURCE http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTTableSimpleDemo.htm Imported for function control   	

	    	Button buttonSave = new Button(shell, SWT.BORDER);
	    	buttonSave.setText(LocalizationHandler.getItem("app.gui.save"));
			gridData = new GridData(GridData.END, GridData.END, false, false);
			gridData.horizontalSpan = 1;
			buttonSave.setLayoutData(gridData);
		    buttonSave.addSelectionListener(new SelectionListener(){
		    	public void widgetSelected(SelectionEvent event){
		    		shell.close();
		    		System.out.println("Linking saved");
		    	}
		    	public void widgetDefaultSelected(SelectionEvent event){
		    		
		    	}
		    });
			
		    shell.setSize(200,200);
			//shell.pack();

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

	    public void closeLinkingGUI(){
	    	shell.close();
	    }
}
