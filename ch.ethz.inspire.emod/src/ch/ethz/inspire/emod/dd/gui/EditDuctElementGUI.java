package ch.ethz.inspire.emod.dd.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.ethz.inspire.emod.dd.model.ADuctElement;
import ch.ethz.inspire.emod.dd.model.AHydraulicProfile;
import ch.ethz.inspire.emod.gui.AConfigGUI;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.model.fluid.Isolation;

import ch.ethz.inspire.emod.model.parameters.ParameterSet;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

public class EditDuctElementGUI  extends AConfigGUI{
	
    private Table tableProperties;
    private ADuctElement element;
    private ParameterSet parametersNew = new ParameterSet();
    private AHydraulicProfile profileOld;
    private Isolation isolationNew;
    
    
    public EditDuctElementGUI(Composite parent, int style, final ADuctElement element){
    	super(parent, style);
    	
    	
    	this.element = element;
    	this.parametersNew.getParameterSet().putAll(element.getParameterSet().getParameterSet());
    	
    	profileOld = this.element.getProfileIn().clone();
    	
    	if(null==this.element.getIsolation())
    		isolationNew = new Isolation();
    	else
    		isolationNew = this.element.getIsolation().clone();
    	
    	this.getContent().setLayout(new GridLayout(1, true));
		
		tableProperties = new Table(this.getContent(), SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		tableProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tableProperties.setLinesVisible(true);
		tableProperties.setHeaderVisible(true);
		
		String[] titles =  {	LocalizationHandler.getItem("app.dd.elemet.gui.property"),
								LocalizationHandler.getItem("app.dd.elemet.gui.value"),
								LocalizationHandler.getItem("app.dd.elemet.gui.unit"),
								"        ",
								"        "};
		
		for(int i=0; i < titles.length; i++){
			TableColumn column = new TableColumn(tableProperties, SWT.NULL);
			column.setText(titles[i]);
			column.setWidth(32);
		}			
	    
	    updatePropertyTable();
    }
    
    public static Shell editDuctElementGUI(Shell parent, ADuctElement element) {
		final Shell shell = new Shell(parent, SWT.APPLICATION_MODAL| SWT.CLOSE | SWT.MAX);
		shell.setLayout(new GridLayout(1, true));
		
		EditDuctElementGUI gui = new EditDuctElementGUI(shell, SWT.NONE, element);
		
		shell.setText(LocalizationHandler.getItem("app.dd.elemet.gui.titel")+" "+element.getName());
		
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
    
    private void updatePropertyTable(){
      	
    	for(Control c: tableProperties.getChildren())
    		c.dispose();
    	
    	try {
			TableUtils.addCellEditor(tableProperties, this, new int[]{1});
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    	
    	tableProperties.clearAll();
    	tableProperties.setItemCount(0);
    	
    	TableItem itemName       = new TableItem(tableProperties, SWT.NONE, 0);
    	new TableItem(tableProperties, SWT.NONE, 1); // Profile
    	new TableItem(tableProperties, SWT.NONE, 2); // Isolation
    	
    	updateProfileItem();
    	updateIsolationItem();
    	
    	itemName.setText(0, LocalizationHandler.getItem("app.dd.elemet.gui.name"));
    	itemName.setText(1, element.getName());     
    	
        for(String key: element.getParameterSet().getParameterSet().keySet()){
        	final int idx = tableProperties.getItemCount();
        	final TableItem itemParam = new TableItem(tableProperties, SWT.NONE, idx);
        	
        	itemParam.setText(0, key);
        	itemParam.setText(1, parametersNew.getPhysicalValue(key).getValue()+"");
        	itemParam.setText(2, parametersNew.getPhysicalValue(key).getUnit().toString());
        	
        }
        
        TableColumn[] columns = tableProperties.getColumns();
        for (int j = 0; j < columns.length; j++) {
        	columns[j].pack();
        }
    }
    
    private void updateIsolationItem(){
    	
    	TableItem itemIsolation  = tableProperties.getItem(2);
    	TableEditor editorButton = new TableEditor(tableProperties);
    	Image imageEdit          = new Image(Display.getDefault(), "src/resources/Edit16.gif");
    	
    	itemIsolation.setText(0, LocalizationHandler.getItem("app.dd.elemet.gui.isolation"));
        if(null==isolationNew)
        	itemIsolation.setText(1, "none");
        else if(null==isolationNew.getMaterial())
        	itemIsolation.setText(1, "none");
        else if(null==isolationNew.getMaterial().getType())
        	itemIsolation.setText(1, "none");
        else {
        	itemIsolation.setText(1, isolationNew.toString());
        	itemIsolation.setText(2, (new SiUnit("m")).toString());
        }
        
        editorButton = new TableEditor(tableProperties);
    	Button buttonEditIsolation = new Button(tableProperties, SWT.NONE);
    	buttonEditIsolation.setImage(imageEdit);
    	buttonEditIsolation.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
    	buttonEditIsolation.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		if(null==isolationNew)
        			isolationNew = new Isolation();
        		
        		wasEdited();
        		editDuctIsolation(isolationNew);
        	}
			public void widgetDefaultSelected(SelectionEvent event){
				// Not used
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
        		isolationNew = null;
        		wasEdited();
        		update();
        	}
			public void widgetDefaultSelected(SelectionEvent event){
				// Not used
        	}
        });
    	buttonDeleteIsolation.pack();
		editorButton.minimumWidth = buttonDeleteIsolation.getSize().x;
		editorButton.horizontalAlignment = SWT.LEFT;
        editorButton.setEditor(buttonDeleteIsolation, itemIsolation, 4);
    }
    
