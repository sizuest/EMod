package ch.ethz.inspire.emod.gui.dd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
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

import ch.ethz.inspire.emod.gui.SelectMaterialGUI;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.model.fluid.Isolation;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.ParameterSet;

public class EditDuctIsolationGUI {
	private Shell shell;
    private static Button saveButton, closeButton;
    private Table tableProperties;
    private Isolation isolation;
    private ParameterSet parameters = new ParameterSet();

	public EditDuctIsolationGUI(final EditDuctElementGUI caller,
			final Isolation iso) {
		
		if(null!=iso){
			this.isolation = iso.copy();
			parameters = this.isolation.getParameterSet();
		}
		else
			parameters = (new Isolation()).getParameterSet();
		
		
		shell = new Shell(Display.getCurrent());
	    shell.setText("Duct Isolation Editor");
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
		
		//SOURCE http://www.tutorials.de/threads/in-editierbarer-swt-tabelle-ohne-eingabe-von-enter-werte-aendern.299858/
	    //create a TableCursor to navigate around the table
	    final TableCursor cursor = new TableCursor(tableProperties, SWT.NONE);
	    // create an editor to edit the cell when the user hits "ENTER"
	    // while over a cell in the table
	    final ControlEditor editor = new ControlEditor(cursor);
	    editor.grabHorizontal = true;
	    editor.grabVertical = true;
	    
	    try {
			TableUtils.addCellEditor(tableProperties, this.getClass().getDeclaredMethod("setIsolationThickness", TableCursor.class, Text.class), this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	   
	    /*cursor.addKeyListener(new KeyAdapter() {
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
			                        		isolation.setParameterSet(parameters);
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
	    });*/
	    
	    updatePropertyTable();
        
		
		saveButton = new Button(shell, SWT.NONE);
		saveButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		setIsolation(iso);
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
	
	public void setIsolationThickness(TableCursor cursor, Text text){
		final TableItem row = cursor.getRow();
		try{
    		parameters.setParameter(row.getText(0), Double.parseDouble(text.getText()), new SiUnit(row.getText(2)));
    		isolation.setParameterSet(parameters);
		}
		catch(Exception ex){
			// Not used
		}
        updatePropertyTable();
	}
	
	private void setIsolation(Isolation iso){
    	iso.setIsolation(isolation);
	}

	private void updatePropertyTable() {
		for(TableItem it: tableProperties.getItems()){
			it.dispose();
		}
			
		
		tableProperties.clearAll();
    	tableProperties.setItemCount(0);
    	
    	final TableItem itemMaterial   = new TableItem(tableProperties, SWT.NONE, 0);
    	
    	itemMaterial.setText(0, "Material");
    	if(null!=isolation)
    		itemMaterial.setText(1, isolation.getMaterial().getType());
    	else
    		itemMaterial.setText(1, "none");
    	
    	TableEditor editorButton = new TableEditor(tableProperties);
    	
    	Button buttonEditMaterial = new Button(tableProperties, SWT.NONE);
    	Image imageEdit = new Image(Display.getDefault(), "src/resources/Edit16.gif");
    	buttonEditMaterial.setImage(imageEdit);
    	buttonEditMaterial.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
    	buttonEditMaterial.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		openMaterialGUI();
        	}
			public void widgetDefaultSelected(SelectionEvent event){
				// Not used
        	}
        });
    	buttonEditMaterial.pack();
		editorButton.minimumWidth = buttonEditMaterial.getSize().x;
		editorButton.horizontalAlignment = SWT.LEFT;
        editorButton.setEditor(buttonEditMaterial, itemMaterial, 3);     
        
    	
        for(String key: parameters.getParameterSet().keySet()){
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
	
	public void setMaterial(String type){
		isolation.setMaterial(type);
		updatePropertyTable();
	}
	
	private void openMaterialGUI(){
		SelectMaterialGUI matGUI = new SelectMaterialGUI(shell);
    	String selection = matGUI.open();
    	if(selection != "" & selection !=null)
    		setMaterial(selection);
	}

}
