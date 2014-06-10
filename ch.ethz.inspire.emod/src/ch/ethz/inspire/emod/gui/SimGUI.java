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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * @author manick
 *
 */

public class SimGUI extends Composite {
	
	private Text textSimTitle;
	private Table tableSimParam;
	private Text textProcessTitle;
	private Table tableProcessParam;
	
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
		textSimTitle = new Text(this, SWT.MULTI);
		textSimTitle.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 1, 1));
		textSimTitle.setText(LocalizationHandler.getItem("app.gui.tabs.simtooltip"));
		
		//Tabelle für Maschinenmodell initieren
		tableSimParam = new Table(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		tableSimParam.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 1, 1));
		tableSimParam.setLinesVisible(true);
		tableSimParam.setHeaderVisible(true);
		
		//Titel der Spalten setzen
		//TODO: Werte in Languagepack übernehmen
		String[] aTitles =  {"Parameter", "initial Value"};
		for(int i=0; i < aTitles.length; i++){
			TableColumn column = new TableColumn(tableSimParam, SWT.NULL);
			column.setText(aTitles[i]);
		}
		
        for (int i = 0; i < 10; i++) {
            TableItem item = new TableItem(tableSimParam, SWT.NONE);
            item.setText(0, "Parameter " + i);
            item.setText(1, "Initial Value");
          }
		
        //Tabelle packen
        TableColumn[] columns = tableSimParam.getColumns();
        for (int i = 0; i < columns.length; i++) {
          columns[i].pack();
        }
        
        
        //versuch manick: inhalt tabelle editieren:
        //SOURCE http://www.tutorials.de/threads/in-editierbarer-swt-tabelle-ohne-eingabe-von-enter-werte-aendern.299858/
        //create a TableCursor to navigate around the table
        final TableCursor cursor = new TableCursor(tableSimParam, SWT.NONE);
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
                    //TableItem row = cursor.getRow();
                    //int column = cursor.getColumn();
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
		textProcessTitle = new Text(this, SWT.MULTI);
		textProcessTitle.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
		textProcessTitle.setText("Die Prozessparameter konfigurieren");
        
		//Tabelle für Prozess initieren
		tableProcessParam = new Table(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		//gridData.widthHint = 600;
		//gridData.heightHint = 300;
		tableProcessParam.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, true, 1, 1));
		tableProcessParam.setLinesVisible(true);
		tableProcessParam.setHeaderVisible(true);
		
		//Titel der Spalten setzen
		//TODO: Werte in Languagepack übernehmen
		String[] bTitles =  {"Time", "Parameter 1 Value", "Parameter 2 Value", "Parameter 3 Value", "Parameter 4 Value"};
		for(int i=0; i < bTitles.length; i++){
			TableColumn column = new TableColumn(tableProcessParam, SWT.NULL);
			column.setText(bTitles[i]);
		}
		
        for (int i = 0; i < 10; i++) {
            TableItem item = new TableItem(tableProcessParam, SWT.NONE);
            item.setText(0, "00:00");
            item.setText(1, "ABC");
            item.setText(2, "DEF");
            item.setText(3, "GHI");
            item.setText(4, "XYZ");
          }
		
        //Tabelle packen
        columns = tableProcessParam.getColumns();
        for (int i = 0; i < columns.length; i++) {
          columns[i].pack();
        }
        
	}
}

