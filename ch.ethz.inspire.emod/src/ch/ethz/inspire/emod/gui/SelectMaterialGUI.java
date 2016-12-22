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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import ch.ethz.inspire.emod.gui.utils.MaterialHandler;
import ch.ethz.inspire.emod.utils.LocalizationHandler;

/**
 * Implements a Material selection GUI;
 * 
 * @author sizuest
 * 
 */
public class SelectMaterialGUI extends Dialog {
	private String input;

	// tree to list all the materials
	private Tree treeMaterialDBView;

	/**
	 * SelectMaterialGUI
	 * 
	 * @param parent
	 */
	public SelectMaterialGUI(Shell parent) {
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}

	/**
	 * New material selection gui
	 * @param parent
	 * @param style
	 */
	public SelectMaterialGUI(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Open the selection GUI
	 * @return
	 */
	public String open() {
		Shell shell = new Shell(getParent(), getStyle());
		shell.setText(LocalizationHandler.getItem("app.gui.matdb.title"));
		shell.setSize(400, 600);
		shell.setLayout(new GridLayout(2, false));

		createContents(shell);
		shell.pack();
		shell.layout();
		shell.open();

		shell.setSize(400, 600);

		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		// Return the entered value, or null
		return input;

	}

	private void createContents(final Shell shell) {
		// create tree element and fill it with the components from the DB
		treeMaterialDBView = new Tree(shell, SWT.SINGLE | SWT.BORDER
				| SWT.V_SCROLL);
		treeMaterialDBView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 2, 1));
		MaterialHandler.fillTree(treeMaterialDBView);

		/* Select Button */
		final Button selectComponentButton = new Button(shell, SWT.PUSH);
		selectComponentButton.setText("OK");
		selectComponentButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP,
				false, false, 1, 1));
		selectComponentButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				input = getSelectionToString();
				shell.close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				// Not used
			}
		});

		/* Close Button */
		final Button closeComponentButton = new Button(shell, SWT.PUSH);
		closeComponentButton.setText("Close");
		closeComponentButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP,
				false, false, 1, 1));
		closeComponentButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				input = "";
				shell.close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				// Not used
			}
		});

		shell.setDefaultButton(selectComponentButton);
	}

	private String getSelectionToString() {
		return treeMaterialDBView.getSelection()[0].getText();
	}

}
