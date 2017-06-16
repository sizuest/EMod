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

import java.io.File;
import java.util.ArrayList;

import ch.ethz.inspire.emod.EModSession;
import ch.ethz.inspire.emod.Process;
import ch.ethz.inspire.emod.utils.Defines;
import ch.ethz.inspire.emod.utils.PropertiesHandler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * GUI to manage the and edit the process settings
 * @author sizuest
 *
 */
public class ProcessManageGUI extends Composite {

	protected CCombo comboSelectProcess;
	protected Button buttonNew, buttonRemove;

	/**
	 * @param parent
	 * @param style
	 */
	public ProcessManageGUI(final ProcessGUI parent, int style) {
		super(parent.getContent(), style);

		this.setLayout(new GridLayout(3, false));
		this.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));

		comboSelectProcess = new CCombo(this, SWT.PUSH );
		comboSelectProcess.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				EModSession.setProcessName(getSelectedName());
				Process.loadProcess(getSelectedName());
				update();
				parent.update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */
			}
		});

		buttonNew = new Button(this, SWT.PUSH);
		buttonNew.setText("New");
		buttonNew.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				newProcessGUI();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */
			}
		});

		buttonRemove = new Button(this, SWT.PUSH);
		buttonRemove.setText("Remove");
		buttonRemove.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Process.removeProcess(getSelectedName());
				update();
				parent.update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */
			}
		});

		fillCombo();
	}

	/**
	 * Fill the combo with the available processes
	 */
	private void fillCombo() {
		/* find available process files */
		String path = PropertiesHandler
				.getProperty("app.MachineDataPathPrefix")
				+ "/"
				+ EModSession.getMachineName()
				+ "/"
				+ Defines.SIMULATIONCONFIGDIR
				+ "/"
				+ EModSession.getSimulationConfig();

		File procDir = new File(path);
		ArrayList<String> procFiles = new ArrayList<String>();

		String[] files = procDir.list();

		if (null == files)
			return;

		for (int i = 0; i < files.length; i++) {
			if (files[i].contains(Defines.PROCESSDEFFILE_PREFIX))
				procFiles.add(files[i].replace(Defines.PROCESSDEFFILE_PREFIX, "").replace(".xml", ""));
		}

		// set the possible parameter sets to the combo
		comboSelectProcess.setItems(procFiles.toArray(new String[procFiles.size()]));
		comboSelectProcess.setText(EModSession.getProcessName());

	}

	@Override
	public void update() {
		this.redraw();
		this.pack();
		fillCombo();
	}

	/**
	 * Returns current
	 * 
	 * @return
	 */
	public String getSelectedName() {
		return comboSelectProcess.getText();
	}

	private void newProcessGUI() {
		final Shell shell = new Shell(Display.getCurrent());

		shell.setText("new process name");
		shell.setLayout(new GridLayout(2, false));
		shell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				update();
			}
		});

		// Text input for new name
		final Text text = new Text(shell, SWT.BORDER);
		text.setMessage("New process name");
		text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

		// Button for cancel
		Button buttonCancel = new Button(shell, SWT.PUSH);
		buttonCancel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false, 1, 1));
		buttonCancel.setText("Cancel");
		buttonCancel.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */
			}
		});

		// Button for create
		Button buttonCreate = new Button(shell, SWT.PUSH);
		buttonCreate.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false,
				false, 1, 1));
		buttonCreate.setText("Create");
		buttonCreate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EModSession.newProcess(text.getText());
				update();
				shell.dispose();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {/* Not used */
			}
		});

		shell.pack();

		// width and height of the shell
		Rectangle rect = shell.getBounds();
		int[] size = { 0, 0 };
		size[0] = rect.width;
		size[1] = rect.height;

		// position the shell into the middle of the last window
		int[] position;
		position = EModGUI.shellPosition();
		shell.setLocation(position[0] - size[0] / 2, position[1] - size[1] / 2);

		// open the new shell
		shell.open();
	}

}