    private void updateProfileItem(){
    	TableItem itemProfile = tableProperties.getItem(1);
    	
    	TableEditor editorButton = new TableEditor(tableProperties);
    	
    	itemProfile.setText(0, LocalizationHandler.getItem("app.dd.elemet.gui.profile"));
    	itemProfile.setText(1, element.getProfileIn().toString());
    	itemProfile.setText(2, (new SiUnit("m")).toString()); 
    	
    	Button buttonEditProfile = new Button(tableProperties, SWT.NONE);
    	Image imageEdit = new Image(Display.getDefault(), "src/resources/Edit16.gif");
    	buttonEditProfile.setImage(imageEdit);
    	buttonEditProfile.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
    	buttonEditProfile.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		editDuctProfile(element);
        		wasEdited();
        	}
			public void widgetDefaultSelected(SelectionEvent event){
				// Not used
        	}
        });
    	buttonEditProfile.pack();
		editorButton.minimumWidth = buttonEditProfile.getSize().x;
		editorButton.horizontalAlignment = SWT.LEFT;
        editorButton.setEditor(buttonEditProfile, itemProfile, 3);
    }
    
	private void editDuctProfile(ADuctElement element) {
		Shell shell = EditDuctProfileGUI.editDuctProfileGUI(this.getShell(), element);
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				updateProfileItem();
			}
		});
	}
	
	private void editDuctIsolation(Isolation isolation) {
		Shell shell = EditDuctIsolationGUI.editDuctIsolationGUI(this.getShell(), isolation);
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				updateIsolationItem();
			}
		});
	}

	public void update() {
		updatePropertyTable();
	}

	@Override
	public void save() {
		
		// Read new Config
		for(int i=3; i<tableProperties.getItemCount(); i++){
			try{
				parametersNew.setPhysicalValue(tableProperties.getItem(i).getText(0),
						Double.valueOf(tableProperties.getItem(i).getText(1)), 
						new SiUnit(""+tableProperties.getItem(i).getText(2)));
			}
			catch(Exception e){
				System.err.println("Can not parse input '"+tableProperties.getItem(i).getText(0)+
						            "' as number: "+tableProperties.getItem(i).getText(1));
			}
		}
		
		element.setParameterSet(parametersNew);
		element.setIsolation(isolationNew);
		
		profileOld = this.element.getProfileIn().clone();
		
		updatePropertyTable();
	}

	@Override
	public void reset() {
		for(String s: this.parametersNew.getParameterSet().keySet())
			this.parametersNew.setPhysicalValue(s, element.getParameterSet().getPhysicalValue(s));
		
		profileOld   = this.element.getProfileIn().clone();
		if(null==this.element.getIsolation())
    		isolationNew = new Isolation();
    	else
    		isolationNew = this.element.getIsolation().clone();
		
		updatePropertyTable();
		
		element.setProfile(profileOld);
		profileOld = this.element.getProfileIn().clone();
		
	}
}