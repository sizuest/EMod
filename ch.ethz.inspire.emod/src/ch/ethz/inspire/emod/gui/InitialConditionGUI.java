package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.ethz.inspire.emod.Machine;
import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.simulation.DynamicState;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

public class InitialConditionGUI extends AConfigGUI{
	
	private Table tableSimParam;

	public InitialConditionGUI(Composite parent, int style) {
		super(parent, style, false);
		
		//Machine.loadInitialConditions();
		
		//Tabelle fuer Maschinenmodell initieren
		tableSimParam = new Table(this.getContent(), SWT.BORDER);
		tableSimParam.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableSimParam.setLinesVisible(true);
		tableSimParam.setHeaderVisible(true);
		
		
		//Titel der Spalten setzen
		String[] aTitles =  {
				LocalizationHandler.getItem("app.gui.sim.initialconditions.component"),
				LocalizationHandler.getItem("app.gui.sim.initialconditions.state"),
				LocalizationHandler.getItem("app.gui.sim.initialconditions.value"),
				LocalizationHandler.getItem("app.gui.sim.initialconditions.unit")};
		
		for(int i=0; i < aTitles.length; i++){
			TableColumn column = new TableColumn(tableSimParam, SWT.NULL);
			column.setText(aTitles[i]);
		}
	    
	    try {
			TableUtils.addCellEditor(tableSimParam, this, new int[] {2});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void update(){
		tableSimParam.clearAll();
		tableSimParam.setItemCount(0);
		
		try{
			for (DynamicState s:Machine.getInstance().getDynamicStatesList()){
				TableItem item = new TableItem(tableSimParam, SWT.NONE);
				item.setText(0, s.getParent());
				item.setText(1, s.getName());
				item.setText(2, s.getInitialValue()+"");
				item.setText(3, s.getUnit().toString());
			}
		}
		catch(Exception e){
			System.err.println("Failed to display initial states.");
			e.printStackTrace();
		}
		
        //Tabelle packen
        TableColumn[] columns = tableSimParam.getColumns();
        for (int i = 0; i < columns.length; i++) {
        	columns[i].pack();
        }
	}

	@Override
	public void save() {
		
		for(TableItem ti:tableSimParam.getItems()){
			Machine.getInstance().getDynamicState(ti.getText(1), ti.getText(0)).setInitialCondition(Double.parseDouble(ti.getText(2)));
		}
		
		Machine.saveInitialConditions();
	}

	@Override
	public void reset() {
		Machine.loadInitialConditions();
		update();
		
	}
}
