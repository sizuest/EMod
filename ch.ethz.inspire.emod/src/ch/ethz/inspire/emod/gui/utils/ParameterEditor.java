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
package ch.ethz.inspire.emod.gui.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PopupList;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.utils.ConfigReader;

/**
 * @author simon
 *
 */
public class ParameterEditor extends Composite{
	
	protected String text = "";
	protected ParameterType type = ParameterType.NONE;
	
	/* Input composites for the different types */
	private String inputBoolean;
	protected Text inputString;
	protected Text inputNumber;
	protected Table inputArray;
	protected Table inputMatrix;
	
	private static final String COL_SEP = ",";
	private static final String ROW_SEP = ";";
	
	protected TableCursor cursor;

	/**
	 * @param style
	 * @param cursor 
	 */
	public ParameterEditor(int style, TableCursor cursor) {
		super(cursor, style);
		
		this.cursor = cursor;
	}
	
	/**
	 * Set the represented text
	 * @param text
	 */
	public void setText(String text){
		this.text = text;
		this.type = getInputType(text);
		
		addInput();
	}
	
	/**
	 * Get the input text
	 * @return 
	 */
	public String getText(){
		switch(type){
		case ARRAY:
			return getArrayValue();
		case BOOLEAN:
			return getBooleanValue();
		case MATRIX:
			return getMatrixValue();
		case NUMBER:
			return getNumericValue();
		case STRING:
			return getStringValue();
		default:
			return "";		
		}
	}
	
	/**
	 * add input
	 */
	private void addInput(){
		// Remove everything
		removeAll();
		
		this.setLayout(new FillLayout());
		
		switch(type){
		case BOOLEAN:
			createPopUp();
			break;
		case ARRAY:
			inputArray = createMatrixEditor(100, 1);
			break;
		case MATRIX:
			inputMatrix = createMatrixEditor(100, 100);
		case NUMBER:
			inputNumber = createText();
			break;
		default:
			inputString = createText();;
			break;
		
		}
	}
	
	private void createPopUp(){
		final PopupList popup = new PopupList(this.getShell());
		popup.setItems(new String[] {"false", "true"});
		
		popup.select(text);
		
		Rectangle bounds = cursor.getParent().getBounds();
		Point point = cursor.getParent().toDisplay(bounds.x, bounds.y);

		Rectangle position = new Rectangle(point.x+cursor.getBounds().x, point.y+cursor.getBounds().y, cursor.getBounds().width, 0);
		
		System.out.println(point.toString());
		System.out.println(bounds.toString() +"  ||  "+position.toString());
		
		inputBoolean = popup.open(position);
		
		changeTableEntry();
		
		dispose();
	}
	
	private Text createText(){
		Text textField = new Text(this, SWT.NONE);
		textField.setText(text);
		textField.selectAll();
		textField.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				changeTableEntry();
				dispose();
			}
			
