package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
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
import ch.ethz.inspire.emod.model.fluid.Isolation;

import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.ParameterSet;

public class EditDuctElementGUI {
	private Shell shell;
    private static Button saveButton, closeButton;
    private Table tableProperties;
    private ADuctElement element;
    private ParameterSet parameters = new ParameterSet();
    
    
    public EditDuctElementGUI(final DuctDesignGUI caller, final ADuctElement element){
    	this.element = element;
    	this.parameters.getParameterSet().putAll(element.getParameterSet().getParameterSet());
    	
    	shell = new Shell(Display.getCurrent());
	    shell.setText("Duct Element Editor"+element.getName());
		shell.setLayout(new GridLayout(2, false));
		
		tableProperties = new Table(shell, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		tableProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tableProperties.setLinesVisible(true);
		tableProperties.setHeaderVisible(true);
		
		String[] titles =  {"Property",
				"Value",
				"Unit",
				"        ",
				"        "};
		
		for(int i=0; i < titles.length; i++){
			TableColumn column = new TableColumn(tableProperties, SWT.NULL);
			column.setText(titles[i]);
			column.setWidth(32);
		}
		
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
		                int column = cursor.getColumn();
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
			                        		parameters.setParameter(row.getText(0), Double.parseDouble(text.getText()), new SiUnit(row.getText(2)));
		                        		}
		                        		catch(Exception ex){}
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
	    
	    updatePropertyTable();
        
		
		saveButton = new Button(shell, SWT.NONE);
		saveButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		element.setParameterSet(parameters);
        		caller.update();
        	}
        	public void widgetDefaultSelected(SelectionEvent event){}
		});
		
		closeButton = new Button(shell, SWT.NONE);
		closeButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
		closeButton.setText("Close");
		closeButton.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		shell.dispose();
        	}
        	public void widgetDefaultSelected(SelectionEvent event){}
		});
		
		
        
        shell.pack();
		
		shell.open();
    }
    
    private void updatePropertyTable(){
    	tableProperties.clearAll();
    	tableProperties.setItemCount(0);
    	
    	TableItem itemName       = new TableItem(tableProperties, SWT.NONE, 0);
    	TableItem itemProfile    = new TableItem(tableProperties, SWT.NONE, 1);
    	TableItem itemIsolation  = new TableItem(tableProperties, SWT.NONE, 2);
    	
    	itemName.setText(0, "Name");
    	itemName.setText(1, element.getName());
    	itemProfile.setText(0, "Profile");
    	itemProfile.setText(1, element.getProfile().toString());
    	itemProfile.setText(2, (new SiUnit("m")).toString());
    	
    	TableEditor editorButton = new TableEditor(tableProperties);
    	
    	Button buttonEditProfile = new Button(tableProperties, SWT.NONE);
    	Image imageEdit = new Image(Display.getDefault(), "src/resources/Edit16.gif");
    	buttonEditProfile.setImage(imageEdit);
    	buttonEditProfile.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
    	buttonEditProfile.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		editDuctProfile(element);
        	}
			public void widgetDefaultSelected(SelectionEvent event){
        		
        	}
        });
    	buttonEditProfile.pack();
		editorButton.minimumWidth = buttonEditProfile.getSize().x;
		editorButton.horizontalAlignment = SWT.LEFT;
        editorButton.setEditor(buttonEditProfile, itemProfile, 3);
        
        itemIsolation.setText(0, "Isolation");
        if(null==element.getIsolation())
        	itemIsolation.setText(1, "none");
        else if(null==element.getIsolation().getMaterial().getType())
        	itemIsolation.setText(1, "none");
        else {
        	itemIsolation.setText(1, element.getIsolation().toString());
        	itemIsolation.setText(2, (new SiUnit("m")).toString());
        }
        
        editorButton = new TableEditor(tableProperties);
    	Button buttonEditIsolation = new Button(tableProperties, SWT.NONE);
    	buttonEditIsolation.setImage(imageEdit);
    	buttonEditIsolation.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
    	buttonEditIsolation.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		if(null==element.getIsolation())
        			element.setIsolation(new Isolation());
        		
        		editDuctIsolation(element.getIsolation());
        	}
			public void widgetDefaultSelected(SelectionEvent event){
        		
        	}
        });
    	buttonEditIsolation.pack();
		editorButton.minimumWidth = buttonEditIsolation.getSize().x;
		editorButton.horizontalAlignment = SWT.LEFT;
        editorButton.setEditor(buttonEditIsolation, itemIsolation, 3);
        
        editorButton = new TableEditor(tableProperties);
    	Button buttonDeleteIsolation = new Button(tableProperties, SWT.NONE);
    	Image imageDelete = new Image(Display.getDefault(), "src/resources/Delete16.gif");
    	buttonDeleteIsolation.setImage(imageDelete);
    	buttonDeleteIsolation.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
    	buttonDeleteIsolation.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		element.setIsolation(null);
        		update();
        	}
			public void widgetDefaultSelected(SelectionEvent event){
        		
        	}
        });
    	buttonDeleteIsolation.pack();
		editorButton.minimumWidth = buttonDeleteIsolation.getSize().x;
		editorButton.horizontalAlignment = SWT.LEFT;
        editorButton.setEditor(buttonDeleteIsolation, itemIsolation, 4);
        
        
    	
        for(String key: element.getParameterSet().getParameterSet().keySet()){
        	final int idx = tableProperties.getItemCount();
        	final TableItem itemParam = new TableItem(tableProperties, SWT.NONE, idx);
        	
        	itemParam.setText(0, key);
        	itemParam.setText(1, parameters.getParameter(key).getValue()+"");
        	itemParam.setText(2, parameters.getParameter(key).getUnit().toString());
        	
        }
        
        TableColumn[] columns = tableProperties.getColumns();
        for (int j = 0; j < columns.length; j++) {
        	columns[j].pack();
        }
    }
    
	private void editDuctProfile(ADuctElement element) {
		new EditDuctProfileGUI(this, element);
	}
	
	private void editDuctIsolation(Isolation isolation) {
		new EditDuctIsolationGUI(this, isolation);		
	}

	public void update() {
		updatePropertyTable();
	}
}