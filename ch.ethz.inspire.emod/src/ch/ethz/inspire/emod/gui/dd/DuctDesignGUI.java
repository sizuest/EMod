package ch.ethz.inspire.emod.gui.dd;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ch.ethz.inspire.emod.gui.utils.TableUtils;
import ch.ethz.inspire.emod.model.fluid.ADuctElement;
import ch.ethz.inspire.emod.model.fluid.Duct;
import ch.ethz.inspire.emod.model.fluid.DuctDrilling;
import ch.ethz.inspire.emod.model.fluid.DuctFitting;
import ch.ethz.inspire.emod.model.fluid.DuctFlowAround;
import ch.ethz.inspire.emod.model.fluid.DuctHelix;
import ch.ethz.inspire.emod.model.fluid.DuctPipe;
import ch.ethz.inspire.emod.model.fluid.DuctElbowFitting;
import ch.ethz.inspire.emod.model.material.Material;
import ch.ethz.inspire.emod.model.units.SiUnit;


public class DuctDesignGUI {
	private Shell shell;
    private static Table tableDuctElements;
    private static Table tableDuctStatistics;
    private static Tree treeDuctDBView;
    private static Button saveButton, closeButton;
    private DuctTestingGUI ductTestingGUI;
    private TabFolder tabFolderDesign;
    private Duct duct;
    
    private ArrayList<Button> buttons = new ArrayList<Button>();
    
    private ADuctElement[] ductElementSelection = { new DuctDrilling(), new DuctFlowAround(), new DuctHelix(), new DuctPipe(), new DuctElbowFitting()};

    /**
     * EditMachineComponentGUI
     */
    public DuctDesignGUI(){}
    
