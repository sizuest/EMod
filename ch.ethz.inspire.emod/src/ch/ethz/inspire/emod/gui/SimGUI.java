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
package ch.ethz.inspire.emod.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.LogLevel;
import ch.ethz.inspire.emod.gui.AnalysisGUI.MachineComponentComposite;
import ch.ethz.inspire.emod.gui.utils.BarChart;
import ch.ethz.inspire.emod.gui.utils.ConsumerData;
import ch.ethz.inspire.emod.gui.utils.LineChart;
import ch.ethz.inspire.emod.gui.utils.StackedAreaChart;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * @author manick
 *
 */

public class SimGUI extends Composite {
	
	private Text aText;
	private Table aTable;
	private Text bText;
	private Table bTable;
	
	/**
	 * @param parent
	 */
	public SimGUI(Composite parent) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(1, false));
		init();
	}

	
	public void init() {
		//Überschrift des Fensters Simulation
		aText = new Text(this, SWT.MULTI);
		GridData gridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false);
		gridData.horizontalSpan = 1;
		aText.setLayoutData(gridData);
		aText.setText(LocalizationHandler.getItem("app.gui.tabs.simtooltip"));
		
		//Tabelle für Maschinenmodell initieren
		aTable = new Table(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		gridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false);
		gridData.horizontalSpan = 1;
		aTable.setLayoutData(gridData);
		aTable.setLinesVisible(true);
		aTable.setHeaderVisible(true);
		
		//Titel der Spalten setzen
		//TODO manick: Werte in Languagepack übernehmen
		String[] aTitles =  {"Parameter", "initial Value"};
		for(int i=0; i < aTitles.length; i++){
			TableColumn column = new TableColumn(aTable, SWT.NULL);
			column.setText(aTitles[i]);
		}
		
        for (int i = 0; i < 10; i++) {
            TableItem item = new TableItem(aTable, SWT.NONE);
            item.setText(0, "Parameter " + i);
            item.setText(1, "Initial Value");
          }
		
        //Tabelle packen
        TableColumn[] columns = aTable.getColumns();
        for (int i = 0; i < columns.length; i++) {
          columns[i].pack();
        }
        
        
        //versuch manick: inhalt tabelle editieren:
        //SOURCE http://www.tutorials.de/threads/in-editierbarer-swt-tabelle-ohne-eingabe-von-enter-werte-aendern.299858/
        //create a TableCursor to navigate around the table
        final TableCursor cursor = new TableCursor(aTable, SWT.NONE);
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
                    TableItem row = cursor.getRow();
                    int column = cursor.getColumn();
                    text.append(String.valueOf(e.character));
                    text.addKeyListener(new KeyAdapter() {
                        public void keyPressed(KeyEvent e) {
                            // close the text editor and copy the data over
                            // when the user hits "ENTER"
                            if (e.character == SWT.CR) {
                                TableItem row = cursor.getRow();
                                int column = cursor.getColumn();
                                row.setText(column, text.getText());
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
        
        
        
        
		//Überschrift des Fensters Simulation
		bText = new Text(this, SWT.MULTI);
		gridData = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		gridData.horizontalSpan = 1;
		bText.setLayoutData(gridData);
		bText.setText("Die Prozessparameter konfigurieren");
        
		//Tabelle für Prozess initieren
		bTable = new Table(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		gridData = new GridData(GridData.BEGINNING, GridData.FILL, false, true);
		gridData.horizontalSpan = 1;
		//gridData.widthHint = 600;
		//gridData.heightHint = 300;
		bTable.setLayoutData(gridData);
		bTable.setLinesVisible(true);
		bTable.setHeaderVisible(true);
		
		//Titel der Spalten setzen
		//TODO manick: Werte in Languagepack übernehmen
		String[] bTitles =  {"Time", "Parameter 1 Value", "Parameter 2 Value", "Parameter 3 Value", "Parameter 4 Value"};
		for(int i=0; i < bTitles.length; i++){
			TableColumn column = new TableColumn(bTable, SWT.NULL);
			column.setText(bTitles[i]);
		}
		
        for (int i = 0; i < 10; i++) {
            TableItem item = new TableItem(bTable, SWT.NONE);
            item.setText(0, "00:00");
            item.setText(1, "ABC");
            item.setText(2, "DEF");
            item.setText(3, "GHI");
            item.setText(4, "XYZ");
          }
		
        //Tabelle packen
        columns = bTable.getColumns();
        for (int i = 0; i < columns.length; i++) {
          columns[i].pack();
        }
        
	}
}

