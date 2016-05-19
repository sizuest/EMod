package ch.ethz.inspire.emod.gui.dd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.ethz.inspire.emod.gui.AConfigGUI;
import ch.ethz.inspire.emod.gui.SelectMaterialGUI;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.model.fluid.Isolation;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.ParameterSet;

public class EditDuctIsolationGUI extends AConfigGUI{
    private Table tableProperties;
    private Isolation isolationOld, isolationNew;
    private ParameterSet parameters = new ParameterSet();
    Button buttonEditMaterial;

	public EditDuctIsolationGUI(Composite parent, int style, final Isolation iso) {
		super(parent, style);
		
		if(null!=iso){
			this.isolationOld = iso;
			this.isolationNew = iso.copy();
			parameters = this.isolationOld.getParameterSet();
		}
		else
			parameters = (new Isolation()).getParameterSet();
		
		this.getContent().setLayout(new GridLayout(1, true));
		
		tableProperties = new Table(this.getContent(), SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
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
		
	    final TableCursor cursor = new TableCursor(tableProperties, SWT.NONE);
	    final ControlEditor editor = new ControlEditor(cursor);
	    editor.grabHorizontal = true;
	    editor.grabVertical = true;
	    
	    try {
			TableUtils.addCellEditor(tableProperties, this, new int[]{1});
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	    updatePropertyTable();
	}
	
	public static Shell editDuctIsolationGUI(Shell parent, Isolation isolation) {
		final Shell shell = new Shell(parent, SWT.TITLE|SWT.SYSTEM_MODAL| SWT.CLOSE | SWT.MAX);
		shell.setLayout(new FillLayout());
		
		EditDuctIsolationGUI gui = new EditDuctIsolationGUI(shell, SWT.NONE, isolation);
		
		shell.setText("Duct Isolation Editor");
		
		shell.pack();
		
		shell.layout();
		shell.redraw();
		shell.open();
		gui.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				shell.dispose();
			}
		});
		
		return shell;
	}
	
	private void setIsolationThickness(){
		try{
			parameters.setParameter(tableProperties.getItem(1).getText(0), Double.parseDouble(tableProperties.getItem(1).getText(1)), new SiUnit(tableProperties.getItem(1).getText(2)));
    		isolationNew.setParameterSet(parameters);
		}
		catch(Exception ex){
			// Not used
		}
	}

	private void updatePropertyTable() {
		if(null!=buttonEditMaterial)
			buttonEditMaterial.dispose();
			
		
		tableProperties.clearAll();
    	tableProperties.setItemCount(0);
    	
    	final TableItem itemMaterial   = new TableItem(tableProperties, SWT.NONE, 0);
    	
    	itemMaterial.setText(0, "Material");
    	if(null!=isolationNew)
    		itemMaterial.setText(1, isolationNew.getMaterial().getType());
    	else
    		itemMaterial.setText(1, "none");
    	
    	TableEditor editorButton = new TableEditor(tableProperties);
    	
    	buttonEditMaterial = new Button(tableProperties, SWT.NONE);
    	Image imageEdit = new Image(Display.getDefault(), "src/resources/Edit16.gif");
    	buttonEditMaterial.setImage(imageEdit);
    	buttonEditMaterial.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
    	buttonEditMaterial.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		openMaterialGUI();
        		wasEdited();
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
	
	private void setMaterial(String type){
		isolationNew.setMaterial(type);
		updatePropertyTable();
	}
	
	private void openMaterialGUI(){
		SelectMaterialGUI matGUI = new SelectMaterialGUI(this.getShell());
    	String selection = matGUI.open();
    	if(selection != "" & selection !=null)
    		setMaterial(selection);
	}

	@Override
	public void save() {
		setIsolationThickness();
		isolationOld.setIsolation(isolationNew);
		updatePropertyTable();
	}

	@Override
	public void reset() {
		isolationNew = isolationOld.copy();
		if(null!=isolationNew){
			parameters = this.isolationNew.getParameterSet();
		}
		else
			parameters = (new Isolation()).getParameterSet();
		
		updatePropertyTable();
	}

}
