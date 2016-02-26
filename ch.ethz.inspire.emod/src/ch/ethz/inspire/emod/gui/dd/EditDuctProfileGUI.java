package ch.ethz.inspire.emod.gui.dd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.model.fluid.ADuctElement;
import ch.ethz.inspire.emod.model.fluid.AHydraulicProfile;
import ch.ethz.inspire.emod.model.fluid.HPCircular;
import ch.ethz.inspire.emod.model.fluid.HPRectangular;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.ParameterSet;

public class EditDuctProfileGUI {
	private Shell shell;
    private static Button saveButton, closeButton;
    private Table tableProperties;
    private AHydraulicProfile profilenew;
    private AHydraulicProfile[] candidates = {new HPCircular(.01), new HPRectangular(.01,.01)};
    private ParameterSet allParameters = new ParameterSet();
    private CCombo comboProfile;
    private ADuctElement element;
    
    
    public EditDuctProfileGUI(final EditDuctElementGUI caller, ADuctElement element){
    	this.element = element;
    	
    	/* Write global parameterset */
    	for(int i=0; i<candidates.length; i++) {
    		if(candidates[i].getClass().equals(element.getProfile().getClass())){
    			candidates[i].setParameterSet(element.getProfile().getParameterSet());
    			this.profilenew = candidates[i];
    		}
    		allParameters.getParameterSet().putAll(candidates[i].getParameterSet().getParameterSet());
    	}
       	
    	shell = new Shell(Display.getCurrent());
	    shell.setText("Duct Profile Editor");
		shell.setLayout(new GridLayout(2, false));
		
		tableProperties = new Table(shell, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		tableProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tableProperties.setLinesVisible(true);
		tableProperties.setHeaderVisible(true);
		
		String[] titles =  {"Property",
				"Value",
				"Unit",
				"        "};
		
		for(int i=0; i < titles.length; i++){
			TableColumn column = new TableColumn(tableProperties, SWT.NULL);
			column.setText(titles[i]);
			column.setWidth(32);
		}
		
		updatePropertyTable();
		
		//SOURCE http://www.tutorials.de/threads/in-editierbarer-swt-tabelle-ohne-eingabe-von-enter-werte-aendern.299858/
	    //create a TableCursor to navigate around the table
	    final TableCursor cursor = new TableCursor(tableProperties, SWT.NONE);
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
		                final TableItem row = cursor.getRow();
		                text.append(String.valueOf(e.character));
		                text.addKeyListener(new KeyAdapter() {
		                    public void keyPressed(KeyEvent e) {
		                        // close the text editor and copy the data over
		                        // when the user hits "ENTER"
		                        if (e.character == SWT.CR) {
		                            int column = cursor.getColumn();
		                        	switch(column){
		                        	case 1:
		                        		try{
			                        		allParameters.setParameter(row.getText(0), Double.parseDouble(text.getText()), new SiUnit(row.getText(2)));
		                        		}
		                        		catch(Exception ex){
		                        			System.err.println("Set Parameter failed: could not parse '"+text.getText()+"' to double.");
		                        		}
			                        	updatePropertyTable();
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
		
		saveButton = new Button(shell, SWT.NONE);
		saveButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		profilenew.setParameterSet(allParameters);
        		updateProfile();
        		caller.update();
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		// Not used
        	}
		});
		
		closeButton = new Button(shell, SWT.NONE);
		closeButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
		closeButton.setText("Close");
		closeButton.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		shell.dispose();
        	}
        	public void widgetDefaultSelected(SelectionEvent event){
        		// Not used
        	}
		});
        
        shell.pack();
		
		shell.open();
    }
    
    private void updateProfile(){
    	element.setProfile(this.profilenew);
    }
    
    private void updatePropertyTable(){
    	
    	tableProperties.clearAll();
    	tableProperties.setItemCount(0);
    	
    	TableItem itemType = new TableItem(tableProperties, SWT.NONE, 0);
    	
    	itemType.setText(0, "Shape");
    	
    	comboProfile = new CCombo(tableProperties, SWT.NONE);
    	String[] comboItems = new String[candidates.length];
    	for(int i=0; i<candidates.length; i++)
    		comboItems[i] = candidates[i].getClass().getSimpleName().replace("HP", "");
    	comboProfile.setItems(comboItems);
    		
  
    	TableEditor editorCombo = new TableEditor(tableProperties);
    	comboProfile.setText(this.profilenew.getClass().getSimpleName().replace("HP", ""));
    	comboProfile.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent event){
				//disable comboMachineConfigName to prevent argument null for updatecomboMachineConfigName
				comboProfile.setEnabled(false);
				
				profilenew = candidates[comboProfile.getSelectionIndex()];
				profilenew.setParameterSet(allParameters);
				comboProfile.dispose();
				
				updatePropertyTable();
				//System.out.println("***comboEditInputUnit: " + sc.getName() + " " + sc.getUnit().toString());
    			//enable comboMachineConfigName after update
    		}
    		public void widgetDefaultSelected(SelectionEvent event){
    			// Not used
    		}
    	});
        
    	comboProfile.pack();
    	editorCombo.minimumWidth = comboProfile.getSize().x;
    	editorCombo.grabHorizontal = true;
    	editorCombo.horizontalAlignment = SWT.LEFT;
    	editorCombo.setEditor(comboProfile, itemType, 1);
    	
        for(String key: this.profilenew.getParameterSet().getParameterSet().keySet()){
        	final int idx = tableProperties.getItemCount();
        	final TableItem itemParam = new TableItem(tableProperties, SWT.NONE, idx);
        	
        	itemParam.setText(0, key);
        	itemParam.setText(1, allParameters.getParameter(key).getValue()+"");
        	itemParam.setText(2, allParameters.getParameter(key).getUnit().toString());
        }
        
        TableColumn[] columns = tableProperties.getColumns();
        for (int j = 0; j < columns.length; j++) {
        	columns[j].pack();
        }
    }
}