    public void editDuctGUI(String name){
	    shell = new Shell(Display.getCurrent());
	    shell.setText("Duct design test");
		shell.setLayout(new GridLayout(3, false));
		
		duct = Duct.buildFromFile(name);
		
		tableDuctElements = new Table(shell, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		tableDuctElements.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tableDuctElements.setLinesVisible(true);
		tableDuctElements.setHeaderVisible(true);
		
		String[] titles =  {"Element",
				"Type",
				"        ",
				"        ",
				"        ",
				"        "};
		
		for(int i=0; i < titles.length; i++){
			TableColumn column = new TableColumn(tableDuctElements, SWT.NULL);
			column.setText(titles[i]);
			column.setWidth(32);
		}
		
		tabFolderDesign = new TabFolder(shell, SWT.NONE);
		tabFolderDesign.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		saveButton = new Button(shell, SWT.NONE);
		saveButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionListener(){
        	public void widgetSelected(SelectionEvent event){
        		duct.save();
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
		
		treeDuctDBView = new Tree(tabFolderDesign, SWT.NONE);
		for(ADuctElement e: ductElementSelection){
			TreeItem childTreeItem     = new TreeItem(treeDuctDBView, SWT.NONE);
			childTreeItem.setText(e.getClass().getSimpleName().replace("Duct", ""));
		}
		
		tableDuctStatistics = new Table(tabFolderDesign, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		tableDuctStatistics.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableDuctStatistics.setLinesVisible(true);
		tableDuctStatistics.setHeaderVisible(true);
		
		ductTestingGUI = new DuctTestingGUI(tabFolderDesign, duct);
		
		TabItem tabDuctDBItem = new TabItem(tabFolderDesign, SWT.NONE);
		tabDuctDBItem.setText("Elements");
		tabDuctDBItem.setToolTipText("Elements");
		tabDuctDBItem.setControl(treeDuctDBView); 

		TabItem tabStatisticsItem = new TabItem(tabFolderDesign, SWT.NONE);
		tabStatisticsItem.setText("Statistics");
		tabStatisticsItem.setToolTipText("Statistics");
		tabStatisticsItem.setControl(tableDuctStatistics); 
		
		TabItem tabTestingItem = new TabItem(tabFolderDesign, SWT.NONE);
		tabTestingItem.setText("Analysis");
		tabTestingItem.setToolTipText("Testing");
		tabTestingItem.setControl(ductTestingGUI); 
		
		
		
		String[] titlesStat =  {"Parameter", "Value", "Unit"};
		
		for(int i=0; i < titlesStat.length; i++){
			TableColumn column = new TableColumn(tableDuctStatistics, SWT.NULL);
			column.setText(titlesStat[i]);
		}
		
		updateDuctElementTable();
		updateDuctStatisticTable();
		updateDuctTestingTable();
		
		// Add editor and cp
		try {
			TableUtils.addCellEditor(tableDuctElements, this.getClass().getDeclaredMethod("editDuctElementName", TableCursor.class, Text.class), this);
			TableUtils.addCopyToClipboard(tableDuctStatistics);
		} catch (Exception e) {
			e.printStackTrace();
		}
		    
	    initDropTarget(tableDuctElements);
	    initElementDragSource(treeDuctDBView);
		
		shell.pack();
		
		shell.open();
		
    }
    
    public void editDuctElementName(TableCursor cursor, Text text){
    	
        TableItem row = cursor.getRow();
        int column = cursor.getColumn();
        final String oldName = row.getText(column);
        switch(column){
        	case 0:
            	duct.setElementName(oldName, text.getText());
                updateDuctElementTable();
                break;
        }
    }
    
    public void editDuctElementGUI(ADuctElement e){
    	new EditDuctElementGUI(this, e);
    }
    
    private void updateDuctTestingTable(){
    	ductTestingGUI.update();
    }
    
    private void updateDuctStatisticTable(){
    	tableDuctStatistics.clearAll();
    	tableDuctStatistics.setItemCount(0);
    	
    	TableItem itemCount   = new TableItem(tableDuctStatistics, SWT.NONE, 0);
    	TableItem itemLength  = new TableItem(tableDuctStatistics, SWT.NONE, 1);
    	TableItem itemSurface = new TableItem(tableDuctStatistics, SWT.NONE, 2);
    	TableItem itemVolume  = new TableItem(tableDuctStatistics, SWT.NONE, 3);
    	
    	itemCount.setText(0, "Number of elements");
    	itemCount.setText(1, duct.getElements().size()+"");
    	
    	itemLength.setText(0, "Length");
    	itemLength.setText(1, String.format("%6.3e", duct.getLength()));
    	itemLength.setText(2, (new SiUnit("m")).toString());
    	
    	itemSurface.setText(0, "Surface");
    	itemSurface.setText(1, String.format("%6.3e", duct.getSurface()));
    	itemSurface.setText(2, (new SiUnit("m^2")).toString());
    	
    	itemVolume.setText(0, "Volume");
    	itemVolume.setText(1, String.format("%6.3e", duct.getVolume()));
    	itemVolume.setText(2, (new SiUnit("m^3")).toString());
    	
    	
    	TableColumn[] columns = tableDuctStatistics.getColumns();
        for (int j = 0; j < columns.length; j++) {
        	columns[j].pack();
        }
        
        shell.pack();
    	
    }
    
    private void updateDuctElementTable(){
    	tableDuctElements.clearAll();
    	tableDuctElements.setItemCount(0);
    	
    	for(Button b: buttons)
    		b.dispose();
    	
    	buttons.clear();
    	
    	
    	for(final ADuctElement e: duct.getElements()){
    		if(e instanceof DuctFitting)
    			continue;
    		
			int i                    = tableDuctElements.getItemCount();
			final TableItem itemProp = new TableItem(tableDuctElements, SWT.NONE, i);
			TableEditor editorButton = new TableEditor(tableDuctElements);
			
			itemProp.setText(0, e.getName());
			itemProp.setText(1, e.getClass().getSimpleName().replace("Duct", ""));
			
			if(e instanceof DuctFitting)
				continue;
			
			/* Edit */
			final Button editElementButton = new Button(tableDuctElements, SWT.PUSH);
			buttons.add(editElementButton);
	        Image imageEdit = new Image(Display.getDefault(), "src/resources/Edit16.gif");
	        editElementButton.setImage(imageEdit);
	        editElementButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
	        editElementButton.addSelectionListener(new SelectionListener(){
	        	public void widgetSelected(SelectionEvent event){
	        		editDuctElementGUI(e);
	        		updateDuctElementTable();
	        		updateDuctStatisticTable();
	        	}
	        	public void widgetDefaultSelected(SelectionEvent event){
	        		
	        	}
	        });
	        editElementButton.pack();
			editorButton.minimumWidth = editElementButton.getSize().x;
			editorButton.horizontalAlignment = SWT.LEFT;
	        editorButton.setEditor(editElementButton, itemProp, 2);
	        
	        /* Up */
	        if(duct.getElementIndex(e.getName())!=0){
		        editorButton = new TableEditor(tableDuctElements);
		        final Button moveElementUpButton = new Button(tableDuctElements, SWT.PUSH);
		        buttons.add(moveElementUpButton);
		        Image imageUp = new Image(Display.getDefault(), "src/resources/Up16.gif");
				moveElementUpButton.setImage(imageUp);
		        moveElementUpButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
		        moveElementUpButton.addSelectionListener(new SelectionListener(){
		        	public void widgetSelected(SelectionEvent event){
		        		duct.moveElementUp(e);
		        		updateDuctElementTable();
		        		updateDuctStatisticTable();
		        	}
		        	public void widgetDefaultSelected(SelectionEvent event){
		        		
		        	}
		        });
		        moveElementUpButton.pack();
				editorButton.minimumWidth = moveElementUpButton.getSize().x;
				editorButton.horizontalAlignment = SWT.LEFT;
		        editorButton.setEditor(moveElementUpButton, itemProp, 3);
	        }
	        
	        /* Down */
	        if(duct.getElementIndex(e.getName())!=duct.getElements().size()-1){
		        editorButton = new TableEditor(tableDuctElements);
		        final Button moveElementDownButton = new Button(tableDuctElements, SWT.PUSH);
		        buttons.add(moveElementDownButton);
		        Image imageDown = new Image(Display.getDefault(), "src/resources/Down16.gif");
				moveElementDownButton.setImage(imageDown);
		        moveElementDownButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
		        moveElementDownButton.addSelectionListener(new SelectionListener(){
		        	public void widgetSelected(SelectionEvent event){
		        		duct.moveElementDown(e);
		        		updateDuctElementTable();
		        		updateDuctStatisticTable();
		        	}
		        	public void widgetDefaultSelected(SelectionEvent event){
		        		
		        	}
		        });
		        moveElementDownButton.pack();
				editorButton.minimumWidth = moveElementDownButton.getSize().x;
				editorButton.horizontalAlignment = SWT.LEFT;
		        editorButton.setEditor(moveElementDownButton, itemProp, 4);
	        }
	        
	        /* Remove */
	        editorButton = new TableEditor(tableDuctElements);
	        final Button removeElementButton = new Button(tableDuctElements, SWT.PUSH);
	        buttons.add(removeElementButton);
	        Image imageDelete = new Image(Display.getDefault(), "src/resources/Delete16.gif");
	        removeElementButton.setImage(imageDelete);
	        removeElementButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, true, 1, 1));
	        removeElementButton.addSelectionListener(new SelectionListener(){
	        	public void widgetSelected(SelectionEvent event){
	        		duct.removeElement(e.getName());
	        		updateDuctElementTable();
	        		updateDuctStatisticTable();
	        	}
	        	public void widgetDefaultSelected(SelectionEvent event){
	        		
	        	}
	        });
	        removeElementButton.pack();
			editorButton.minimumWidth = removeElementButton.getSize().x;
			editorButton.horizontalAlignment = SWT.LEFT;
	        editorButton.setEditor(removeElementButton, itemProp, 5);
		}
		
		TableColumn[] columns = tableDuctElements.getColumns();
        for (int j = 0; j < columns.length; j++) {
        	columns[j].pack();
        }
        
        shell.pack();
    }
    
    public void update(){
    	updateDuctElementTable();
    	updateDuctStatisticTable();
    	updateDuctTestingTable();
    }
    
    private void initElementDragSource(final Tree treeElementDBView){
		//set tree as dragsource for the DnD of the components
		int operations = DND.DROP_COPY;
		final DragSource source = new DragSource(treeElementDBView, operations);
		
		//SOURCE for drag source:
		//http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html
		
		//DnD shall transfer text of the selected element
		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
		source.setTransfer(types);
		
		//create draglistener to transfer text of selected tree element
		source.addDragListener(new DragSourceListener() {
			private TreeItem[] selection = null;
			
			//at drag start, get the selected tree element
			public void dragStart(DragSourceEvent event){
				selection = treeElementDBView.getSelection();
			}
			
			//set the text of the selected tree element as event data
			public void dragSetData(DragSourceEvent event){
				String text = "";
				for(TreeItem item:selection){
					text += (String)item.getText();	
				}
				event.data = text;
			}
			
			//nothing needs to be done at the end of the drag
			public void dragFinished(DragSourceEvent event) {
			
			}
		});
	}
    
    private void initDropTarget(final Table tableModelView){
		//set table as drop target
		int operations = DND.DROP_COPY;
		DropTarget target = new DropTarget(tableModelView, operations);
		
		//SOURCE for drop target:
		//http://www.eclipse.org/articles/Article-SWT-DND/DND-in-SWT.html
		
		//only accept texttransfer
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] types = new Transfer[] {textTransfer};
		target.setTransfer(types);
		
		//add drop listener to the target
		target.addDropListener(new DropTargetListener(){
			//show copy icon at mouse pointer
			public void dragEnter(DropTargetEvent event){
				event.detail = DND.DROP_COPY;
			}
			public void dragOver(DropTargetEvent event){

			}
			public void dragLeave(DropTargetEvent event){
				
			}
			public void dragOperationChanged(DropTargetEvent event) {
				
			}
			public void dropAccept(DropTargetEvent event){
				
			}
			//only action is required when element is dropped
			public void drop(DropTargetEvent event){
				//collect string of drag
				String string = null;
		        string = (String) event.data;
		        
		        //get position of the drop		        
				Point p = event.display.map(null, tableModelView, event.x, event.y);
				TableItem dropItem = tableModelView.getItem(p);
				int index = dropItem == null ? tableModelView.getItemCount() : tableModelView.indexOf(dropItem);
				
				ADuctElement e = Duct.newDuctElement(string);
				
				if(null!=e){
					duct.addElement(index, e);
					updateDuctElementTable();
					updateDuctStatisticTable();
		        }

			}
		});	
	}

	public void editDuctGUI(String type, String parameter, String name) {
		editDuctGUI(type+"_"+parameter+"_"+name);
	}
		
		
}