			@Override
			public void focusGained(FocusEvent e) {/* Not used */}
		});
		
		return textField;
	}
	
	/**
	 * Array Editor
	 */
	private Table createMatrixEditor(int rows, int cols){
		
		/* Shell for editor */
		final Shell shell = new Shell(cursor.getShell(), SWT.APPLICATION_MODAL | SWT.NONE | SWT.NO_TRIM );
		shell.setText("Matrix Editor");
		GridLayout layout = new GridLayout(1, true);
	    layout.horizontalSpacing = 0;
	    layout.verticalSpacing = 0;
		shell.setLayout(layout);
		
		/* Button to close */
		Button button = new Button(shell, SWT.FLAT | SWT.NO_TRIM);
		button.setText("Close");
		button.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		button.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				changeTableEntry();
				shell.dispose();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		/* Table for value display */
		final Table table = new Table(shell, SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_TRIM);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// Colums
		for(int i=0; i<cols+1; i++) {
			TableColumn col = new TableColumn(table, SWT.NULL);
			if(i==0)
				col.setText("  ");
			else
				col.setText(getColumnName(i-1));
		}
		// Rows
		for(int i=0; i<rows; i++) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, ""+(i+1));
		}
		
		
		/* Fill with values */
		fillTable(table, text, rows, cols);
		
		/* Layout of the table */
		int maxWidth = 0;
		TableColumn[] columns = table.getColumns();
		for (int j = 0; j < columns.length; j++) {
			columns[j].pack();
			maxWidth = Math.max(columns[j].getWidth(), maxWidth);
		}
		
		for (int j = 0; j < columns.length; j++) {
			columns[j].setWidth(maxWidth);
		}
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		try {
			int [] idx=new int[cols];
			for (int i=0; i <cols; i++)
				idx[i]=i+1;

			TableUtils.addCellEditor(table, idx);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		shell.open();
		
		shell.setMinimumSize(200, 300);
		shell.pack();
		
		Rectangle bounds = cursor.getParent().getBounds();
		Point point = cursor.getParent().toDisplay(bounds.x, bounds.y);

		Rectangle position = new Rectangle(point.x+cursor.getBounds().x, point.y+cursor.getBounds().y, cursor.getBounds().width, 200);
		
		shell.setBounds(position);
		
		shell.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				//table.setFocus();
				//changeTableEntry();
				dispose();
				cursor.getParent().setEnabled(true);
			}
		});
		
		cursor.getParent().setEnabled(false);
		
		return table;
		
	}
	
	/**
	 * @param i
	 * @return
	 */
	private String getColumnName(int i) {
		String out;
		if(i<26)
			out =  ""+(char)(i+65);
		else
			out =  getColumnName(i/26-1)+getColumnName(i%26);
		
		return out;
	}

	private void fillTable(Table table, String data, int rows, int cols){
		double[][] val = ConfigReader.stringToDoubleMatrix(data);
		
		int numCols = Math.min(val[0].length, cols);
		int numRows = Math.min(val.length, rows);
		
		if(val.length == 1){
			numCols = Math.min(val[0].length, rows);
			for(int r=0; r<numCols; r++){
				table.getItem(r).setText(1, ""+val[0][r]);
			}
		}
		else{
		
			for(int r=0; r<numRows; r++){
				for(int c=0; c<numCols; c++)
					table.getItem(r).setText(c+1, val[r][c]+"");
			}
		}
		
		
	}
	
	/**
	 * 
	 */
	private void removeAll() {
		if(inputArray != null){
			inputArray.dispose();
		}
		if(inputMatrix != null){
			inputMatrix.dispose();
		}
		if(inputNumber != null){
			inputNumber.dispose();
		}
		if(inputString != null){
			inputString.dispose();
		}
	}


	/**
	 * Determine the kind of input field to be used
	 */
	private ParameterType getInputType(String text){
		
		/* Boolean */
		if(text.contentEquals("true") | text.contentEquals("false"))
			return ParameterType.BOOLEAN;
		
		/* Numeric */
		try{
			Double.valueOf(text);
			return ParameterType.NUMBER;
		} catch(Exception e){}
		
		/* Array & Matrix */
		try{
			double[][] val = ConfigReader.stringToDoubleMatrix(text);
			
			if(val.length == 1 | val[0].length == 1)
				return ParameterType.ARRAY;
			else
				return ParameterType.MATRIX;
			
		}catch(Exception e){
			return ParameterType.STRING;
		}

	}
	
	
	/**
	 * Method to get selected boolean value
	 * @return
	 */
	private String getBooleanValue(){
		if(null == inputBoolean)
			return "false";
		
		return inputBoolean;
	}
	
	/**
	 * Method to get a string input
	 */
	private String getStringValue(){
		if(null == inputString)
			return "";
		if(inputString.isDisposed()) 
			return "";
		
		return inputString.getText();
	}
	
	/**
	 * Method to get numeric input
	 */
	private String getNumericValue(){
		if(null == inputNumber)
			return "";
		if(inputNumber.isDisposed()) 
			return "";
		
		return inputNumber.getText();
	}
	
	/**
	 * Method to get an array input
	 */
	private String getArrayValue(){		
		return table2String(inputArray);
	}
	
	/**
	 * Method to get a matrix input
	 */
	private String getMatrixValue(){		
		return table2String(inputMatrix);
	}
	
	/**
	 * Convert a table to a string
	 * @param table
	 * @return
	 */
	private String table2String(Table table){
		if(null == table)
			return "";
		if(table.isDisposed()) 
			return "";
		
		// Size of the table
		int numCols = table.getColumnCount();
		int numRows = table.getItemCount();
		
		// Fetch values
		double[][] vals = new double[numRows][numCols-1];
		
		for(int r=0; r<numRows; r++){
			for(int c=1; c<numCols; c++){
				try{
					vals[r][c-1]=Double.valueOf(table.getItem(r).getText(c));
				} catch(Exception e){
					vals[r][c-1] = Double.NaN;
				}
			}
		}
		
		// Identify size
		rLoop: for(int r=numRows-1; r>=0; r--){
			for(int c=numCols-1; c>=1; c--){
				if(!Double.isNaN(vals[r][c-1])){
					numRows = r+1;
					break rLoop;
				}
			}
		}
		
		cLoop: for(int c=numCols-1; c>=1; c--){
			for(int r=numRows-1; r>=0; r--){
				if(!Double.isNaN(vals[r][c-1])){
					numCols = c;
					break cLoop;
				}
			}
		}
		
		
		String rowSep = (numCols<2) ? COL_SEP : ROW_SEP;
		String colSep = (numCols<2) ? "" : COL_SEP;
		
		String out = "";
		
		for(int r=0; r<numRows; r++){
			for(int c=0; c<numCols; c++){
				out+=vals[r][c]+colSep;
			}
			out+=rowSep;
		}
		
		return out;
	}
	
	
	
	/**
	 * Types of parameter editors
	 * @author simon
	 *
	 */
	public enum ParameterType {
		/**
		 * Default
		 */
		NONE,
		/**
		 * Boolean
		 */
		BOOLEAN,
		/**
		 * String
		 */
		STRING,
		/**
		 * Scalar
		 */
		NUMBER,
		/**
		 * Vector
		 */
		ARRAY,
		/**
		 * Matrix
		 */
		MATRIX;
	}
	
	/**
	 * Write the text to the cursor
	 */
	private void changeTableEntry() {
		try{
			TableItem row = cursor.getRow();
			int column = cursor.getColumn();
			row.setText(column, this.getText());
		} catch(Exception e){
			e.printStackTrace();
		}
	}

}

