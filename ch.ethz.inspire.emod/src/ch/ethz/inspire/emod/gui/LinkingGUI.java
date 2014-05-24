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

import ch.ethz.inspire.emod.utils.LocalizationHandler;

public class LinkingGUI {

    private Shell shell;


    public LinkingGUI(){
	        shell = new Shell(Display.getCurrent());
	    }

	public void openLinkingGUI(String aString){
	        System.out.println("LinkingGUI opened");
	        
	        shell.setText("IO Linking " + aString);

	    	shell.setLayout(new GridLayout(2, false));
	    	
	    	Text aText = new Text(shell, SWT.MULTI);
			GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
			gridData.horizontalSpan = 2;
			aText.setLayoutData(gridData);
			aText.setText("Component: " + aString);
	    	
	    	//SOURCE http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTTableSimpleDemo.htm Imported for function control
	    	Table aTable = new Table(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    	gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
			gridData.horizontalSpan = 2;
	    	aTable.setLayoutData(gridData);
			aTable.setHeaderVisible(true);
	    		    //TODO manick: language file!
	    		    String[] titles = { "Input", "Link to"};

	    		    for (int i=0; i < titles.length; i++) {
	    		      TableColumn column = new TableColumn(aTable, SWT.NULL);
	    		      column.setText(titles[i]);
	    		    }

	    		    // inputs auslesen aus komponente??
	    		    for (int loopIndex = 0; loopIndex < 4; loopIndex++) {
	    		      TableItem item = new TableItem(aTable, SWT.NULL);
	    		      item.setText("Item " + loopIndex);
	    		      item.setText(0, "Input");
	    		      
	  		        	TableEditor editor = new TableEditor(aTable);
	  		        	Combo aCombo = new Combo(aTable, SWT.DROP_DOWN);
	  		        	String items[] = {"Output 1", "Output 2", "Output 3", "and so on"};
	  		        	aCombo.setItems(items);
	  		        	
	  		        	aCombo.addSelectionListener(new SelectionListener(){
	  		        		public void widgetSelected(SelectionEvent event){
			        		
	  		        			String string = (String) event.data;
	  		        			System.out.println("Ouput selected: " + string);
	  		        		}
	  		        		public void widgetDefaultSelected(SelectionEvent event){
			        		
	  		        		}
	  		        	});
	  		        	aCombo.pack();
	  		        	editor.minimumWidth = aCombo.getSize().x;
	  		        	editor.horizontalAlignment = SWT.LEFT;
	  		        	editor.setEditor(aCombo, item, 1);
	    		    }

	    		    for (int loopIndex = 0; loopIndex < titles.length; loopIndex++) {
	    		      aTable.getColumn(loopIndex).pack();
	    		    }

	    		    //aTable.setBounds(25, 25, 220, 200);
	    	    	//SOURCE http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTTableSimpleDemo.htm Imported for function control   	

	    	Button aButton = new Button(shell, SWT.BORDER);
	    	aButton.setText("Speichern");
			gridData = new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_END, true, false);
			gridData.horizontalSpan = 1;
			aButton.setLayoutData(gridData);
			
			//TODO manick: selectionListener Button
	        
			
			shell.pack();

			Rectangle rect = shell.getBounds();
			
			//width and height of the shell
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
