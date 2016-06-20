package ch.ethz.inspire.emod.dd.gui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ch.ethz.inspire.emod.dd.Duct;
import ch.ethz.inspire.emod.dd.model.ADuctElement;
import ch.ethz.inspire.emod.dd.model.DuctDefinedValues;
import ch.ethz.inspire.emod.dd.model.DuctDrilling;
import ch.ethz.inspire.emod.dd.model.DuctElbowFitting;
import ch.ethz.inspire.emod.dd.model.DuctFitting;
import ch.ethz.inspire.emod.dd.model.DuctFlowAround;
import ch.ethz.inspire.emod.dd.model.DuctHelix;
import ch.ethz.inspire.emod.dd.model.DuctPipe;
import ch.ethz.inspire.emod.gui.AConfigGUI;
import ch.ethz.inspire.emod.gui.utils.ShowButtons;
import ch.ethz.inspire.emod.gui.utils.TableUtils;


public class DuctConfigGUI extends AConfigGUI{
	private SashForm form;
    private static Table tableDuctElements;
    private static Tree treeDuctDBView;
    private DuctTestingGUI ductTestingGUI;
    private TabFolder tabFolder;
    private Duct duct = new Duct();
    
    private ArrayList<Button> buttons = new ArrayList<Button>();
    
    private ADuctElement[] ductElementSelection = { new DuctDrilling(), new DuctFlowAround(), new DuctHelix(), new DuctPipe(), new DuctElbowFitting(), new DuctDefinedValues()};
    
    private ArrayList<String> elementNames = new ArrayList<String>();
    
    String name;

    /**
     * EditMachineComponentGUI
     * @param parent 
     * @param style 
     * @param duct 
     * @param buttons 
     * @param resetAvail 
     * @param name 
     */
    
    public DuctConfigGUI(Composite parent, int style, Duct duct, int buttons){
    	super(parent, style, buttons);
    	
    	this.getContent().setLayout(new GridLayout(1, true));
    	
    	tabFolder = new TabFolder(this.getContent(), SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    	
	    form = new SashForm(tabFolder, SWT.FILL);
	    form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		form.setLayout(new GridLayout(3, false));
		
		this.duct = duct;
		
		tableDuctElements = new Table(form, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
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
		
		treeDuctDBView = new Tree(form, SWT.BORDER);
		for(ADuctElement e: ductElementSelection){
			TreeItem childTreeItem     = new TreeItem(treeDuctDBView, SWT.NONE);
			childTreeItem.setText(e.getClass().getSimpleName().replace("Duct", ""));
		}
		
		
		ductTestingGUI = new DuctTestingGUI(tabFolder, duct);
		
		TabItem tabDuctDBItem = new TabItem(tabFolder, SWT.NONE);
		tabDuctDBItem.setText("Design");
		tabDuctDBItem.setToolTipText("Design");
		tabDuctDBItem.setControl(form); 
		
		TabItem tabTestingItem = new TabItem(tabFolder, SWT.NONE);
		tabTestingItem.setText("Analysis");
		tabTestingItem.setToolTipText("Testing");
		tabTestingItem.setControl(ductTestingGUI); 
		
		
		updateDuctElementTable();
		updateDuctTestingTable();
		
		// Add editor and cp
		try {
			TableUtils.addCellEditor(tableDuctElements, this.getClass().getDeclaredMethod("editDuctElementName"), this, new int[] {0});
		} catch (Exception e) {
			e.printStackTrace();
		}
		    
	    initDropTarget(tableDuctElements);
	    initElementDragSource(treeDuctDBView);
    }
    
    public void editDuctElementName(){
        
        for(int i=0; i<elementNames.size(); i++){
	        String newName = tableDuctElements.getItem(i).getText(0);
        	duct.setElementName(elementNames.get(i), newName);
        }
        updateDuctElementTable();
        
        wasEdited();
    }
    
    public void editDuctElementGUI(ADuctElement e){
    	Shell shell = EditDuctElementGUI.editDuctElementGUI(this.getShell(), e);
    	shell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				update();
			}
		});
    	
    }
    
    private void updateDuctTestingTable(){
    	ductTestingGUI.update();
    }
    
    private void updateDuctElementTable(){
    	tableDuctElements.clearAll();
    	tableDuctElements.setItemCount(0);
    	
    	elementNames.clear();
    	
    	for(Button b: buttons)
    		b.dispose();
    	
    	buttons.clear();
    	
    	
    	for(final ADuctElement e: duct.getElements()){
    		if(e instanceof DuctFitting)
    			continue;
    		
    		elementNames.add(e.getName());
    		
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
	        		update();
	        	}
	        	public void widgetDefaultSelected(SelectionEvent event){
	        		// Not used
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
		        	}
		        	public void widgetDefaultSelected(SelectionEvent event){
		        		// Not used
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
		        	}
		        	public void widgetDefaultSelected(SelectionEvent event){
		        		// Not used
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
	        	}
	        	public void widgetDefaultSelected(SelectionEvent event){
	        		// Not used
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
        
        form.layout();
    }
    
    public void update(){
    	updateDuctElementTable();
    	updateDuctTestingTable();
    	
    	this.redraw();
    	this.layout();
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
				// Not used
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
				// Not used
			}
			public void dragLeave(DropTargetEvent event){
				// Not used
			}
			public void dragOperationChanged(DropTargetEvent event) {
				// Not used
			}
			public void dropAccept(DropTargetEvent event){
				// Not used
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
		        }

			}
		});	
	}

	public static void editDuctGUI(String type, String parameter, String name) {
		editDuctGUI(type+"_"+parameter+"_"+name);
	}
	
	public static void editDuctGUI(String type) {
		final Shell shell = new Shell(Display.getCurrent());
		shell.setLayout(new GridLayout(1, true));
		DuctConfigGUI gui = new DuctConfigGUI(shell, SWT.NONE, Duct.buildFromDB(type), ShowButtons.ALL);
		
		shell.setText("DuctDesigner: "+type);
		
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
	}

	@Override
	public void save() {
		duct.save();
	}

	@Override
	public void reset() {
		duct.clone(Duct.buildFromDB(this.name));
		update();
	}

	public void setDuct(Duct duct) {
		this.duct.clone(duct);
		update();
	}
	
	public TabFolder getTabFolder(){
		return this.tabFolder;
	}
	
		
		
}
