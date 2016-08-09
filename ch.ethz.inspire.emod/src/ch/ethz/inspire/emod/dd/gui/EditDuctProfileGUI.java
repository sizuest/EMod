package ch.ethz.inspire.emod.dd.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.ethz.inspire.emod.dd.model.ADuctElement;
import ch.ethz.inspire.emod.dd.model.AHydraulicProfile;
import ch.ethz.inspire.emod.dd.model.HPCircular;
import ch.ethz.inspire.emod.dd.model.HPRectangular;
import ch.ethz.inspire.emod.gui.AConfigGUI;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.model.parameters.ParameterSet;
import ch.ethz.inspire.emod.model.units.SiUnit;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

public class EditDuctProfileGUI extends AConfigGUI{

    private Table tableProperties;
    private AHydraulicProfile profileNew;
    private AHydraulicProfile[] candidates = {new HPCircular(.01), new HPRectangular(.01,.01)};
    private ParameterSet allParameters = new ParameterSet();
    private CCombo comboProfile;
    private ADuctElement element;
    
    
    public EditDuctProfileGUI(Composite parent, int style, ADuctElement element){
    	super(parent, style);
    	
    	this.element = element;
    	
    	/* Write global parameterset */
    	for(int i=0; i<candidates.length; i++) {
    		if(candidates[i].getClass().equals(element.getProfileIn().getClass())){
    			candidates[i].setParameterSet(element.getProfileIn().getParameterSet());
    			this.profileNew = candidates[i];
    		}
    		allParameters.getParameterSet().putAll(candidates[i].getParameterSet().getParameterSet());
    	}
		
    	this.getContent().setLayout(new GridLayout(1, true));
    	
		tableProperties = new Table(this.getContent(), SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		tableProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tableProperties.setLinesVisible(true);
		tableProperties.setHeaderVisible(true);
		
		String[] titles =  {LocalizationHandler.getItem("app.dd.elemet.gui.property"),
							LocalizationHandler.getItem("app.dd.elemet.gui.value"),
							LocalizationHandler.getItem("app.dd.elemet.gui.unit"),
							"        "};
		
		for(int i=0; i < titles.length; i++){
			TableColumn column = new TableColumn(tableProperties, SWT.NULL);
			column.setText(titles[i]);
			column.setWidth(32);
		}
		
		updatePropertyTable();

    }
    
    public static Shell editDuctProfileGUI(Shell parent, ADuctElement element) {
		final Shell shell = new Shell(parent, SWT.SYSTEM_MODAL| SWT.CLOSE | SWT.MAX);
		shell.setLayout(new GridLayout());
		
		EditDuctProfileGUI gui = new EditDuctProfileGUI(shell, SWT.NONE, element);
		
		shell.setText(LocalizationHandler.getItem("app.dd.elemet.gui.profile.title")+": "+element.getName());
		
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
    
    private void updateProfile(){
    	element.setProfile(this.profileNew);
    }
    
    private void updatePropertyTable(){
    	
    	tableProperties.clearAll();
    	tableProperties.setItemCount(0);
    	
    	for(Control c: tableProperties.getChildren())
    		c.dispose();
    	
    	try {
			TableUtils.addCellEditor(tableProperties, this, new int[]{1});
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    	
    	TableItem itemType = new TableItem(tableProperties, SWT.NONE, 0);
    	
    	itemType.setText(0, LocalizationHandler.getItem("app.dd.elemet.gui.profile.shape"));
    	
    	comboProfile = new CCombo(tableProperties, SWT.NONE);
    	String[] comboItems = new String[candidates.length];
    	for(int i=0; i<candidates.length; i++)
    		comboItems[i] = candidates[i].getClass().getSimpleName().replace("HP", "");
    	comboProfile.setItems(comboItems);
    		
  
    	TableEditor editorCombo = new TableEditor(tableProperties);
    	comboProfile.setText(this.profileNew.getClass().getSimpleName().replace("HP", ""));
    	comboProfile.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent event){
				comboProfile.setEnabled(false);
				
				profileNew = candidates[comboProfile.getSelectionIndex()];
				profileNew.setParameterSet(allParameters);
				comboProfile.dispose();
				
				wasEdited();
				
				updatePropertyTable();
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
    	
        for(String key: this.profileNew.getParameterSet().getParameterSet().keySet()){
        	
        	final int idx = tableProperties.getItemCount();
        	final TableItem itemParam = new TableItem(tableProperties, SWT.NONE, idx);
        	
        	itemParam.setText(0, key);
        	itemParam.setText(1, allParameters.getPhysicalValue(key).getValue()+"");
        	itemParam.setText(2, allParameters.getPhysicalValue(key).getUnit().toString());
        }
        
        for(int i=0; i<allParameters.getParameterSet().size()-this.profileNew.getParameterSet().getParameterSet().size(); i++){
        	final int idx = tableProperties.getItemCount();
        	final TableItem itemParam = new TableItem(tableProperties, SWT.NONE, idx);
        	itemParam.setText(0, "");
        }
        
        
        
        TableColumn[] columns = tableProperties.getColumns();
        for (int j = 0; j < columns.length; j++) {
        	columns[j].pack();
        }
    }

	@Override
	public void save() {
		
		// Read new Config
		for(int i=1; i<tableProperties.getItemCount(); i++){
			try{
				allParameters.setPhysicalValue(tableProperties.getItem(i).getText(0),
						Double.valueOf(tableProperties.getItem(i).getText(1)), 
						new SiUnit(""+tableProperties.getItem(i).getText(2)));
			}
			catch(Exception e){
				System.err.println("Can not parse input '"+tableProperties.getItem(i).getText(0)+
						            "' as number: "+tableProperties.getItem(i).getText(1));
			}
		}
			
		profileNew.setParameterSet(allParameters);
		updateProfile();
		updatePropertyTable();
	}

	@Override
	public void reset() {
		
    	for(int i=0; i<candidates.length; i++) {
    		if(candidates[i].getClass().equals(element.getProfileIn().getClass())){
    			candidates[i].setParameterSet(element.getProfileIn().getParameterSet());
    			this.profileNew = candidates[i];
    		}
    		allParameters.getParameterSet().putAll(candidates[i].getParameterSet().getParameterSet());
    	}
    	
    	updatePropertyTable();
	}
}
