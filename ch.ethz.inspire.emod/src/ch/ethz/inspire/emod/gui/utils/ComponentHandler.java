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

import java.io.File;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ch.ethz.inspire.emod.utils.PropertiesHandler;

public class ComponentHandler {

	//method to read the component db and write it to a tree
	public static void fillTree(Tree aTree){
		
		//read machinecomponent db folder from the current path
		String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/";
		File dir = new File(path);		
		File[] subDirs = dir.listFiles();
		Arrays.sort(subDirs);
		
		//iterate twice, first over categories of machinecomponents, second over the parameter sets of each component
		for (int i = 0; i < subDirs.length; i++){
			TreeItem child = new TreeItem(aTree, SWT.NONE);
			child.setText(subDirs[i].getName());
						
			//read the different parameter sets from subfolders
			String subpath = path + subDirs[i].getName() + "/";
			dir = new File(subpath);
			File[] subDirsComponents = dir.listFiles();
			Arrays.sort(subDirsComponents);
			
			//append parameter sets to their parent
			for(int j = 0; j < subDirsComponents.length; j++){
				TreeItem grandChild = new TreeItem(child, SWT.NONE);
				grandChild.setText(subDirsComponents[j].getName());
			}
		} 
	}
}
